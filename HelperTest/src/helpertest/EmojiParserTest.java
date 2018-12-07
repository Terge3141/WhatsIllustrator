package helpertest;

import static org.junit.Assert.*;

import helper.EmojiParser;
import helper.FileHandler;
import helper.Misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class EmojiParserTest {

	@Test
	public void testReplaceEmojis_Single_NotInList() throws IOException {
		List<String> list = new ArrayList<String>();
		EmojiParser parser = new EmojiParser(list,
				new EmojiFormatFunctionStub());
		String input = getUtf32(0x1f55e);
		String output = parser.replaceEmojis(input);
		assertEquals(input, output);
	}

	@Test
	public void TestReplaceEmojis_Single_InList1() {
		List<String> list = Arrays.asList("1f55e");
		EmojiParser parser = new EmojiParser(list,
				new EmojiFormatFunctionStub());
		String input = getUtf32(0x1f55e);
		String output = parser.replaceEmojis(input);
		assertEquals("ICON(1f55e)", output);
	}

	@Test
	public void TestReplaceEmojis_Single_InList2() {
		List<String> list = Arrays.asList("1f4aa", "1f4aa_1f3fb");
		EmojiParser parser = new EmojiParser(list,
				new EmojiFormatFunctionStub());
		String input = getUtf32(0x1f4aa);
		String output = parser.replaceEmojis(input);
		assertEquals("ICON(1f4aa)", output);
	}

	@Test
	public void TestReplaceEmojis_Double_NotInList() {
		List<String> list = new ArrayList<String>();
		EmojiParser parser = new EmojiParser(list,
				new EmojiFormatFunctionStub());
		String input = getUtf32(0x1f4a8) + getUtf32(0x1f4a9);
		String output = parser.replaceEmojis(input);
		assertEquals(input, output);
	}

	@Test
	public void TestReplaceEmojis_Double_InList1() {
		List<String> list = Arrays.asList("1f4aa_1f3fb");
		EmojiParser parser = new EmojiParser(list,
				new EmojiFormatFunctionStub());
		String input = getUtf32(0x1f4aa) + getUtf32(0x1f3fb);
		String output = parser.replaceEmojis(input);
		assertEquals("ICON(1f4aa_1f3fb)", output);
	}

	@Test
	public void TestReplaceEmojis_Double_InList2() {
		List<String> list = Arrays.asList("1f4aa", "1f4aa_1f3fb");
		EmojiParser parser = new EmojiParser(list,
				new EmojiFormatFunctionStub());
		String input = getUtf32(0x1f4aa) + getUtf32(0x1f3fb);
		String output = parser.replaceEmojis(input);
		assertEquals("ICON(1f4aa_1f3fb)", output);
	}

	@Test
	public void TestReplaceEmojis_List1() {
		List<String> list = Arrays.asList("1f4aa");
		EmojiParser parser = new EmojiParser(list,
				new EmojiFormatFunctionStub());
		String input = "abcde";
		String output = parser.replaceEmojis(input);
		assertEquals(input, output);
	}

	@Test
	public void TestReplaceEmojis_List2() {
		List<String> list = Arrays.asList("1f4aa");
		EmojiParser parser = new EmojiParser(list,
				new EmojiFormatFunctionStub());
		String input = "abcdef" + getUtf32(0x1f4aa) + "GHIJKLM";
		String output = parser.replaceEmojis(input);
		assertEquals("abcdefICON(1f4aa)GHIJKLM", output);
	}

	@Test
	public void TestReplaceEmojis_NormalCharAndEmoji() {
		// # and 0x20e3 will result in 0023-20e3 (#=0023)
		List<String> list = Arrays.asList("0023_20e3");
		EmojiParser parser = new EmojiParser(list,
				new EmojiFormatFunctionStub());
		String input = "#" + getUtf32(0x20e3);
		String output = parser.replaceEmojis(input);
		assertEquals("ICON(0023_20e3)", output);
	}

	// https://www.programcreek.com/java-api-examples/index.php?source_dir=EmojiEverywhere-master/app/src/main/java/emojicon/emoji/Symbols.java#
	public static String getUtf32(int codePoint) {
		if (Character.charCount(codePoint) == 1) {
			return String.valueOf(codePoint);
		} else {
			return new String(Character.toChars(codePoint));
		}
	}

	public static String convert16to32(String toConvert) {
		for (int i = 0; i < toConvert.length();) {
			int codePoint = Character.codePointAt(toConvert, i);
			i += Character.charCount(codePoint);
			// System.out.printf("%x%n", codePoint);
			String utf32 = String.format("0x%x%n", codePoint);
			return utf32;
		}
		return null;
	}

	/*
	 * String str =
	 * Misc.readAllText("/home/michael/whatsappprint/whatsbook/smiler.txt");
	 * byte[] b = str.getBytes(); String c = "\u1f55e"; String d =
	 * newString(0x1f55e); String e = d + "abc";
	 * Misc.writeAllText("/tmp/out_c.txt", c);
	 * Misc.writeAllText("/tmp/out_d.txt", d); int f = e.length();
	 * System.out.println();
	 */

}
