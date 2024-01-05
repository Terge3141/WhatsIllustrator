package messageparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import configurator.ConfigurationException;
import configurator.Global;

public class WhatsappBackupParserTest {
	@Test
	void testNextMessageMultipleTexts(@TempDir Path tmpDir) throws SQLException, IOException, ParserException {
		Path inputDir = tmpDir.resolve("input");
		WhatsappBackupParserSQLMocker sm = new WhatsappBackupParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		ZonedDateTime zdt1 = createZonedDT(2023, 1, 21, 23, 23, 40);
		ZonedDateTime zdt2 = createZonedDT(2023, 1, 21, 23, 24, 41);
		createTextMessage(con, 1, zdt1, "From1", "Mychat", "Content1");
		createTextMessage(con, 2, zdt2, "From2", "Mychat", "Content2");
		
		con.close();

		WhatsappBackupParser wbp = createWhatsappBackupParser(tmpDir, sm.getSqliteDBPath(), "Mychat");
		
		IMessage msg;
		TextMessage tm;
		
		msg = wbp.nextMessage();
		checkBaseMessage(msg, "From1", zdt1);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals("Content1", tm.getContent());
		
		msg = wbp.nextMessage();
		checkBaseMessage(msg, "From2", zdt2);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals("Content2", tm.getContent());
		
		assertNull(wbp.nextMessage());
	}
	
	@Test
	void testNextMessageMultipleChats(@TempDir Path tmpDir) throws IOException, SQLException, ParserException {
		Path inputDir = tmpDir.resolve("input");
		WhatsappBackupParserSQLMocker sm = new WhatsappBackupParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		ZonedDateTime zdt1 = createZonedDT(2023, 1, 21, 23, 23, 40);
		ZonedDateTime zdt2 = createZonedDT(2023, 1, 21, 23, 24, 41);
		createTextMessage(con, 1, zdt1, "From1", "Mychat1", "Content1");
		createTextMessage(con, 2, zdt2, "From2", "Mychat2", "Content2");
		
		con.close();

		WhatsappBackupParser wbp = createWhatsappBackupParser(tmpDir, sm.getSqliteDBPath(), "Mychat2");
		
		IMessage msg;
		TextMessage tm;
		
		msg = wbp.nextMessage();
		checkBaseMessage(msg, "From2", zdt2);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals("Content2", tm.getContent());
		
		assertNull(wbp.nextMessage());
	}
	
	@Test
	void testNextMessageImage(@TempDir Path tmpDir) throws SQLException, IOException, ParserException {
		Path inputDir = tmpDir.resolve("input");
		WhatsappBackupParserSQLMocker sm = new WhatsappBackupParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		ZonedDateTime zdt = createZonedDT(2023, 1, 21, 23, 23, 40);
		Path whatsappDir = getWhatsappDir(tmpDir);
		Files.createDirectories(whatsappDir);
		createMultiMediaMessage(con, whatsappDir, 123, zdt, "From", "Mychat", "Text", "images/myimage.jpg", "PICTURE");
		
		con.close();
		
		WhatsappBackupParser wbp = createWhatsappBackupParser(tmpDir, sm.getSqliteDBPath(), "Mychat");
		
		IMessage msg = wbp.nextMessage();
		checkBaseMessage(msg, "From", zdt);
		assertTrue(msg instanceof ImageMessage);
		ImageMessage im = (ImageMessage)msg;
		
		assertEquals("Text", im.getSubscription());
		Path expImagePath = whatsappDir.resolve("images/myimage.jpg");
		assertEquals(expImagePath, im.getFilepath());
		
		assertNull(wbp.nextMessage());
	}
	
	@Test
	void testNextMessageVideo(@TempDir Path tmpDir) throws SQLException, IOException, ParserException {
		Path inputDir = tmpDir.resolve("input");
		WhatsappBackupParserSQLMocker sm = new WhatsappBackupParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		ZonedDateTime zdt = createZonedDT(2023, 1, 21, 23, 23, 40);
		Path whatsappDir = getWhatsappDir(tmpDir);
		Files.createDirectories(whatsappDir);
		createMultiMediaMessage(con, whatsappDir, 123, zdt, "From", "Mychat", "Text", "videos/myvideo.mpg", "VIDEO");
		
		con.close();
		
		WhatsappBackupParser wbp = createWhatsappBackupParser(tmpDir, sm.getSqliteDBPath(), "Mychat");
		
		IMessage msg = wbp.nextMessage();
		checkBaseMessage(msg, "From", zdt);
		assertTrue(msg instanceof VideoMessage);
		VideoMessage vm = (VideoMessage)msg;
		
		assertEquals("Text", vm.getSubscription());
		Path expImagePath = whatsappDir.resolve("videos/myvideo.mpg");
		assertEquals(expImagePath, vm.getFilepath());
		
		assertNull(wbp.nextMessage());
	}
	
