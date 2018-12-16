package messageparsertest;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
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
import messageparser.MediaOmittedMessage;
import messageparser.NameLookup;
import messageparser.TextMessage;
import messageparser.WhatsappParser;

class WhatsappParserTest {

	@Test
	void testNextMessage_InvalidHeader() {
		List<String> lines = Arrays.asList("Something different");
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher(), new NameLookup());
		try {
			wp.nextMessage();
			fail("No exception thrown");
		} catch (IllegalArgumentException iae) {
		}
	}

	@Test
	void testNextMessage_SeveralMessages() {
		List<String> lines = Arrays.asList("21/10/2015, 16:29 - biff: Hmm", "21/10/2015, 16:30 - biffer: Oh",
				"21/10/2015, 16:30 - biff: Delorean?");
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher(), new NameLookup());

		IMessage msg1 = wp.nextMessage();
		IMessage msg2 = wp.nextMessage();
		IMessage msg3 = wp.nextMessage();

		assertNotNull(msg1);
		assertNotNull(msg2);
		assertNotNull(msg3);

		assertTrue(msg1 instanceof TextMessage);
		assertTrue(msg2 instanceof TextMessage);
		assertTrue(msg3 instanceof TextMessage);

		TextMessage tm1 = (TextMessage) msg1;
		TextMessage tm2 = (TextMessage) msg2;
		TextMessage tm3 = (TextMessage) msg3;

		assertEquals("biff", tm1.getSender());
		assertEquals("biffer", tm2.getSender());
		assertEquals("biff", tm3.getSender());

		assertEquals(LocalDateTime.of(2015, 10, 21, 16, 29), tm1.getTimepoint());
		assertEquals(LocalDateTime.of(2015, 10, 21, 16, 30), tm2.getTimepoint());
		assertEquals(LocalDateTime.of(2015, 10, 21, 16, 30), tm3.getTimepoint());

		assertEquals("Hmm", tm1.getContent());
		assertEquals("Oh", tm2.getContent());
		assertEquals("Delorean?", tm3.getContent());

		assertNull(wp.nextMessage());
	}

	@Test
	void testNextMessage_TextMessage_LookUpSender() {
		List<String> lines = Arrays.asList("21/10/2015, 16:29 - biff: Hmm", "21/10/2015, 16:29 - buff: Hmm");

		NameLookup nl = new NameLookup();
		nl.add("biff", "baff");
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher(), nl);
		
		IMessage msg1 = wp.nextMessage();
		IMessage msg2 = wp.nextMessage();

		assertNotNull(msg1);
		assertNotNull(msg2);

		assertTrue(msg1 instanceof TextMessage);
		assertTrue(msg2 instanceof TextMessage);

		TextMessage tm1 = (TextMessage) msg1;
		TextMessage tm2 = (TextMessage) msg2;

		assertEquals("baff", tm1.getSender());
		assertEquals("buff", tm2.getSender());
		
		assertNull(wp.nextMessage());

	}

	@Test
	void testNextMessage_TextMessage() {
		List<String> lines = Arrays.asList("21/10/2015, 16:29 - biff: Hmm");
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher(), new NameLookup());

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
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher(), new NameLookup());

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
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher(), new NameLookup());

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
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher(), new NameLookup());

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
		WhatsappParser wp = new WhatsappParser(lines, new ImageMatcher(), new NameLookup());

		IMessage msg = wp.nextMessage();
		assertTrue(msg instanceof MediaMessage);

		MediaMessage mm = (MediaMessage) msg;
		assertEquals("biff", mm.getSender());
		assertEquals(LocalDateTime.of(2015, 10, 21, 16, 29), mm.getTimepoint());
		assertEquals("mcfly.vcf", mm.getFilename());
		assertEquals("", mm.getSubscription());

		assertNull(wp.nextMessage());
	}

	@Test
	void testNextMessage_MediaOmittedMessage() {
		ImageMatcher im = new ImageMatcher();

		FileEntry f1 = getFileEntry("delorean1.jpg", 2015, 10, 21);
		FileEntry f2 = getFileEntry("delorean2.jpg", 2015, 10, 21);
		FileEntry f3 = getFileEntry("delorean3.jpg", 2015, 10, 22);

		MatchEntry me1 = new MatchEntry(LocalDateTime.of(2015, 10, 21, 16, 29), Arrays.asList(f1, f2), 0);
		MatchEntry me2 = new MatchEntry(LocalDateTime.of(2015, 10, 22, 17, 10), Arrays.asList(f3), 0);

		List<MatchEntry> matchList = Arrays.asList(me1, me2);
		im.setMatchList(matchList);
		im.setSearchMode(true);

		List<String> lines = Arrays.asList("21/10/2015, 16:29 - biff: <Media omitted>");
		WhatsappParser wp = new WhatsappParser(lines, im, new NameLookup());

		IMessage msg = wp.nextMessage();
		assertTrue(msg instanceof MediaOmittedMessage);

		MediaOmittedMessage mom = (MediaOmittedMessage) msg;
		assertEquals("biff", mom.getSender());
		assertEquals(LocalDateTime.of(2015, 10, 21, 16, 29), mom.getTimepoint());

		List<String> relPaths = mom.getRelpaths();
		assertEquals(2, relPaths.size());
		assertEquals("path/to/img/delorean1.jpg", relPaths.get(0));
		assertEquals("path/to/img/delorean2.jpg", relPaths.get(1));
	}

	private FileEntry getFileEntry(String name, int year, int month, int day) {
		FileEntry f1 = new FileEntry();
		f1.setFileName(name);
		f1.setTimePoint(LocalDate.of(year, month, day));
		f1.setRelPath("path/to/img/" + name);
		return f1;
	}
}
