package emojicontainer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class EmojiContainerTest {
	
	@Test
	void testEmoji() throws IOException {
		// smiley
		String in = uc(0x1f600);
		check("(1f600)", in);
	}
	
	@Test
	void testNoEmoji() throws IOException {
		String in = "Hi there";
		check(in, in);
	}
	
	@Test
	void testTextAndEmoji() throws IOException {
		String in = "Hi " + uc(0x1f471) + ", how are you?";
		check("Hi (1f471), how are you?", in);
	}
	
	@Test
	void testEmojiSequence() throws IOException {
		String in = "Here is a boy: " + uc(0x1f466) + uc(0x1f3ff);
		check("Here is a boy: (1f466_1f3ff)", in);
	}
	
	@Test
	void testEmojiSequenceNotInDB() throws IOException {
		String in = "Does not exist: " + uc(0x1f386) + uc(0x1f3ff);
		check("Does not exist: (1f386)(1f3ff)", in);
	}
	
	@Test
	void testEmojiNotInDB() {
		String in = uc(0x2f466);
		check(in, in);
	}
	
	@Test
	void testSoftbank() {
		String in = "Hi " + uc(0xe404) + ", how are you?";
		check("Hi (1f601), how are you?", in);
	}
	
	private void check(String expected, String in) {
		EmojiContainer ec = null;
		try {
			ec = new EmojiContainer();
		} catch (IOException e) {
			fail(e);
		}
		
		String actual = ec.replaceEmojis(in, x -> bracket(x));
		assertEquals(expected, actual);
	}
	
	@Test
	void develop() throws IOException {
		EmojiContainerV2 ec = new EmojiContainerV2();
		fail();
	}
	
	private String bracket(String str) {
		return "(" + str + ")";
	}
	
	private String uc(int codePoint) {
		char[] chars = Character.toChars(codePoint);
		return new String(chars);
	}

}
