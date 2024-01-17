package emojicontainer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import emojicontainer.EmojiContainer.Token;

class EmojiContainerTest {
	
	@Test
	void replaceEmojis_testEmoji() throws IOException {
		// smiley
		String in = uc(0x1f600);
		checkReplaceEmojis("(1F600)", in);
	}
	
	@Test
	void replaceEmojis_testNoEmoji() throws IOException {
		String in = "Hi there";
		checkReplaceEmojis(in, in);
	}
	
	@Test
	void replaceEmojis_testTextAndEmoji() throws IOException {
		String in = "Hi " + uc(0x1f471) + ", how are you?";
		checkReplaceEmojis("Hi (1F471), how are you?", in);
	}
	
	@Test
	void replaceEmojis_testEmojiSequence() throws IOException {
		String in = "Here is a boy: " + uc(0x1f466) + uc(0x1f3ff);
		checkReplaceEmojis("Here is a boy: (1F466-1F3FF)", in);
	}
	
	@Test
	void replaceEmojis_testEmojiSequenceNotInDB() throws IOException {
		String in = "Does not exist: " + uc(0x1f386) + uc(0x1f3ff);
		checkReplaceEmojis("Does not exist: (1F386)(1F3FF)", in);
	}
	
	@Test
	void replaceEmojis_testEmojiNotInDB() {
		String in = uc(0x2f466);
		checkReplaceEmojis(in, in);
	}
	
	@Test
	void replaceEmojis_testSoftbank() {
		String in = "Hi " + uc(0xe404) + ", how are you?";
		checkReplaceEmojis("Hi (1F601), how are you?", in);
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
	
	@Test
	void testWrapEmojis_EmojiOnly() {
		String in = uc(0x1f471);
		checkWrapEmojis("(" + in + ")", in);
	}
	
	@Test
	void testWrapEmojis_EmojiAndText() {
		String in = "This is a " + uc(0x1f471) + " with text";
		checkWrapEmojis("This is a (" + uc(0x1f471) + ") with text", in);
	}
	
	@Test
	void testWrapEmojis_TextOnly() {
		String in = "Text only";
		checkWrapEmojis(in, in);
	}
	
	@Test
	void testWrapEmojis_testSoftbank() {
		String in = "Hi " + uc(0xe404) + ", how are you?";
		checkWrapEmojis("Hi (" + uc(0x1f601) + "), how are you?", in);
	}
	
	private void checkWrapEmojis(String expected, String in) {
		EmojiContainer ec = null;
		try {
			ec = new EmojiContainer();
		} catch (IOException e) {
			fail(e);
		}
		
		String actual = ec.wrapEmojis(in, x -> bracket(x));
		assertEquals(expected, actual);
	}
	
	private void checkReplaceEmojis(String expected, String in) {
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
