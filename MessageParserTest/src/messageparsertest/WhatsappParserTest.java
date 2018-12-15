package messageparsertest;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import imagematcher.FileEntry;
import imagematcher.ImageMatcher;
import imagematcher.MatchEntry;
import messageparser.IMessage;
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.TextMessage;
import messageparser.WhatsappParser;

class WhatsappParserTest {

	@Test
	void testNextMessage_InvalidHeader() {
		List<String> lines = Arrays.asList("Something different");
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher());
		try {
			wp.nextMessage();
			fail("No exception thrown");
		} catch (IllegalArgumentException iae) {
		}
	}

	@Test
	void testNextMessage_TextMessage() {
		List<String> lines = Arrays.asList("21/10/2015, 16:29 - biff: Hmm");
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher());

		IMessage msg = wp.nextMessage();
		assertTrue(msg instanceof TextMessage);

		TextMessage tm = (TextMessage) msg;
		assertEquals("biff", tm.getSender());
		assertEquals(LocalDateTime.of(2015, 10, 21, 16, 29), tm.getTimepoint());
		assertEquals("Hmm", tm.getContent());

		assertNull(wp.nextMessage());
	}

	@Test
	void testNextMessage_TextMessage_SeveralLines() {
		List<String> lines = Arrays.asList("21/10/2015, 16:29 - biff: Hmm", "a flying delorean", "Can't be true");
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher());

		IMessage msg = wp.nextMessage();
		assertTrue(msg instanceof TextMessage);

		TextMessage tm = (TextMessage) msg;
		assertEquals("biff", tm.getSender());
		assertEquals(LocalDateTime.of(2015, 10, 21, 16, 29), tm.getTimepoint());
		assertEquals("Hmm\na flying delorean\nCan't be true", tm.getContent());

		assertNull(wp.nextMessage());
	}

	@Test
	void testNextMessage_ImageMessage_Nosubscription() {
		List<String> lines = Arrays.asList("21/10/2015, 16:29 - biff: delorean.jpg (file attached)");
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher());

		IMessage msg = wp.nextMessage();
		assertTrue(msg instanceof ImageMessage);

		ImageMessage im = (ImageMessage) msg;
		assertEquals("biff", im.getSender());
		assertEquals(LocalDateTime.of(2015, 10, 21, 16, 29), im.getTimepoint());
		assertEquals("delorean.jpg", im.getFilename());
		assertEquals("", im.getSubscription());

		assertNull(wp.nextMessage());
	}

	@Test
	void testNextMessage_ImageMessage_Subscription() {
		List<String> lines = Arrays.asList("21/10/2015, 16:29 - biff: delorean.jpg (file attached)", "Flying delorean");
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher());

		IMessage msg = wp.nextMessage();
		assertTrue(msg instanceof ImageMessage);

		ImageMessage im = (ImageMessage) msg;
		assertEquals("biff", im.getSender());
		assertEquals(LocalDateTime.of(2015, 10, 21, 16, 29), im.getTimepoint());
		assertEquals("delorean.jpg", im.getFilename());
		assertEquals("Flying delorean", im.getSubscription());

		assertNull(wp.nextMessage());
	}

	@Test
	void testNextMessage_MediaMessage() {
		List<String> lines = Arrays.asList("21/10/2015, 16:29 - biff: mcfly.vcf (file attached)");
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher());

		IMessage msg = wp.nextMessage();
		assertTrue(msg instanceof MediaMessage);

		MediaMessage mm = (MediaMessage) msg;
		assertEquals("biff", mm.getSender());
		assertEquals(LocalDateTime.of(2015, 10, 21, 16, 29), mm.getTimepoint());
		assertEquals("mcfly.vcf", mm.getFilename());
		assertEquals("", mm.getSubscription());

		assertNull(wp.nextMessage());
	}

	/*@Test
	void testNextMessage_MediaOmittedMessage() {
		ImageMatcher im = new ImageMatcher();
		
		FileEntry f1 = new FileEntry();
		f1.setFileName("delorean1.jpg");
		f1.setTimePoint(LocalDateTime.of(2015, 10, 21, 0, 0));
		f1.setRelPath("path/to/img");
		
		File
		
		MatchEntry me = new MatchEntry(LocalDateTime.of(2015, 10, 21, 16, 29), fileMatches, 0);
		im.setMatchList(matchList);
	}*/
}
