package helper.test;

import static org.junit.Assert.*;

import helper.EmojiParser;
import helper.EmojiParser.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class EmojiParserTest {

	@Test
	public void testReplaceEmojis_Single_NotInList() throws IOException {
		List<String> list = new ArrayList<String>();
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f55e);
		String output = parser.replaceEmojis(input, x -> formatFunction(x));
		assertEquals(input, output);
	}

	@Test
	public void testGetTokens_Single_NotInList() throws IOException {
		List<String> list = new ArrayList<String>();
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f55e);
		assertTokensEqual(parser.getTokens(input), input, false);
	}

	@Test
	public void testReplaceEmojis_Single_InList1() {
		List<String> list = Arrays.asList("1f55e");
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f55e);
		String output = parser.replaceEmojis(input, x -> formatFunction(x));
		assertEquals("ICON(1f55e)", output);
	}

	@Test
	public void testGetTokens_Single_InList1() {
		List<String> list = Arrays.asList("1f55e");
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f55e);
		assertTokensEqual(parser.getTokens(input), "1f55e", true);
	}

	@Test
	public void testReplaceEmojis_Single_InList2() {
		List<String> list = Arrays.asList("1f4aa", "1f4aa_1f3fb");
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f4aa);
		String output = parser.replaceEmojis(input, x -> formatFunction(x));
		assertEquals("ICON(1f4aa)", output);
	}

	@Test
	public void testGetTokens_Single_InList2() {
		List<String> list = Arrays.asList("1f4aa", "1f4aa_1f3fb");
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f4aa);
		assertTokensEqual(parser.getTokens(input), "1f4aa", true);
	}

	@Test
	public void testReplaceEmojis_Double_NotInList() {
		List<String> list = new ArrayList<String>();
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f4a8) + getUtf32(0x1f4a9);
		String output = parser.replaceEmojis(input, x -> formatFunction(x));
		assertEquals(input, output);
	}

	@Test
	public void testGetTokens_Double_NotInList() {
		List<String> list = new ArrayList<String>();
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f4a8) + getUtf32(0x1f4a9);
		assertTokensEqual(parser.getTokens(input), input, false);
	}

	@Test
	public void testReplaceEmojis_Double_InList1() {
		List<String> list = Arrays.asList("1f4aa_1f3fb");
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f4aa) + getUtf32(0x1f3fb);
		String output = parser.replaceEmojis(input, x -> formatFunction(x));
		assertEquals("ICON(1f4aa_1f3fb)", output);
	}

	@Test
	public void testGetTokens_Double_InList1() {
		List<String> list = Arrays.asList("1f4aa_1f3fb");
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f4aa) + getUtf32(0x1f3fb);
		assertTokensEqual(parser.getTokens(input), "1f4aa_1f3fb", true);
	}

	@Test
	public void testReplaceEmojis_Double_InList2() {
		List<String> list = Arrays.asList("1f4aa", "1f4aa_1f3fb");
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f4aa) + getUtf32(0x1f3fb);
		String output = parser.replaceEmojis(input, x -> formatFunction(x));
		assertEquals("ICON(1f4aa_1f3fb)", output);
	}

	@Test
	public void testGetTokens_Double_InList2() {
		List<String> list = Arrays.asList("1f4aa", "1f4aa_1f3fb");
		EmojiParser parser = new EmojiParser(list);
		String input = getUtf32(0x1f4aa) + getUtf32(0x1f3fb);
		assertTokensEqual(parser.getTokens(input), "1f4aa_1f3fb", true);
	}

	@Test
	public void testReplaceEmojis_List1() {
		List<String> list = Arrays.asList("1f4aa");
		EmojiParser parser = new EmojiParser(list);
		String input = "abcde";
		String output = parser.replaceEmojis(input, x -> formatFunction(x));
		assertEquals(input, output);
	}

	@Test
	public void testGetTokens_List1() {
		List<String> list = Arrays.asList("1f4aa");
		EmojiParser parser = new EmojiParser(list);
		String input = "abcde";
		assertTokensEqual(parser.getTokens(input), input, false);
	}

	@Test
	public void testReplaceEmojis_List2() {
		List<String> list = Arrays.asList("1f4aa");
		EmojiParser parser = new EmojiParser(list);
		String input = "abcdef" + getUtf32(0x1f4aa) + "GHIJKLM";
		String output = parser.replaceEmojis(input, x -> formatFunction(x));
		assertEquals("abcdefICON(1f4aa)GHIJKLM", output);
	}

	@Test
	public void testGetTokens_List2() {
		List<String> list = Arrays.asList("1f4aa");
		EmojiParser parser = new EmojiParser(list);
		String input = "abcdef" + getUtf32(0x1f4aa) + "GHIJKLM";
		assertTokensEqual(parser.getTokens(input), "abcdef", false, "1f4aa", true, "GHIJKLM", false);
	}

	// https://www.programcreek.com/java-api-examples/index.php?source_dir=EmojiEverywhere-master/app/src/main/java/emojicon/emoji/Symbols.java#
	public static String getUtf32(int codePoint) {
		if (Character.charCount(codePoint) == 1) {
			return String.valueOf(codePoint);
		} else {
			return new String(Character.toChars(codePoint));
		}
	}

	private String formatFunction(String str) {
		return String.format("ICON(%s)", str);
	}

	/**
	 * Checks if a list of tokens equals the expected ones. Runs silently unless
	 * tokens don't match
	 * 
	 * @param actual   Actual tokens
	 * @param expected Expected tokens, passed as String,Boolean,String,Boolean,...
	 */
	private void assertTokensEqual(List<Token> tokens, Object... expected) {
		assertEquals("Number of expected argument must be equal", 0, expected.length % 2);

		int size = expected.length / 2;
		assertEquals(size, tokens.size());
		for (int i = 0; i < size; i++) {
			Token token = tokens.get(i);
			String expectedStr = (String) expected[2 * i];
			boolean emoji = (boolean) expected[2 * i + 1];

			assertEquals(String.format("List differ at index %d", i), expectedStr, token.getString());
			assertEquals(String.format("List differ at index %d", i), emoji, token.isEmoji());
		}
	}
}
