package messageparser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import configurator.Global;
import helper.Misc;

class WhatsappParserTest {
	@Test
	void testNextMessageTexts(@TempDir Path tmpDir) throws IOException {
		String msgTxt = "21/10/2015, 10:11 - myself: Hi Marty, where are you?\n"
				+ "21/10/2015, 10:12 - myself: Hello? Waiting for you!\n"
				+ "21/10/2015, 10:14 - Marty: Be right with you!\n";
		
		Path chatDir = createChatDir(tmpDir);
		createChatMessage(chatDir, msgTxt);
		
		String xmlConfig = getXmlConfig(tmpDir);
		WhatsappParser wap = new WhatsappParser();
		try {
			wap.init(xmlConfig, new Global());
		} catch (ParserException e) {
			fail(e);
		}
		
		IMessage msg = null;
		TextMessage tm = null;
		
		msg = wap.nextMessage();
		assertNotNull(msg);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(LocalDateTime.of(2015, 10, 21, 10, 11), tm.getTimepoint());
		assertEquals("myself", tm.getSender());
		assertEquals("Hi Marty, where are you?", tm.getContent());
		
		msg = wap.nextMessage();
		assertNotNull(msg);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(LocalDateTime.of(2015, 10, 21, 10, 12), tm.getTimepoint());
		assertEquals("myself", tm.getSender());
		assertEquals("Hello? Waiting for you!", tm.getContent());
		
		msg = wap.nextMessage();
		assertNotNull(msg);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(LocalDateTime.of(2015, 10, 21, 10, 14), tm.getTimepoint());
		assertEquals("Marty", tm.getSender());
		assertEquals("Be right with you!", tm.getContent());
		
		assertNull(wap.nextMessage());
	}
	
	@Test
	void testNextMessageImageSubscription(@TempDir Path tmpDir) throws IOException {
		String msgTxt = "21/10/2015, 10:11 - myself: IMG-20151021-WA0000.jpg (file attached)\n"
				+ "This is the subscripton text!\n";
		Path chatDir = createChatDir(tmpDir);
		createChatMessage(chatDir, msgTxt);
		
		Path imgFile = chatDir.resolve("IMG-20151021-WA0000.jpg");
		imgFile.toFile().createNewFile();
		
		String xmlConfig = getXmlConfig(tmpDir);
		WhatsappParser wap = new WhatsappParser();
		try {
			wap.init(xmlConfig, new Global());
		} catch (ParserException e) {
			fail(e);
		}
		
		IMessage msg = wap.nextMessage();
		assertNotNull(msg);
		assertTrue(msg instanceof ImageMessage);
		
		ImageMessage im = (ImageMessage)msg;
		assertEquals(chatDir.resolve("IMG-20151021-WA0000.jpg"), im.getFilepath());
		assertEquals("This is the subscripton text!", im.getSubscription());

		assertNull(wap.nextMessage());
	}
	
	@Test
	void testNextMessageImageNoSubscription(@TempDir Path tmpDir) throws IOException {
		String msgTxt = "21/10/2015, 10:11 - myself: IMG-20151021-WA0000.jpg (file attached)\n";
		Path chatDir = createChatDir(tmpDir);
		createChatMessage(chatDir, msgTxt);
		
		Path imgFile = chatDir.resolve("IMG-20151021-WA0000.jpg");
		imgFile.toFile().createNewFile();
		
		String xmlConfig = getXmlConfig(tmpDir);
		WhatsappParser wap = new WhatsappParser();
		try {
			wap.init(xmlConfig, new Global());
		} catch (ParserException e) {
			fail(e);
		}
		
		IMessage msg = wap.nextMessage();
		assertNotNull(msg);
		assertTrue(msg instanceof ImageMessage);
		
		ImageMessage im = (ImageMessage)msg;
		assertEquals(chatDir.resolve("IMG-20151021-WA0000.jpg"), im.getFilepath());
		assertEquals("", im.getSubscription());

		assertNull(wap.nextMessage());
	}
	
	@Test
	void testNextMessageNameLookup(@TempDir Path tmpDir) throws IOException {
		String xmlNameLookup = "";
		String tmp = ""
				+ xmlWrap("oldName", "myself")
				+ xmlWrap("newName", "Terge");
		xmlNameLookup += xmlWrap("ReplaceItem", tmp);
		xmlNameLookup = xmlWrap("NameLookup", xmlNameLookup);
		
		String msgTxt = "21/10/2015, 10:11 - myself: My message!";
		
		Path chatDir = createChatDir(tmpDir);
		createChatMessage(chatDir, msgTxt);
		Path configDir = createConfigDir(tmpDir);
		Files.writeString(configDir.resolve("namelookup.xml"), xmlNameLookup);
		
		String xmlConfig = getXmlConfig(tmpDir);
		WhatsappParser wap = new WhatsappParser();
		try {
			wap.init(xmlConfig, new Global());
		} catch (ParserException e) {
			fail(e);
		}
		
		IMessage msg = wap.nextMessage();
		assertNotNull(msg);
		assertTrue(msg instanceof TextMessage);
		TextMessage tm = (TextMessage)msg;
		
		assertEquals("Terge", tm.getSender());
	}
	
	@Test
	void testGetNameSuggestion1(@TempDir Path tmpDir) throws IOException {
		Path chatDir = createChatDir(tmpDir);
		Path chatFile = chatDir.resolve("WhatsApp Mytest.txt");
		chatFile.toFile().createNewFile();
		
		String xmlConfig = getXmlConfig(tmpDir);
		WhatsappParser wap = new WhatsappParser();
		try {
			wap.init(xmlConfig, new Global());
		} catch (ParserException e) {
			fail(e);
		}
		
		assertEquals("WhatsApp Mytest", wap.getNameSuggestion());
	}
	
	@Test
	void testGetNameSuggestion2(@TempDir Path tmpDir) throws IOException {
		Path chatDir = createChatDir(tmpDir);
		Path chatFile = chatDir.resolve("Mytest.txt");
		chatFile.toFile().createNewFile();
		
		String xmlConfig = getXmlConfig(tmpDir);
		WhatsappParser wap = new WhatsappParser();
		try {
			wap.init(xmlConfig, new Global());
		} catch (ParserException e) {
			fail(e);
		}
		
		assertEquals("Whatsapp_Mytest", wap.getNameSuggestion());
	}

	private Path createChatMessage(Path chatDir, String msg) throws IOException {
		Path chatFile = chatDir.resolve("WhatsApp Chat with Terge.txt");
		Files.writeString(chatFile, msg);
		
		return chatFile;
	}
	
	private Path createChatDir(Path tmpDir) throws IOException {
		Path chatDir = tmpDir.resolve("chat");
		Files.createDirectories(chatDir);
		
		return chatDir;
	}
	
	private Path createConfigDir(Path tmpDir) throws IOException {
		Path configDir = tmpDir.resolve("config");
		Files.createDirectories(configDir);
		
		return configDir;
	}
	
	private String getXmlConfig(Path tmpDir) {
		String xml = ""
				+ "<parserconfiguration>\n"
				+ "<messagedir>" + tmpDir + "</messagedir>\n"
				//+ "                        <imagepooldir>Path to all whatsapp images (used for ImageMatcher)\n"
				//+ "                        </imagepooldir>\n"
				+ "</parserconfiguration>\n";
		return xml;
	}
	
	private String xmlWrap(String tag, String value) {
		return String.format("<%s>%s</%s>", tag, value, tag);
	}
}
