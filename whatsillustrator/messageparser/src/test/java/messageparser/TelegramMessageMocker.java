package messageparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import configurator.Global;
import helper.Misc;

public class TelegramMessageMocker {
	private Path tempDir;
	private boolean firstMessage;
	private String json = "";
	private String xmlConfig = null;
	
	public TelegramMessageMocker(Path tempDir) {
		this.tempDir = tempDir;
		this.firstMessage = true;
	}
	
	public void addMessage(String jsonMessage) {
		if(!firstMessage) {
			json += ",";
		}
		firstMessage = false;
		
		json = json + "{" + jsonMessage + "}";
	}
	
	public void addTextMessage(String from, String message, LocalDateTime dt) {
		addMessage(createBaseMessage(from, message, dt));
	}
	
	public TelegramParser createTelegramParser(String chatName) throws IOException {
		String wrappedJson = wrapAll(chatName, json);
		Misc.writeAllText(getJsonPath(), wrappedJson);
		
		this.xmlConfig = createXmlConfig(chatName);
		
		return createTelegramParser(getJsonPath(), chatName, xmlConfig);
	}
	
	public static TelegramParser createTelegramParser(Path jsonPath, String chatName) {
		String xmlConfig = createXmlConfig(jsonPath, chatName);
		return createTelegramParser(jsonPath, chatName, xmlConfig);
	}
	
	private static TelegramParser createTelegramParser(Path jsonPath, String chatName, String xmlConfig) {
		TelegramParser tp = new TelegramParser();
		try {
			tp.init(xmlConfig, new Global());
		} catch (ParserException e) {
			fail(e);
		}
		
		return tp;
	}
	
	public static void checkBaseMessage(IMessage msg, String sender, LocalDateTime dt) {
		assertNotNull(msg);
		assertEquals(sender, msg.getSender());
		assertEquals(dt, msg.getTimepoint());
	}
	
	private Path getJsonPath() {
		return tempDir.resolve("chats.json");
	}
	
	public static String createBaseMessage(String from, String message, LocalDateTime dt) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String dateStr = dt.format(dtf);
		
		String json = ""
				+ addKeyValue("id", 123, true)
				+ addKeyValue("type", "message")
				+ addKeyValue("date", dateStr)
				+ addKeyValue("from", from)
				+ addKeyValue("from_id", "user12345")
				+ addKeyValue("text", message);
		
		return json;
	}
	
	public static String createXmlConfig(Path jsonPath, String chatName) {
		String xml = ""
				+ "<parserconfiguration>\n"
				+ "<messagepath>" + jsonPath + "</messagepath>\n"
				+ "<chatname>" + chatName + "</chatname>\n"
				+ "</parserconfiguration>\n";
		
		return xml;
	}
	
	private String createXmlConfig(String chatName) {
		return createXmlConfig(getJsonPath(), chatName);
	}
	
	public static String wrapAll(String chatName, String messages) {
		return "{" + wrapChats(chatName, messages) + "}";
	}
	
	public static String wrapChats(String chatName, String messages) {
		String json = ""
				+ "\"chats\": {"
				+ "\"list\": ["
				+ wrapChat(chatName, messages)
				+ "]"
				+ "}";
				
		return json;
	}
	
	public static String wrapChat(String chatName, String messages) {
		String json = ""
				+ "{"
				+ addKeyValue("name", chatName, true)
				+ addKeyValue("type", "personal_chat")
				+ addKeyValue("id", 12345)
				+ wrapMessages(messages)
				+ "}";
		
		return json;
	}

	public static String wrapMessages(String messages) {
		String json = ""
				+ ",\n\"messages\": ["
				+ messages 
				+ "]";

		return json;
	}

	public static String addKeyValue(String key, String value, boolean first) {
		String comma = first ? "" : ",";
		return String.format("%s\n\"%s\": \"%s\"", comma, key, value);
	}
	
	public static String addKeyValue(String key, String value) {
		return addKeyValue(key, value, false);
	}
	
	public static String addKeyValue(String key, int value, boolean first) {
		String comma = first ? "" : ",";
		return String.format("%s\n\"%s\": %d", comma, key, value);
	}
	
	public static String addKeyValue(String key, int value) {
		return addKeyValue(key, value, false);
	}
	
	public static String addKeyValue(String key, double value, boolean first) {
		String comma = first ? "" : ",";
		return String.format("%s\n\"%s\": %f", comma, key, value);
	}
	
	public static String addKeyValue(String key, double value) {
		return addKeyValue(key, value, false);
	}
}
