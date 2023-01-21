package messageparser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
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
		LocalDateTime dt1 = LocalDateTime.of(2023, 1, 15, 19, 27, 12);
		LocalDateTime dt2 = LocalDateTime.of(2023, 1, 15, 19, 27, 23);
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addTextMessage("Terge", "This is message1", dt1);
		tmm.addTextMessage("Biff", "This is message2", dt2);
		
		TelegramParser tp = tmm.createTelegramParser("Tergechat");
		
		IMessage msg = null;
		TextMessage tm = null;
		
		msg = tp.nextMessage();
		tmm.checkBaseMessage(msg, "Terge", dt1);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(tm.getContent(), "This is message1");
		
		msg = tp.nextMessage();
		tmm.checkBaseMessage(msg, "Biff", dt2);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(tm.getContent(), "This is message2");
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testNextMessageMultipleChats(@TempDir Path tmpDir) throws IOException {
		Path jsonPath = tmpDir.resolve("chats.json");
		
		LocalDateTime dt1 = LocalDateTime.of(2023, 1, 15, 23, 23, 23);
		String msgChat1 = "{" + TelegramMessageMocker.createBaseMessage("from1", "message1", dt1) + "}";
		
		LocalDateTime dt2 = LocalDateTime.of(2023, 1, 15, 23, 24, 23);
		String msgChat2 = "{" + TelegramMessageMocker.createBaseMessage("from2", "message2", dt2) + "}";
		
		String chat1 = TelegramMessageMocker.wrapChat("chat1", msgChat1);
		String chat2 = TelegramMessageMocker.wrapChat("chat2", msgChat2);
		
		String json = ""
				+ "\"chats\": {"
				+ "\"list\": ["
				+ chat1
				+ ", " + chat2
				+ "]"
				+ "}";
		
		json = "{" + json + "}";
		
		Misc.writeAllText(jsonPath, json);
		
		TelegramParser tp = TelegramMessageMocker.createTelegramParser(jsonPath, "chat2");
		
		IMessage msg = tp.nextMessage();
		assertNotNull(msg);
		assertEquals("from2", msg.getSender());
	}
	
	@Test
	void testNextMessagePhoto(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt = LocalDateTime.of(2023, 1, 16, 23, 8, 1);
		Path photoPath = tmpDir.resolve("chats/bla.jpg");
		
		String jsonMessage = TelegramMessageMocker.createBaseMessage("From", "subscription", dt)
			+ TelegramMessageMocker.addKeyValue("photo", photoPath.toString());
		jsonMessage = "{" + jsonMessage + "}";
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Tergechat");
		
		IMessage msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "From", dt);
		
		assertTrue(msg instanceof ImageMessage);
		ImageMessage im = (ImageMessage)msg;

		Path expFilePath = tmpDir.resolve("chats/bla.jpg").toAbsolutePath();
		assertEquals(expFilePath, im.getFilepath());
		assertEquals("subscription", im.getSubscription());
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testNextMessageSticker(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt = LocalDateTime.of(2023, 1, 16, 23, 8, 1);
		String emoji = new String(Character.toChars(0x1F601));
		
		String jsonMessage = TelegramMessageMocker.createBaseMessage("From", "", dt)
				+ TelegramMessageMocker.addKeyValue("file", "chats/bla.webp")
				+ TelegramMessageMocker.addKeyValue("thumbnail", "chats/bla_thumb.webp.jpg")
				+ TelegramMessageMocker.addKeyValue("media_type", "sticker")
				+ TelegramMessageMocker.addKeyValue("sticker_emoji", emoji);
		jsonMessage = "{" + jsonMessage + "}";
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Mychat");
		
		IMessage msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "From", dt);

		assertTrue(msg instanceof TextMessage);
		TextMessage tm = (TextMessage)msg;

		assertEquals(emoji, tm.getContent());
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testNextMessageJpeg(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt = LocalDateTime.of(2023, 1, 20, 22, 40, 12);
		
		String jsonMessage = TelegramMessageMocker.createBaseMessage("From", "", dt)
				+ TelegramMessageMocker.addKeyValue("file", "chats/bla.jpg")
				+ TelegramMessageMocker.addKeyValue("thumbnail", "chats/bla.jpg_thumb.jpg")
				+ TelegramMessageMocker.addKeyValue("mime_type", "image/jpeg");
		jsonMessage = "{" + jsonMessage + "}";
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Mychat");
		
		IMessage msg = tp.nextMessage();
		tmm.checkBaseMessage(msg, "From", dt);
		
		assertTrue(msg instanceof ImageMessage);
		ImageMessage im = (ImageMessage)msg;
		Path expFilePath = tmpDir.resolve("chats/bla.jpg").toAbsolutePath();
		assertEquals(expFilePath, im.getFilepath());
		assertEquals("", im.getSubscription());
		
		assertNull(tp.nextMessage());
	}
}
