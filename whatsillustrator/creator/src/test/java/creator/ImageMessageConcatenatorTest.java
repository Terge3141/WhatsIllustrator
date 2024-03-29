package creator;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import messageparser.IMessage;
import messageparser.ImageMessage;
import messageparser.ImageStackMessage;
import messageparser.LinkMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;
import messageparser.VideoMessage;

class ImageMessageConcatenatorTest {

	/*
	 * LinkMessage
	 */

	@Test
	void testAddMessage_TextMessage() {
		testSingleMessage(new TextMessage(LocalDateTime.of(2023, 11, 8, 21, 02, 00), "Terge", "Hi"));
	}
	
	@Test
	void testAddMessage_VideoMessage() {
		testSingleMessage(new VideoMessage(LocalDateTime.of(2023, 11, 8, 21, 02, 00), "Terge", Paths.get("."), "Hi"));
	}
	
	@Test
	void testAddMessage_MediaOmittedMessage() {
		List<Path> list = new ArrayList<Path>();
		testSingleMessage(new MediaOmittedMessage(LocalDateTime.of(2023, 11, 8, 21, 02, 00), "Terge", list, 1));
	}
	
	@Test
	void testAddMessage_MediaMessage() {
		testSingleMessage(new MediaMessage(LocalDateTime.of(2023, 11, 8, 21, 02, 00), "Terge", "filename", "subscription"));
	}
	
	@Test
	void testAddMessage_LinkMessage() {
		testSingleMessage(new LinkMessage(LocalDateTime.of(2023, 11, 8, 21, 02, 00), "Terge", "url"));
	}
	
	@Test
	// Scenario: Three image messages same date and a text message
	void testAddMessage_ImageMessage1() {
		ImageMessageConcatenator imc = new ImageMessageConcatenator();

		LocalDateTime tp = LocalDateTime.of(2023, 11, 8, 21, 02, 00);
		String sender = "Terge";
		String subscription = "sub";
		Path p1 = Paths.get("path1");
		Path p2 = Paths.get("path2");
		Path p3 = Paths.get("path3");
		
		ImageMessage im1 = new ImageMessage(tp, sender, p1, subscription);
		ImageMessage im2 = new ImageMessage(tp, sender, p2, subscription);
		ImageMessage im3 = new ImageMessage(tp, sender, p3, subscription);
		TextMessage tm = new TextMessage(LocalDateTime.of(2023, 11, 8, 21, 02, 00), "Terge", "Hi");
		
		assertEquals(0, imc.addMessage(im1).size());
		assertEquals(0, imc.addMessage(im2).size());
		assertEquals(0, imc.addMessage(im3).size());
		
		List<IMessage> list = imc.addMessage(tm);
		assertEquals(2, list.size());
		
		checkImageStackMessage(list.get(0), tp, sender, subscription, List.of(p1, p2, p3));
		
		// check that tm remains unchanged
		assertTrue(tm == list.get(1));
	}
	
	@Test
	// Scenario: Two image messages different sender
	void testAddMessage_ImageMessage2() {
		LocalDateTime tp = LocalDateTime.of(2023, 11, 8, 21, 02, 00);
		String subscription = "sub";
		Path p1 = Paths.get("path1");
		Path p2 = Paths.get("path2");
		
		ImageMessage im1 = new ImageMessage(tp, "Sender1", p1, subscription);
		ImageMessage im2 = new ImageMessage(tp, "Sender2", p2, subscription);
		
		testNonStackable(im1, im2);
	}
	
	@Test
	// Scenario: Two image messages different subscription
	void testAddMessage_ImageMessage3() {
		LocalDateTime tp = LocalDateTime.of(2023, 11, 8, 21, 02, 00);
		String sender = "Terge";
		Path p1 = Paths.get("path1");
		Path p2 = Paths.get("path2");
		
		ImageMessage im1 = new ImageMessage(tp, sender, p1, "sub1");
		ImageMessage im2 = new ImageMessage(tp, sender, p2, "sub2");
		
		testNonStackable(im1, im2);
	}
	