	@Test
	void testNextMessageUnknownContentType(@TempDir Path tmpDir) throws SQLException, IOException, ParserException {
		Path inputDir = tmpDir.resolve("input");
		WhatsappBackupParserSQLMocker sm = new WhatsappBackupParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		ZonedDateTime zdt = createZonedDT(2023, 1, 21, 23, 23, 40);
		Path whatsappDir = getWhatsappDir(tmpDir);
		Files.createDirectories(whatsappDir);
		createMultiMediaMessage(con, whatsappDir, 123, zdt, "From", "Mychat", "Text", "bla/mydoc.dat", "DAT");
		createTextMessage(con, 124, zdt, "Frpm", "Mychat", ",mymessage");
	
		WhatsappBackupParser wbp = createWhatsappBackupParser(tmpDir, sm.getSqliteDBPath(), "Mychat");
		
		assertNotNull(wbp.nextMessage());
		assertNull(wbp.nextMessage());
	}
	
	@Test
	void testNoMessagesInChat(@TempDir Path tmpDir) throws SQLException, IOException {
		Path inputDir = tmpDir.resolve("input");
		WhatsappBackupParserSQLMocker sm = new WhatsappBackupParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		ZonedDateTime zdt1 = createZonedDT(2023, 1, 21, 23, 23, 40);
		ZonedDateTime zdt2 = createZonedDT(2023, 1, 21, 23, 24, 41);
		createTextMessage(con, 1, zdt1, "From1", "Mychat1", "Content1");
		createTextMessage(con, 2, zdt2, "From2", "Mychat2", "Content2");
		createTextMessage(con, 2, zdt2, "From2", "Mychat2", "Content3");
		
		con.close();

		try {
			createWhatsappBackupParser(tmpDir, sm.getSqliteDBPath(), "Mychat3");
			fail("ParserException expected");
		} catch (ParserException e) {
		}
		
		Path chatsPath = getWorkDir(tmpDir).resolve("whatsappparser").resolve("chats.txt");
		assertTrue(Files.exists(chatsPath));
		
		List<String> lines = Files.readAllLines(chatsPath);
		assertEquals("Mychat1", lines.get(0));
		assertEquals("Mychat2", lines.get(1));
	}
	
	@Test
	void testFilePathNull(@TempDir Path tmpDir) throws SQLException, IOException, ParserException {
		Path inputDir = tmpDir.resolve("input");
		WhatsappBackupParserSQLMocker sm = new WhatsappBackupParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		ZonedDateTime zdt = createZonedDT(2023, 1, 21, 23, 23, 40);
		Path whatsappDir = getWhatsappDir(tmpDir);
		Files.createDirectories(whatsappDir);
		createTextMessage(con, 122, zdt, "From", "Mychat", "MyMessage");
		createMultiMediaMessage(con, whatsappDir, 123, zdt, "From", "Mychat", "Text", null, "VIDEO");
		
		con.close();
		
		WhatsappBackupParser wbp = createWhatsappBackupParser(tmpDir, sm.getSqliteDBPath(), "Mychat");
		
		IMessage msg = wbp.nextMessage();
		checkBaseMessage(msg, "From", zdt);
		assertTrue(msg instanceof TextMessage);
	}
	
	@Test
	void testTwoMediaDirs(@TempDir Path tmpDir) throws SQLException, IOException, ParserException {
		Path inputDir = tmpDir.resolve("input");
		WhatsappBackupParserSQLMocker sm = new WhatsappBackupParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		String chatName = "MyChat";
		
		Path whatsappDir1 = tmpDir.resolve("wa1");
		Path whatsappDir2 = tmpDir.resolve("wa2");
		
		Files.createDirectories(whatsappDir1);
		Files.createDirectories(whatsappDir2);
		
		String additionalTag = "<whatsappdir>" + whatsappDir2 + "</whatsappdir>";
		String xmlConfig = createXmlConfig(sm.getSqliteDBPath(), whatsappDir1, chatName, additionalTag);
		
		ZonedDateTime zdt = createZonedDT(2023, 1, 21, 23, 23, 40);
		createMultiMediaMessage(con, whatsappDir1, 123, zdt, "From", chatName, "Text", "images/myimage1.jpg", "PICTURE");
		createMultiMediaMessage(con, whatsappDir2, 123, zdt, "From", chatName, "Text", "images/myimage2.jpg", "PICTURE");
		
		con.close();
		
		Path workDir = getWorkDir(tmpDir);
		Global global = initGlobal(workDir);
		
		WhatsappBackupParser wbp = new WhatsappBackupParser();
		wbp.init(xmlConfig, global);
		
		IMessage msg;
		ImageMessage im;
		
		msg = wbp.nextMessage();
		checkBaseMessage(msg, "From", zdt);
		assertTrue(msg instanceof ImageMessage);
		im = (ImageMessage)msg;
		assertEquals(whatsappDir1.resolve("images/myimage1.jpg"), im.getFilepath());
		
		msg = wbp.nextMessage();
		checkBaseMessage(msg, "From", zdt);
		assertTrue(msg instanceof ImageMessage);
		im = (ImageMessage)msg;
		assertEquals(whatsappDir2.resolve("images/myimage2.jpg"), im.getFilepath());
	}
	
