package messageparser;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	private Path workdir;
	// Directory where one of the subdirectories is "Media"
	private Path whatsappdir;
	
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
			
			// TODO trim keyStr
			String keyStr = Xml.getTextFromNode(document, "//passphrase");
			key = keyStr.getBytes();
			
			this.chatName = Xml.getTextFromNode(document, "//chatname");
			this.whatsappdir = Xml.getPathFromNode(document, "//whatsappdir");
			this.msgstoreDBPath = Xml.getPathFromNode(document, "//msgstoredbpath");
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			throw new ParserException("Could not read xml configuration", e);
		}
		
		this.workdir = this.globalConfig.getOutputDir().resolve("whatsappparser");
		
		if(this.msgstoreDBPath==null) {
			this.msgstoreDBPath = this.workdir.resolve("msgstore.db");
			
			DatabaseDumper dumper;
			try {
				dumper = DatabaseDumper.of(cryptFilePath, key, this.workdir);
				dumper.setCreateExtraSqlViews(true);
				dumper.run();
			} catch (WhatsappBackupReaderException | SQLException e) {
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
			
			String type = resultSet.getString("type_description");
			if (type.equals(TYPE_TEXT)) {
				messages.add(new TextMessage(timepoint, sender, text));
			} else {
				parseMediaMessage(timepoint, sender, type, text, messageId);
			}
		}
	}

	private void parseMediaMessage(LocalDateTime timepoint, String sender, String type, String text, long messageId) throws SQLException {
		String sql = "select file_path from message_media where message_row_id=?";
		PreparedStatement pstmt = connection.prepareStatement(sql);
		pstmt.setLong(1, messageId);
		ResultSet rs = pstmt.executeQuery();
		
		while(rs.next()) {
			String filePath = rs.getString("file_path");
			if(type.equalsIgnoreCase(TYPE_PICTURE)) {
				ImageMessage im = new ImageMessage(timepoint, sender, this.whatsappdir.resolve(filePath), text);
				this.messages.add(im);
			}
			else if(type.equalsIgnoreCase(TYPE_VIDEO)) {
				VideoMessage vm = new VideoMessage(timepoint, sender, this.whatsappdir.resolve(filePath), text);
				this.messages.add(vm);
			}
			else {
				logger.warn("Type '{}' not supported", type);
			}
		}
	}
}
