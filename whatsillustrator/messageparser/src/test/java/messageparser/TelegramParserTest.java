package messageparser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import configurator.Global;
import helper.Misc;

class TelegramParserTest {

	@Test
	void testNextMessageMultipleTexts(@TempDir Path tmpDir) throws IOException {
		//tmpDir = Paths.get("/tmp/bla");
		Path jsonPath = tmpDir.resolve("chats.json");
		
		LocalDateTime dt1 = LocalDateTime.of(2023, 1, 15, 19, 27, 12);
		LocalDateTime dt2 = LocalDateTime.of(2023, 1, 15, 19, 27, 23);
		String json = ""
				+ createTextMessage("Terge", "This is message1", dt1)
				+ "," + createTextMessage("Biff", "This is message2", dt2);
		json = wrapAll("Tergechat", json);
		
		Files.createDirectories(tmpDir);
		Misc.writeAllText(jsonPath, json);
		
		String xmlConfig = getXmlConfig(jsonPath, "Tergechat");
		
		TelegramParser tp = new TelegramParser();
		try {
			tp.init(xmlConfig, new Global());
		} catch (ParserException e) {
			fail(e);
		}
		
		IMessage msg = null;
		TextMessage tm = null;
		
		msg = tp.nextMessage();
		assertNotNull(msg);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(dt1, tm.getTimepoint());
		assertEquals("Terge", tm.getSender());
		assertEquals(tm.getContent(), "This is message1");
		
		msg = tp.nextMessage();
		assertNotNull(msg);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(dt2, tm.getTimepoint());
		assertEquals("Biff", tm.getSender());
		assertEquals(tm.getContent(), "This is message2");
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testNextMessageMultipleChats(@TempDir Path tmpDir) throws IOException {
		tmpDir = Paths.get("/tmp/bla");
		Path jsonPath = tmpDir.resolve("chats.json");
		
		LocalDateTime dt1 = LocalDateTime.of(2023, 1, 15, 23, 23, 23);
		String msgChat1 = createTextMessage("from1", "message1", dt1);
		
		LocalDateTime dt2 = LocalDateTime.of(2023, 1, 15, 23, 24, 23);
		String msgChat2 = createTextMessage("from2", "message2", dt2);
		
		String chat1 = wrapChat("chat1", msgChat1);
		String chat2 = wrapChat("chat2", msgChat2);
		
		String json = ""
				+ "\"chats\": {"
				+ "\"list\": ["
				+ chat1
				+ ", " + chat2
				+ "]"
				+ "}";
		
		json = "{" + json + "}";
		
		Files.createDirectories(tmpDir);
		Misc.writeAllText(jsonPath, json);
		
		String xmlConfig = getXmlConfig(jsonPath, "chat2");
		
		TelegramParser tp = new TelegramParser();
		try {
			tp.init(xmlConfig, new Global());
		} catch (ParserException e) {
			fail(e);
		}
		
		IMessage msg = tp.nextMessage();
		assertNotNull(msg);
		assertEquals("from2", msg.getSender());
	}
	
	private String createTextMessage(String from, String message, LocalDateTime dt) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String dateStr = dt.format(dtf);
		
		String json = ""
				+ addKeyValue("id", 123, true)
				+ addKeyValue("type", "message")
				+ addKeyValue("date", dateStr)
				+ addKeyValue("from", from)
				+ addKeyValue("from_id", "user12345")
				+ addKeyValue("text", message);
		
		json = "{" + json + "}";
		
		return json;
	}
	
	private String getXmlConfig(Path msgPath, String chatName) {
		String xml = ""
				+ "<parserconfiguration>\n"
				+ "<messagepath>" + msgPath + "</messagepath>\n"
				+ "<chatname>" + chatName + "</chatname>\n"
				+ "</parserconfiguration>\n";
		return xml;
	}
	
	private String wrapAll(String chatName, String messages) {
		return "{" + wrapChats(chatName, messages) + "}";
	}
	
	private String wrapChats(String chatName, String messages) {
		String json = ""
				+ "\"chats\": {"
				+ "\"list\": ["
				+ wrapChat(chatName, messages)
				+ "]"
				+ "}";
				
		return json;
	}
	
	private String wrapChat(String chatName, String messages) {
		String json = ""
				+ "{"
				+ addKeyValue("name", chatName, true)
				+ addKeyValue("type", "personal_chat")
				+ addKeyValue("id", 12345)
				+ wrapMessages(messages)
				+ "}";
		
		return json;
	}

	private String wrapMessages(String messages) {
		String json = ""
				+ ",\n\"messages\": ["
				+ messages 
				+ "]";

		return json;
	}

	private String addKeyValue(String key, String value, boolean first) {
		String comma = first ? "" : ",";
		return String.format("%s\n\"%s\": \"%s\"", comma, key, value);
	}
	
	private String addKeyValue(String key, String value) {
		return addKeyValue(key, value, false);
	}
	
	private String addKeyValue(String key, int value, boolean first) {
		String comma = first ? "" : ",";
		return String.format("%s\n\"%s\": %d", comma, key, value);
	}
	
	private String addKeyValue(String key, int value) {
		return addKeyValue(key, value, false);
	}
}