	@Test
	// Scenario: Two image messages different date
	void testAddMessage_ImageMessage4() {
		String sender = "Terge";
		String subscription = "sub";
		Path p1 = Paths.get("path1");
		Path p2 = Paths.get("path2");
		
		ImageMessage im1 = new ImageMessage(LocalDateTime.of(2023, 11, 8, 21, 02, 1), sender, p1, subscription);
		ImageMessage im2 = new ImageMessage(LocalDateTime.of(2023, 11, 8, 21, 02, 2), sender, p2, subscription);
		
		testNonStackable(im1, im2);
	}
	
	@Test
	// Scenario: Two image messages different sender
	void testAddMessage_ImageMessage5() {
		LocalDateTime tp = LocalDateTime.of(2023, 11, 8, 21, 02, 00);
		String subscription = "sub";
		Path p1 = Paths.get("path1");
		Path p2 = Paths.get("path2");
		
		ImageMessage im1 = new ImageMessage(tp, "Terge", p1, subscription);
		ImageMessage im2 = new ImageMessage(tp, "Biff", p2, subscription);
		
		testNonStackable(im1, im2);
	}
	
	@Test
	// Scenario: Two image messages same date, one image other date, text message
	void testAddMessage_ImageMessage6() {
		ImageMessageConcatenator imc = new ImageMessageConcatenator();
		
		LocalDateTime tp1 = LocalDateTime.of(2023, 11, 9, 22, 26, 0);
		LocalDateTime tp2 = LocalDateTime.of(2023, 11, 9, 22, 27, 0);
		String sender = "Terge";
		String subscription = "sub";
		
		Path p1 = Paths.get("path1");
		Path p2 = Paths.get("path2");
		Path p3 = Paths.get("path3");
		
		ImageMessage im1 = new ImageMessage(tp1, sender, p1, subscription);
		ImageMessage im2 = new ImageMessage(tp1, sender, p2, subscription);
		ImageMessage im3 = new ImageMessage(tp2, sender, p3, subscription);
		TextMessage tm = new TextMessage(tp2, sender, "Hi");
		
		assertEquals(0, imc.addMessage(im1).size());
		assertEquals(0, imc.addMessage(im2).size());
		
		List<IMessage> list1 = imc.addMessage(im3);
		assertEquals(1, list1.size());
		checkImageStackMessage(list1.get(0), tp1, sender, subscription, List.of(p1, p2));
		
		List<IMessage> list2 = imc.addMessage(tm);
		assertEquals(2, list2.size());
		assertTrue(im3 == list2.get(0));
		assertTrue(tm == list2.get(1));
	}
	
	@Test
	// Scenario: Three image messages datediff <= 60 and a text message
	void testAddMessage_ImageMessage7() {
		ImageMessageConcatenator imc = new ImageMessageConcatenator(60);

		LocalDateTime tp1 = LocalDateTime.of(2023, 11, 8, 21, 02, 0);
		LocalDateTime tp2 = LocalDateTime.of(2023, 11, 8, 21, 02, 20);
		LocalDateTime tp3 = LocalDateTime.of(2023, 11, 8, 21, 03, 0);
		String sender = "Terge";
		String subscription = "sub";
		Path p1 = Paths.get("path1");
		Path p2 = Paths.get("path2");
		Path p3 = Paths.get("path3");
		
		ImageMessage im1 = new ImageMessage(tp1, sender, p1, subscription);
		ImageMessage im2 = new ImageMessage(tp2, sender, p2, subscription);
		ImageMessage im3 = new ImageMessage(tp3, sender, p3, subscription);
		TextMessage tm = new TextMessage(LocalDateTime.of(2023, 11, 8, 21, 04, 00), "Terge", "Hi");
		
		assertEquals(0, imc.addMessage(im1).size());
		assertEquals(0, imc.addMessage(im2).size());
		assertEquals(0, imc.addMessage(im3).size());
		
		List<IMessage> list = imc.addMessage(tm);
		assertEquals(2, list.size());
		
		checkImageStackMessage(list.get(0), tp1, sender, subscription, List.of(p1, p2, p3));
		
		// check that tm remains unchanged
		assertTrue(tm == list.get(1));
	}
	
