package emojicontainer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import emojicontainer.EmojiContainer.Token;

class EmojiContainerTest {
	
	@Test
	void testEmoji() throws IOException {
		// smiley
		String in = uc(0x1f600);
		check("(1F600)", in);
	}
	
	@Test
	void testNoEmoji() throws IOException {
		String in = "Hi there";
		check(in, in);
	}
	
	@Test
	void testTextAndEmoji() throws IOException {
		String in = "Hi " + uc(0x1f471) + ", how are you?";
		check("Hi (1F471), how are you?", in);
	}
	
	@Test
	void testEmojiSequence() throws IOException {
		String in = "Here is a boy: " + uc(0x1f466) + uc(0x1f3ff);
		check("Here is a boy: (1F466-1F3FF)", in);
	}
	
	@Test
	void testEmojiSequenceNotInDB() throws IOException {
		String in = "Does not exist: " + uc(0x1f386) + uc(0x1f3ff);
		check("Does not exist: (1F386)(1F3FF)", in);
	}
	
	@Test
	void testEmojiNotInDB() {
		String in = uc(0x2f466);
		check(in, in);
	}
	
	@Test
	void testSoftbank() {
		String in = "Hi " + uc(0xe404) + ", how are you?";
		check("Hi (1F601), how are you?", in);
	}
	
	@Test
	void testGetTokens_TextOnly() throws IOException {
		String in = "Hi, this is a test";
		EmojiContainer ec = new EmojiContainer();
		List<Token> tokens = ec.getTokens(in);
		
		assertEquals(1, tokens.size());
		assertEquals(in, tokens.get(0).getString());
		assertFalse(tokens.get(0).isEmoji());
	}
	
	@Test
	void testGetTokens_EmojiOnly() throws IOException {
		String in = uc(0x1f471);
		EmojiContainer ec = new EmojiContainer();
		List<Token> tokens = ec.getTokens(in);
		
		assertEquals(1, tokens.size());
		assertEquals("1F471", tokens.get(0).getString());
		assertTrue(tokens.get(0).isEmoji());
	}
	
	@Test
	void testGetTokens_EmojiAtTextEnd() throws IOException {
		String in = "Hi " + uc(0x1f471);
		EmojiContainer ec = new EmojiContainer();
		List<Token> tokens = ec.getTokens(in);
		
		assertEquals(2, tokens.size());
		
		assertEquals("Hi ", tokens.get(0).getString());
		assertFalse(tokens.get(0).isEmoji());
		
		assertEquals("1F471", tokens.get(1).getString());
		assertTrue(tokens.get(1).isEmoji());
	}
	
	@Test
	void testGetTokens_TextAndEmoji() throws IOException {
		String in = "Hi " + uc(0x1f471) + ", how are you?";
		EmojiContainer ec = new EmojiContainer();
		List<Token> tokens = ec.getTokens(in);
		
		assertEquals(3, tokens.size());
		
		assertEquals("Hi ", tokens.get(0).getString());
		assertFalse(tokens.get(0).isEmoji());
		
		assertEquals("1F471", tokens.get(1).getString());
		assertTrue(tokens.get(1).isEmoji());
		
		assertEquals(", how are you?", tokens.get(2).getString());
		assertFalse(tokens.get(2).isEmoji());
	}
	
	@Test
	void testAllEmojisHaveFile() throws IOException {
		EmojiContainer ec = new EmojiContainer();
		assertTrue(ec.allEmojisHaveFile());
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
	
	/*@Test
	void develop() throws IOException {
		EmojiContainerV2 ec = new EmojiContainerV2();
		fail();
	}*/
	
	private String bracket(String str) {
		return "(" + str + ")";
	}
	
	private String uc(int codePoint) {
		char[] chars = Character.toChars(codePoint);
		return new String(chars);
	}

}
