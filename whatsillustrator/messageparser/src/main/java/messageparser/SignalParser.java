package messageparser;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
import org.xml.sax.SAXException;

import configurator.Global;
import helper.Xml;
import signalbackupreader.DatabaseAndBlobDumper;
import signalbackupreader.SignalBackupReaderException;

public class SignalParser implements IParser {
	
	private final String TYPE_MMS = "mms";
	private final String TYPE_SMS = "sms";
	
	private static Logger logger = LogManager.getLogger(SignalParser.class);

	private Global globalConfig;
	private Path backupFilePath;
	private String chatName;
	// Direct path to extracted sql database --> for testing and debugging
	private Path sqliteDBPath;
	private Path workdir;
	private ResultSet resultSet;
	private Connection connection;
	private List<IMessage> messages;
	private int messageIndex;
	
	private LocalDateTime dtmin;
	private LocalDateTime dtmax;

	@Override
	public void init(String xmlConfig, Global globalConfig) throws ParserException {
		String passphrase;
		try {
			Document document = Xml.documentFromString(xmlConfig);
			
			this.globalConfig = globalConfig;
			this.backupFilePath = Xml.getPathFromNode(document, "//backupfile");
			passphrase = Xml.getTextFromNode(document, "//passphrase");
			this.chatName = Xml.getTextFromNode(document, "//chatname");
			this.sqliteDBPath = Xml.getPathFromNode(document, "//sqlitedbpath");
			
			this.dtmin = Xml.getLocalDateTimeFromNode(document, "//mindate");
			this.dtmax = Xml.getLocalDateTimeFromNode(document, "//maxdate");
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			throw new ParserException("Could not read xml configuration", e);
		}
		
		this.workdir = this.globalConfig.getOutputDir().resolve("signalparser");
		
		if(this.sqliteDBPath==null) {
			DatabaseAndBlobDumper dumper;
			try {
				dumper = DatabaseAndBlobDumper.of(backupFilePath, passphrase, this.workdir);
				dumper.setCreateExtraSqlViews(true);
				dumper.setAllowsOverrides(true);
				dumper.run();
			} catch (SignalBackupReaderException | SQLException e) {
				throw new ParserException("Could not dump database and blobs", e);
			}
			
			this.sqliteDBPath = dumper.getSqliteOutputPath();
		}
		
		this.messages = new ArrayList<IMessage>();
		this.messageIndex = 0;
		
		try {
			String url = String.format("jdbc:sqlite:%s", sqliteDBPath);
			connection = DriverManager.getConnection(url);

			String query = "select msgid, date, sender, chatname, text, type from v_chats where chatname=? order by date";
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
	
	public void parseMessages() throws SQLException, ParserException {
		while (	resultSet.next()) {
			long unixtime = resultSet.getLong("date");
			LocalDateTime timepoint = Instant.ofEpochMilli(unixtime).atZone(ZoneId.systemDefault()).toLocalDateTime();
			String sender = resultSet.getString("sender");
			String text = resultSet.getString("text");
			long messageId = resultSet.getLong("msgid");
			
			if(dtmin!=null && timepoint.isBefore(dtmin)) {
				continue;
			}
			
			if(dtmax!=null && timepoint.isAfter(dtmax)) {
				continue;
			}
			
			String type = resultSet.getString("type");
			if (type.equals(TYPE_SMS)) {
				messages.add(new TextMessage(timepoint, sender, text));
			} else if (type.equals(TYPE_MMS)) {
				parseMMS(timepoint, sender, text, messageId);
			}
		}
		
		if(this.messages.size() == 0) {
			String sql = "SELECT DISTINCT(chatname) cn FROM v_chats WHERE cn NOT NULL ORDER BY cn ASC";
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

	private void parseMMS(LocalDateTime timepoint, String sender, String text, long messageId) throws SQLException, ParserException {
		String query = "select ct, unique_id, sticker_id from part where mid=?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setLong(1, messageId);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()) {
			String contentType = rs.getString("ct");
			long uniqueId = rs.getLong("unique_id");
			@SuppressWarnings("unused")
			long stickerId = rs.getLong("sticker_id");
			
			if(contentType.equalsIgnoreCase("image/webp")) {
				handleWebp(timepoint, sender, messageId);
			}
			else if(contentType.equalsIgnoreCase("image/jpeg")) {
				Path dst = copyAttachment("image", "jpg", uniqueId);
				ImageMessage im = new ImageMessage(timepoint, sender, dst, text);
				messages.add(im);
			}
			else if(contentType.equalsIgnoreCase("video/mp4")) {
				Path dst = copyAttachment("video", "mp4", uniqueId);
				VideoMessage vm = new VideoMessage(timepoint, sender, dst, text);
				messages.add(vm);
			}
			else {
				logger.warn("Contenttype '{}' not supported, messageId '{}'", contentType, messageId);
			}
		}
	}

	@Override
	public String getNameSuggestion() {
		return "Signal " + this.chatName;
	}
	
	private void handleWebp(LocalDateTime timepoint, String sender, long messageId) throws SQLException {
		String query = "SELECT file_id, sticker_emoji  FROM v_stickers WHERE mid=?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setLong(1, messageId);
		
		ResultSet rs = pstmt.executeQuery();
		if(!rs.next()) {
			String msg = String.format("Could not find sticker in v_sticker for messageId '%d', skipping message", messageId);
			return;
		}
		
		long fileId = rs.getLong("file_id");
		
		// sometimes the sticker is not available --> fallback to emoji in this case
		if(rs.wasNull()) {
			String emoji = rs.getString("sticker_emoji");
			TextMessage tm = new TextMessage(timepoint, sender, emoji);
			messages.add(tm);
			return;
		}
		
		String prefix = String.format("Sticker_%04d", fileId);
		Path src = workdir.resolve(String.format("%s.bin", prefix));
		Path dst = workdir.resolve(String.format("%s.webp", prefix));
		
		try {
			Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
			String msg = String.format("Copied sticker from '%s' to '%s'", src, dst);
			logger.info(msg);
			
			StickerMessage sm = new StickerMessage(timepoint, sender, dst);
			messages.add(sm);
		} catch (IOException e) {
			String msg = String.format("Could not copy sticker from '%s' to '%s', exception '%s'", src, dst, e);
			logger.warn(msg);
		}
	}
	
	private Path copyAttachment(String prefix, String extension, long uniqueId) throws ParserException {
		Path src = workdir.resolve(String.format("Attachment_%d.bin", uniqueId));
		Path dst = workdir.resolve(String.format("%s_%d.%s", prefix, uniqueId, extension));
		try {
			Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
			String msg = String.format("Copied attachment from '%s' to '%s'", src, dst);
			logger.info(msg);
		} catch (IOException e) {
			String msg = String.format("Could not copy attachment from '%s' to '%s', exception '%s'", src, dst, e);
			logger.warn(msg);
		}
		
		return dst;
	}
}
