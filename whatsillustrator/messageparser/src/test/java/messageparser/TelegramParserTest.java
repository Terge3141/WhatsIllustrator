package messageparser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
		TelegramMessageMocker.checkBaseMessage(msg, "Terge", dt1);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(tm.getContent(), "This is message1");
		
		msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "Biff", dt2);
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
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Mychat");
		
		IMessage msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "From", dt);
		
		assertTrue(msg instanceof ImageMessage);
		ImageMessage im = (ImageMessage)msg;
		Path expFilePath = tmpDir.resolve("chats/bla.jpg").toAbsolutePath();
		assertEquals(expFilePath, im.getFilepath());
		assertEquals("", im.getSubscription());
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testNextMessageVideoFile(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt = LocalDateTime.of(2023, 1, 21, 19, 3, 56);
		
		Path videoPath = tmpDir.resolve("chats/video.mp4");
		
		String jsonMessage = TelegramMessageMocker.createBaseMessage("From", "Mymessage", dt)
				+ TelegramMessageMocker.addKeyValue("media_type", "video_file")
				+ TelegramMessageMocker.addKeyValue("mime_type", "video/mp4")
				+ TelegramMessageMocker.addKeyValue("file", videoPath.toString());
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Mychat");
		
		IMessage msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "From", dt);
		
		assertTrue(msg instanceof VideoMessage);
		VideoMessage vm = (VideoMessage)msg;
		
		assertEquals("Mymessage", vm.getSubscription());
		assertEquals(videoPath, vm.getFilepath());
	}
	
	@Test
	void testNextMessageAnimation(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt = LocalDateTime.of(2023, 1, 21, 19, 13, 57);
		
		Path videoPath = tmpDir.resolve("chats/video.mp4");
		
		String jsonMessage = TelegramMessageMocker.createBaseMessage("From", "Mymessage", dt)
				+ TelegramMessageMocker.addKeyValue("media_type", "animation")
				+ TelegramMessageMocker.addKeyValue("mime_type", "video/mp4")
				+ TelegramMessageMocker.addKeyValue("file", videoPath.toString());
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Mychat");
		
		IMessage msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "From", dt);
		
		assertTrue(msg instanceof VideoMessage);
		VideoMessage vm = (VideoMessage)msg;
		
		assertEquals(videoPath, vm.getFilepath());
		
		assertEquals("Mymessage", vm.getSubscription());
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testNextMessageVoice(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt = LocalDateTime.of(2023, 1, 21, 19, 13, 57);
		
		Path audioFile = tmpDir.resolve("chats/audio.ogg");
		
		String jsonMessage = TelegramMessageMocker.createBaseMessage("From",  "", dt)
				+ TelegramMessageMocker.addKeyValue("file", audioFile.toString())
				+ TelegramMessageMocker.addKeyValue("media_type", "voice_message")
				+ TelegramMessageMocker.addKeyValue("mime_type", "audio/ogg")
				+ TelegramMessageMocker.addKeyValue("duration_seconds", 34);
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Mychat");
		
		IMessage msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "From", dt);
		
		assertTrue(msg instanceof TextMessage);
		TextMessage tm = (TextMessage)msg;
		
		assertEquals("voice message of 34s", tm.getContent());
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testNextMessageAudio(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt = LocalDateTime.of(2023, 1, 21, 19, 13, 57);
		
		Path audioFile = tmpDir.resolve("chats/audio.ogg");
		
		String jsonMessage = TelegramMessageMocker.createBaseMessage("From",  "", dt)
				+ TelegramMessageMocker.addKeyValue("file", audioFile.toString())
				+ TelegramMessageMocker.addKeyValue("media_type", "audio_file")
				+ TelegramMessageMocker.addKeyValue("mime_type", "audio/ogg")
				+ TelegramMessageMocker.addKeyValue("duration_seconds", 123);
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Mychat");
		
		IMessage msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "From", dt);
		
		assertTrue(msg instanceof TextMessage);
		TextMessage tm = (TextMessage)msg;
		
		assertEquals("audio file of 123s", tm.getContent());
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testNextMessageVideoMessage(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt = LocalDateTime.of(2023, 1, 21, 19, 47, 32);
		
		Path videoDir = tmpDir.resolve("chats");
		String videoFilename = "video.mp4";
		String thumbFilename = videoFilename + "_thumb.jpg";
		Path thumbPath = videoDir.resolve(thumbFilename);
		
		String jsonMessage = TelegramMessageMocker.createBaseMessage("From", "Mymessage", dt)
				+ TelegramMessageMocker.addKeyValue("file", videoDir.resolve(videoFilename).toString())
				+ TelegramMessageMocker.addKeyValue("thumbnail", thumbPath.toString())
				+ TelegramMessageMocker.addKeyValue("media_type", "video_message")
				+ TelegramMessageMocker.addKeyValue("mime_type", "video/mp4");
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Mychat");
		
		IMessage msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "From", dt);
		assertTrue(msg instanceof ImageMessage);
		ImageMessage im = (ImageMessage)msg;
		
		assertEquals("Mymessage", im.getSubscription());
		assertEquals(thumbPath, im.getFilepath());
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testNextMessagePDF(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt = LocalDateTime.of(2023, 1, 21, 19, 47, 32);
		
		String jsonMessage = TelegramMessageMocker.createBaseMessage("From", "Mymessage", dt)
				+ TelegramMessageMocker.addKeyValue("file", "chats/doc.pdf")
				+ TelegramMessageMocker.addKeyValue("mime_type", "application/pdf");
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Mychat");
		
		IMessage msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "From", dt);
		assertTrue(msg instanceof TextMessage);
		TextMessage tm = (TextMessage)msg;
		
		assertEquals("Mymessage (attached PDF document)", tm.getContent());
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testNextMessageLocation(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt = LocalDateTime.of(2007, 10, 1, 9, 0, 0);
		
		String jsonMessage = TelegramMessageMocker.createBaseMessage("developer", "", dt)
				+ ",\n\"location_information\": {"
				+ TelegramMessageMocker.addKeyValue("latitude", 37.782979, true)
				+ TelegramMessageMocker.addKeyValue("longitude", -122.390864)
				+ "}";
		
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addMessage(jsonMessage);
		
		TelegramParser tp = tmm.createTelegramParser("Mychat");
		
		IMessage msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "developer", dt);
		
		assertTrue(msg instanceof TextMessage);
		TextMessage tm = (TextMessage)msg;
		
		assertEquals("Latitude: 37.782979\nLongitude: -122.390864\n", tm.getContent());
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testMinDate(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt1 = LocalDateTime.of(2023, 11, 6, 20, 00, 00);
		LocalDateTime dt2 = LocalDateTime.of(2023, 11, 6, 20, 01, 00);
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addTextMessage("Terge", "This is message1", dt1);
		tmm.addTextMessage("Biff", "This is message2", dt2);
		
		TelegramParser tp = tmm.createTelegramParser("Tergechat", LocalDateTime.of(2023, 11, 6, 20, 00, 30), null);
		
		IMessage msg = null;
		TextMessage tm = null;
		
		msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "Biff", dt2);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(tm.getContent(), "This is message2");
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testMaxDate(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt1 = LocalDateTime.of(2023, 11, 6, 20, 00, 00);
		LocalDateTime dt2 = LocalDateTime.of(2023, 11, 6, 20, 01, 00);
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addTextMessage("Terge", "This is message1", dt1);
		tmm.addTextMessage("Biff", "This is message2", dt2);
		
		TelegramParser tp = tmm.createTelegramParser("Tergechat", null, LocalDateTime.of(2023, 11, 6, 20, 00, 30));
		
		IMessage msg = null;
		TextMessage tm = null;
		
		msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "Terge", dt1);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(tm.getContent(), "This is message1");
		
		assertNull(tp.nextMessage());
	}
	
	@Test
	void testMinMaxDate(@TempDir Path tmpDir) throws IOException {
		LocalDateTime dt1 = LocalDateTime.of(2023, 11, 6, 20, 00, 00);
		LocalDateTime dt2 = LocalDateTime.of(2023, 11, 6, 20, 01, 00);
		LocalDateTime dt3 = LocalDateTime.of(2023, 11, 6, 20, 02, 00);
		TelegramMessageMocker tmm = new TelegramMessageMocker(tmpDir);
		tmm.addTextMessage("Terge", "This is message1", dt1);
		tmm.addTextMessage("Biff", "This is message2", dt2);
		tmm.addTextMessage("Terge", "This is message3", dt3);
		
		TelegramParser tp = tmm.createTelegramParser("Tergechat", LocalDateTime.of(2023, 11, 6, 20, 00, 30), LocalDateTime.of(2023, 11, 6, 20, 01, 30));
		
		IMessage msg = null;
		TextMessage tm = null;
		
		msg = tp.nextMessage();
		TelegramMessageMocker.checkBaseMessage(msg, "Biff", dt2);
		assertTrue(msg instanceof TextMessage);
		tm = (TextMessage)msg;
		assertEquals(tm.getContent(), "This is message2");
		
		assertNull(tp.nextMessage());
	}
}