	private void createTextMessage(Connection con, int msgid, ZonedDateTime zdt, String sender, String chatname, String text) throws SQLException {
		createMessagesEntry(con, msgid, zdt, sender, chatname, text, "TEXT");
	}

	private void createMultiMediaMessage(Connection con, Path mediaDir, int msgid, ZonedDateTime zdt, String sender, String chatname, String text, String relFilePath, String type) throws SQLException, IOException {
		createMessagesEntry(con, msgid, zdt, sender, chatname, text, type);
		
		String sql = "INSERT INTO message_media (message_row_id, file_path) VALUES (?, ?);";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setInt(1, msgid);
		pstmt.setString(2, relFilePath);
		
		pstmt.execute();
		
		if(relFilePath != null) {
			Path filePath = mediaDir.resolve(relFilePath);
			createFilePath(filePath);
		}
	}
	
	private void createMessagesEntry(Connection con, int msgid, ZonedDateTime zdt, String sender, String chatname, String text,
			String type) throws SQLException {
		long unixtime = zdt.toInstant().toEpochMilli();
		
		String sql = "INSERT INTO v_messages (messageid, chatname, sendername, type_description, text, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		pstmt.setInt(1, msgid);
		pstmt.setString(2, chatname);
		pstmt.setString(3, sender);
		pstmt.setString(4, type);
		pstmt.setString(5, text);
		pstmt.setLong(6, unixtime);
		
		pstmt.execute();
	}

	private WhatsappBackupParser createWhatsappBackupParser(Path tmpDir, Path sqliteDBPath, String chatName) throws ParserException  {
		String xmlConfig = createXmlConfig(sqliteDBPath, getWhatsappDir(tmpDir), chatName);
		
		Path workDir = getWorkDir(tmpDir);
		Global global = initGlobal(workDir);
		
		WhatsappBackupParser wbp = new WhatsappBackupParser();
		wbp.init(xmlConfig, global);
		
		return wbp;
	}

	private Global initGlobal(Path outputDir) {
		try {
			return Global.fromXmlString("<global>"
					+ "<outputdir>"
					+ outputDir
					+ "</outputdir>"
					+ "<locale>en</locale>"
					+ "</global>");
		} catch (XPathExpressionException | ConfigurationException | ParserConfigurationException | SAXException
				| IOException e) {
			fail(e);
		}
		
		// this should not happen due to fail(e) in catch
		return null;
	}
	
	private ZonedDateTime createZonedDT(int year, int month, int dayOfMonth, int hour, int minute, int second) {
		LocalDateTime ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
		return ZonedDateTime.of(ldt, ZoneId.systemDefault());
	}

	private String createXmlConfig(Path sqliteDBPath, Path whatsappDir, String chatName, String additionalTags) {
		String xml = ""
				+ "<parserconfiguration>"
				+ "<backupfile>/tmp/msgstore.db.crypt15</backupfile>"
				+ "<passphrase>12345678 12345678 12345678 12345678"
				+ " 12345678 12345678 12345678 12345678</passphrase>"
				+ "<msgstoredbpath>" + sqliteDBPath + "</msgstoredbpath>"
				+ "<whatsappdir>" + whatsappDir + "</whatsappdir>"
				+ "<chatname>" + chatName + "</chatname>";
		
		if(additionalTags != null) {
			xml += additionalTags;
		}
		
		xml += "</parserconfiguration>";
		return xml;
	}
	
	private String createXmlConfig(Path sqliteDBPath, Path whatsappDir, String chatName) {
		return createXmlConfig(sqliteDBPath, whatsappDir, chatName, null);
	}
	
	private void createFilePath(Path filePath) throws IOException {
		File f = filePath.toFile();
		f.mkdirs();
		f.createNewFile();
	}
	
	private void checkBaseMessage(IMessage msg, String sender, ZonedDateTime zdt) {
		assertNotNull(msg);
		assertEquals(sender, msg.getSender());
		assertEquals(zdt, msg.getTimepoint().atZone(ZoneId.systemDefault()));
	}
	
	private Path getWorkDir(Path tmpDir) {
		return tmpDir.resolve("workdir");
	}
	
	// Directory where one of the subdirectories is "Media"
	private Path getWhatsappDir(Path tmpDir) {
		return getWorkDir(tmpDir).resolve("whatsapp");
	}
}
