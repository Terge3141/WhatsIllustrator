package messageparser;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import configurator.Global;
import helper.Xml;
import whatsappbackupreader.DatabaseDumper;
import whatsappbackupreader.WhatsappBackupReaderException;

public class WhatsappBackupParser implements IParser {
	private final String TYPE_TEXT = "TEXT";
	private final String TYPE_PICTURE = "PICTURE";
	private final String TYPE_VIDEO = "VIDEO";
	
	private static Logger logger = LogManager.getLogger(WhatsappBackupParser.class);
	
	private Global globalConfig;
	private Path cryptFilePath;
	private String chatName;
	private Path msgstoreDBPath;
	private Path contactsCsvPath;
	private Path workdir;
	// Directory where one of the subdirectories is "Media"
	private List<Path> whatsappdirs;
	
	private LocalDateTime dtmin;
	private LocalDateTime dtmax;
	
	private Connection connection;
	private ResultSet resultSet;
	private List<IMessage> messages;
	private int messageIndex;
	
	@Override
	public void init(String xmlConfig, Global globalConfig) throws ParserException {
		byte[] key;
		try {
			Document document = Xml.documentFromString(xmlConfig);
			
			this.globalConfig = globalConfig;
			this.cryptFilePath = Xml.getPathFromNode(document, "//cryptfile");
			
			// passphrase my contain white spaces, those will be deleted by whatsappbackupreader
			String keyStr = Xml.getTextFromNode(document, "//passphrase");
			key = keyStr.getBytes();
			
			this.chatName = Xml.getTextFromNode(document, "//chatname");
			this.msgstoreDBPath = Xml.getPathFromNode(document, "//msgstoredbpath");
			this.contactsCsvPath = Xml.getPathFromNode(document, "//contactscsvpath");
			
			this.dtmin = str2Dt(Xml.getTextFromNode(document, "//mindate"));
			this.dtmax = str2Dt(Xml.getTextFromNode(document, "//maxdate"));
			
			this.whatsappdirs = new ArrayList<Path>();
			NodeList nodes = Xml.selectNodes(document, "//whatsappdir");
			for(int i=0; i<nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String str = node.getTextContent();
				
				if(str!=null) {
					this.whatsappdirs.add(Paths.get(str));
				}
			}
			
			this.workdir = this.globalConfig.getOutputDir().resolve("whatsappparser");
			Files.createDirectories(this.workdir);
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			throw new ParserException("Could not read xml configuration", e);
		}
		
		if(this.msgstoreDBPath==null) {
			this.msgstoreDBPath = this.workdir.resolve("msgstore.db");
			
			DatabaseDumper dumper;
			try {
				dumper = DatabaseDumper.of(cryptFilePath, key, this.msgstoreDBPath);
				dumper.setCreateExtraSqlViews(true);
				dumper.readContacts(contactsCsvPath);
				dumper.run();
			} catch (WhatsappBackupReaderException | SQLException | IOException e) {
				throw new ParserException("Could not dump database", e);
			}
		}
		
		this.messages = new ArrayList<IMessage>();
		this.messageIndex = 0;
		
		try {
			String url = String.format("jdbc:sqlite:%s", msgstoreDBPath);
			connection = DriverManager.getConnection(url);
			
			String query = "select messageid, chatname, sendername, type_description, text, timestamp from v_messages where chatname=? order by timestamp";
			PreparedStatement pstmt = connection.prepareStatement(query);
			pstmt.setString(1, chatName);
			resultSet = pstmt.executeQuery();

			parseMessages();
		} catch (SQLException e) {
			throw new ParserException("Could not execute message query", e);
		}
	}

	@Override
	public IMessage nextMessage() {
		if(messageIndex>=messages.size()) {
			return null;
		}
		
		IMessage msg = messages.get(messageIndex);
		messageIndex++;
		return msg;
	}

	@Override
	public String getNameSuggestion() {
		return "Whatsapp " + this.chatName;
	}
	
	private void parseMessages() throws SQLException, ParserException {
		while (resultSet.next()) {
			long unixtime = resultSet.getLong("timestamp");
			LocalDateTime timepoint = Instant.ofEpochMilli(unixtime).atZone(ZoneId.systemDefault()).toLocalDateTime();
			String sender = resultSet.getString("sendername");
			String text = resultSet.getString("text");
			long messageId = resultSet.getLong("messageid");
			
			if(dtmin!=null && timepoint.isBefore(dtmin)) {
				continue;
			}
			
			if(dtmax!=null && timepoint.isAfter(dtmax)) {
				continue;
			}
			
			String type = resultSet.getString("type_description");
			if (type.equals(TYPE_TEXT)) {
				messages.add(new TextMessage(timepoint, sender, text));
			} else {
				parseMediaMessage(timepoint, sender, type, text, messageId);
			}
		}
		
		if(messages.size()==0) {
			String sql = "SELECT DISTINCT(chatname) cn FROM v_messages WHERE cn NOT NULL ORDER BY cn ASC";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			Path chatNamesPath = this.workdir.resolve("chats.txt");
			FileWriter writer;
			try {
				writer = new FileWriter(chatNamesPath.toFile());
				while(rs.next()) {
					writer.append(rs.getString("cn") + "\n");
				}
				writer.close();
			} catch (IOException e) {
				throw new ParserException("No messages for chat and criteria found. Could not write chats to " + chatNamesPath);
			}
			
			throw new ParserException("No messages for chat and criteria found. Available chats written to " + chatNamesPath);
		}
	}

	private void parseMediaMessage(LocalDateTime timepoint, String sender, String type, String text, long messageId) throws SQLException {
		String sql = "select file_path from message_media where message_row_id=?";
		PreparedStatement pstmt = connection.prepareStatement(sql);
		pstmt.setLong(1, messageId);
		ResultSet rs = pstmt.executeQuery();
		
		while(rs.next()) {
			String filePath = rs.getString("file_path");
			if(filePath==null) {
				logger.warn("No file_path for messageId '{}' specified, skipping message", messageId);
				continue;
			}
			
			Path absFilePath = searchFile(filePath);
			if(absFilePath==null) {
				logger.warn("Relative file_path '{}' not found in the whatsapp directories, skipping message", filePath);
				continue;
			}
			
			if(type.equalsIgnoreCase(TYPE_PICTURE)) {
				ImageMessage im = new ImageMessage(timepoint, sender, absFilePath, text);
				this.messages.add(im);
			}
			else if(type.equalsIgnoreCase(TYPE_VIDEO)) {
				VideoMessage vm = new VideoMessage(timepoint, sender, absFilePath, text);
				this.messages.add(vm);
			}
			else {
				logger.warn("Type '{}' not supported, messageId '{}'", type, messageId);
			}
		}
	}
	
	private LocalDateTime str2Dt(String dateStr) {
		if(dateStr==null) {
			return null;
		}
		
		return LocalDateTime.parse(dateStr);
	}
	
	// searches for a file with a given relPath in whatsappdirs-list
	// returns the absolute path if file is found, null if not found
	private Path searchFile(String relPath) {
		for(Path wadir : this.whatsappdirs) {
			Path p = wadir.resolve(relPath);
			if(Files.exists(p)) {
				return p;
			}
		}
		
		return null;
	}
}
