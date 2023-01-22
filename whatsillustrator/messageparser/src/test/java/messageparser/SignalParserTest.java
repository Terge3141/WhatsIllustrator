package messageparser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import configurator.ConfigurationException;
import configurator.Global;

class SignalParserTest {

	@Test
	void testNextMessageMultipleTexts(@TempDir Path tmpDir) throws SQLException, IOException {
		Path inputDir = tmpDir.resolve("input");
		SignalParserSQLMocker sm = new SignalParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		ZonedDateTime zdt1 = createZonedDT(2023, 1, 21, 23, 23, 40);
		ZonedDateTime zdt2 = createZonedDT(2023, 1, 21, 23, 24, 41);
		createTextMessage(con, 1, zdt1, "From1", "Mychat", "Content1");
		createTextMessage(con, 2, zdt2, "From2", "Mychat", "Content2");
		
		con.close();

		SignalParser sp = createSignalParser(tmpDir, sm.getSqliteDBPath(), "Mychat");
		
		IMessage msg;
		TextMessage tm;
		
		msg = sp.nextMessage();
		checkBaseMessage(msg, "From1", zdt1);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals("Content1", tm.getContent());
		
		msg = sp.nextMessage();
		checkBaseMessage(msg, "From2", zdt2);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals("Content2", tm.getContent());
		
		assertNull(sp.nextMessage());
	}
	
	@Test
	void testNextMessageMultipleChats(@TempDir Path tmpDir) throws IOException, SQLException {
		Path inputDir = tmpDir.resolve("input");
		SignalParserSQLMocker sm = new SignalParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		ZonedDateTime zdt1 = createZonedDT(2023, 1, 21, 23, 23, 40);
		ZonedDateTime zdt2 = createZonedDT(2023, 1, 21, 23, 24, 41);
		createTextMessage(con, 1, zdt1, "From1", "Mychat1", "Content1");
		createTextMessage(con, 2, zdt2, "From2", "Mychat2", "Content2");
		
		con.close();

		SignalParser sp = createSignalParser(tmpDir, sm.getSqliteDBPath(), "Mychat2");
		
		IMessage msg;
		TextMessage tm;
		
		msg = sp.nextMessage();
		checkBaseMessage(msg, "From2", zdt2);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals("Content2", tm.getContent());
		
		assertNull(sp.nextMessage());
	}
	
	@Test
	void testNextMessageImage(@TempDir Path tmpDir) throws SQLException, IOException {
		Path inputDir = tmpDir.resolve("input");
		SignalParserSQLMocker sm = new SignalParserSQLMocker(inputDir);
		Connection con = sm.getConnection();
		
		ZonedDateTime zdt = createZonedDT(2023, 1, 21, 23, 23, 40);
		Path signalDir = tmpDir.resolve("workdir").resolve("signalparser");
		Files.createDirectories(signalDir);
		createMultiMediaMessage(con, signalDir, 123, zdt, "From", "Mychat", "Text", "image/jpeg", 123456, 2345);
		
		con.close();
		
		SignalParser sp = createSignalParser(tmpDir, sm.getSqliteDBPath(), "Mychat");
		
		IMessage msg = sp.nextMessage();
		checkBaseMessage(msg, "From", zdt);
		assertTrue(msg instanceof ImageMessage);
		ImageMessage im = (ImageMessage)msg;
		
		assertEquals("Text", im.getSubscription());
		Path expImagePath = signalDir.resolve("image_123456.jpg");
		assertEquals(expImagePath, im.getFilepath());
		
		assertNull(sp.nextMessage());
	}
	
	private void createTextMessage(Connection con, int msgid, ZonedDateTime zdt, String sender, String chatname, String text) throws SQLException {
		createChatsEntry(con, msgid, zdt, sender, chatname, text, "sms");
	}

	private void createMultiMediaMessage(Connection con, Path signalDir, int msgid, ZonedDateTime zdt, String sender, String chatname, String text, String contentType, long uniqueId, long stickerId) throws SQLException, IOException {
		createChatsEntry(con, msgid, zdt, sender, chatname, text, "mms");
		
		String sql = "INSERT INTO part (mid, ct, unique_id, sticker_id) VALUES (?, ?, ?, ?);";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setLong(1, msgid);
		pstmt.setString(2, contentType);
		pstmt.setLong(3, uniqueId);
		pstmt.setLong(4, stickerId);
		
		pstmt.execute();
		
		Path attachmentPath = signalDir.resolve(String.format("Attachment_%d.bin", uniqueId));
		
		attachmentPath.toFile().createNewFile();
	}
	
	private void createChatsEntry(Connection con, int msgid, ZonedDateTime zdt, String sender, String chatname, String text,
			String type) throws SQLException {
		long unixtime = zdt.toInstant().toEpochMilli();
		
		String sql = "INSERT INTO v_chats (msgid, date, sender, chatname, text, type) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		pstmt.setInt(1, msgid);
		pstmt.setLong(2, unixtime);
		pstmt.setString(3, sender);
		pstmt.setString(4, chatname);
		pstmt.setString(5, text);
		pstmt.setString(6, type);
		
		pstmt.execute();
	}

	private SignalParser createSignalParser(Path tmpDir, Path sqliteDBPath, String chatName)  {
		String xmlConfig = createXmlConfig(sqliteDBPath, chatName);
		
		Path workDir = tmpDir.resolve("workdir");
		Global global = null;
		try {
			global = Global.fromXmlString("<global>"
					+ "<outputdir>"
					+ workDir
					+ "</outputdir>"
					+ "</global>");
		} catch (XPathExpressionException | ConfigurationException | ParserConfigurationException | SAXException
				| IOException e) {
			fail(e);
		}
		
		SignalParser sp = new SignalParser();
		try {
			sp.init(xmlConfig, global);
		} catch (ParserException e) {
			fail(e);
		}
		
		return sp;
	}
	
	private ZonedDateTime createZonedDT(int year, int month, int dayOfMonth, int hour, int minute, int second) {
		LocalDateTime ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
		return ZonedDateTime.of(ldt, ZoneId.systemDefault());
	}

	private String createXmlConfig(Path sqliteDBPath, String chatName) {
		String xml = ""
				+ "<parserconfiguration>\n"
				+ "<backupfile>/tmp/signal-2023-01-21-21-254-17.backup</backupfile>\n"
				+ "<passphrase>12345 12345 12345 12345 12345 12345</passphrase>\n"
				+ "<sqlitedbpath>" + sqliteDBPath + "</sqlitedbpath>"
				+ "<chatname>" + chatName + "</chatname>"
				//+ "<messagedir>" + tmpDir + "</messagedir>\n"
				//+ "                        <imagepooldir>Path to all whatsapp images (used for ImageMatcher)\n"
				//+ "                        </imagepooldir>\n"
				+ "</parserconfiguration>\n";
		return xml;
	}
	
	void checkBaseMessage(IMessage msg, String sender, ZonedDateTime zdt) {
		assertNotNull(msg);
		assertEquals(sender, msg.getSender());
		assertEquals(zdt, msg.getTimepoint().atZone(ZoneId.systemDefault()));
	}
}