	@Test
	// Scenario: Two image messages datediff <= 60, one image message later, and a text message
	void testAddMessage_ImageMessage8() {
		ImageMessageConcatenator imc = new ImageMessageConcatenator(60);

		LocalDateTime tp1 = LocalDateTime.of(2023, 11, 8, 21, 02, 0);
		LocalDateTime tp2 = LocalDateTime.of(2023, 11, 8, 21, 02, 20);
		LocalDateTime tp3 = LocalDateTime.of(2023, 11, 8, 21, 03, 20);
		String sender = "Terge";
		String subscription = "sub";
		Path p1 = Paths.get("path1");
		Path p2 = Paths.get("path2");
		Path p3 = Paths.get("path3");
		
		ImageMessage im1 = new ImageMessage(tp1, sender, p1, subscription);
		ImageMessage im2 = new ImageMessage(tp2, sender, p2, subscription);
		ImageMessage im3 = new ImageMessage(tp3, sender, p3, subscription);
		TextMessage tm = new TextMessage(LocalDateTime.of(2023, 11, 8, 21, 04, 00), "Terge", "Hi");
		
		assertEquals(0, imc.addMessage(im1).size());
		assertEquals(0, imc.addMessage(im2).size());
		
		List<IMessage> list1 = imc.addMessage(im3);
		assertEquals(1, list1.size());
		checkImageStackMessage(list1.get(0), tp1, sender, subscription, List.of(p1, p2));
		
		List<IMessage> list2 = imc.addMessage(tm);
		assertEquals(2, list2.size());
		assertTrue(im3 == list2.get(0));
		assertTrue(tm == list2.get(1));
	}
	
	@Test
	void testFlush_OneImageMessage() {
		ImageMessageConcatenator imc = new ImageMessageConcatenator();
		ImageMessage im = new ImageMessage(LocalDateTime.of(2023, 11, 9, 22, 44, 0), "Terge", Paths.get("path"), "sub");
		
		imc.addMessage(im);
		
		List<IMessage> list = imc.flush();
		assertEquals(1, list.size());
		assertTrue(im == list.get(0));
	}
	
	@Test
	void testFlush_TwoImageMessages() {
		ImageMessageConcatenator imc = new ImageMessageConcatenator();
		
		LocalDateTime tp = LocalDateTime.of(2023, 11, 9, 22, 58, 0);
		String sender = "Terge";
		Path p1 = Paths.get("path1");
		Path p2 = Paths.get("path2");
		String subscription = "sub";
		
		ImageMessage im1 = new ImageMessage(tp, sender, p1, subscription);
		ImageMessage im2 = new ImageMessage(tp, sender, p2, subscription);
		
		imc.addMessage(im1);
		imc.addMessage(im2);
		
		List<IMessage> list = imc.flush();
		assertEquals(1, list.size());
		checkImageStackMessage(list.get(0), tp, sender, subscription, List.of(p1, p2));
	}
	
	void testSingleMessage(IMessage msg) {
		ImageMessageConcatenator imc = new ImageMessageConcatenator();
		List<IMessage> list = imc.addMessage(msg);

		assertEquals(1, list.size());
		assertTrue(msg == list.get(0));
	}
	
	void testNonStackable(ImageMessage im1, ImageMessage im2) {
		ImageMessageConcatenator imc = new ImageMessageConcatenator();
		
		TextMessage tm = new TextMessage(LocalDateTime.of(2023, 11, 8, 21, 02, 00), "Terge", "Hi");
		
		assertEquals(0, imc.addMessage(im1).size());
		
		List<IMessage> list1 = imc.addMessage(im2);
		assertEquals(1, list1.size());
		assertTrue(im1 == list1.get(0));
		
		List<IMessage> list2 = imc.addMessage(tm);
		assertEquals(2, list2.size());
		assertTrue(im2 == list2.get(0));
		assertTrue(tm == list2.get(1));
	}
	
	void checkImageStackMessage(IMessage msg, LocalDateTime tp, String sender, String subscription, List<Path> paths) {
		assertTrue(msg instanceof ImageStackMessage);
		ImageStackMessage ism = (ImageStackMessage)msg;
		
		assertEquals(tp, ism.getTimepoint());
		assertEquals(sender, ism.getSender());
		assertEquals(subscription, ism.getSubscription());
		
		assertEquals(paths.size(), ism.getFilepaths().size());
		for(int i=0; i<paths.size(); i++) {
			assertEquals(ism.getFilepaths().get(i), paths.get(i));
		}
	}
}
