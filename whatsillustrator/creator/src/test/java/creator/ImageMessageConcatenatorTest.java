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
	// Scenario: Two image messages same date and a text message
	void testAddMessage_ImageMessage1() {
		ImageMessageConcatenator imc = new ImageMessageConcatenator();

		LocalDateTime tp = LocalDateTime.of(2023, 11, 8, 21, 02, 00);
		String sender = "Terge";
		String subscription = "sub";
		Path p1 = Paths.get("path1");
		Path p2 = Paths.get("path2");
		
		ImageMessage im1 = new ImageMessage(tp, sender, p1, subscription);
		ImageMessage im2 = new ImageMessage(tp, sender, p2, subscription);
		TextMessage tm = new TextMessage(LocalDateTime.of(2023, 11, 8, 21, 02, 00), "Terge", "Hi");
		
		assertEquals(0, imc.addMessage(im1).size());
		assertEquals(0, imc.addMessage(im2).size());
		
		List<IMessage> list = imc.addMessage(tm);
		assertEquals(2, list.size());
		
		// check that im1 and im 2 become stackmessage
		assertTrue(list.get(0) instanceof ImageStackMessage);
		ImageStackMessage ism = (ImageStackMessage)list.get(0);
		
		assertEquals(tp, ism.getTimepoint());
		assertEquals(sender, ism.getSender());
		assertEquals(subscription, ism.getSubscription());
		
		assertEquals(2, ism.getFilepaths().size());
		assertEquals(ism.getFilepaths().get(0), p1);
		assertEquals(ism.getFilepaths().get(1), p2);
		
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
	// Scenario: Two image messages same date, one image other date, text message
	void testAddMessage_ImageMessage5() {
		fail("Not implemented");
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
}
