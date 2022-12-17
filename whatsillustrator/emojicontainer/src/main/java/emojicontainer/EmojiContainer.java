package emojicontainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.text.TextStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import helper.Misc;

public class EmojiContainer {
	private List<String> emojiList;
	
	private static final String SEPERATOR = "_";
	private final String EMOJIPREFIX = "emoji_u";
	private static Logger logger = LogManager.getLogger(EmojiContainer.class);
	
	private int tokenMax;
	
	public EmojiContainer() throws IOException {
		this.emojiList = readEmojiList();
		
		Iterator<String> it = this.emojiList.iterator();

		this.tokenMax = 0;
		while (it.hasNext()) {
			String str = it.next();
			this.tokenMax = Math.max(this.tokenMax, str.split(SEPERATOR).length);
		}

		this.tokenMax++;
	}
	
	/**
	 * Replaces The emojis for a given string.
	 * 
	 * @param str The string to replaced
	 * @param emojiFormatFunction The lambda function that is when an emoji is
	 * found. The unicode(s) are passed to the function
	 * and the formatted code is returned.
	 * @return The replaced string
	 */
	public String replaceEmojis(String str, Function<String, String> emojiFormatFunction) {
		List<Token> tokens = getTokens(str);

		TextStringBuilder tsb = new TextStringBuilder();
		for (Token token : tokens) {
			if (token.isEmoji()) {
				tsb.append(emojiFormatFunction.apply(token.getString()));
			} else {
				tsb.append(token.getString());
			}
		}

		return tsb.toString();
	}
	
	public List<Token> getTokens(String str) {
		List<Token> tokens = new ArrayList<Token>();
		
		if(str==null) {
			return tokens;
		}
		
		int index = 0;
		while (index < str.length()) {
			index = parseChars(str, index, tokens);
		}

		return tokens;
	}
	
	public Path copyEmoji(String id, Path dst) throws IOException {
		String filename = this.EMOJIPREFIX + id + ".png";
		if(Files.isDirectory(dst)) {
			dst = dst.resolve(filename);
		}
		
		InputStream in = this.getClass().getResourceAsStream(filename);
		Files.copy(in, dst, StandardCopyOption.REPLACE_EXISTING);
		
		return dst;
	}

	private int parseChars(String str, int index, List<Token> tokens) {
		return parseChars(str, index, tokens, null, 0);
	}

	/**
	 * Recursively parses one or more unicode characters starting at index
	 * 
	 * @param str   String to be parsed
	 * @param index start index
	 * @param tsb   StringBuilder were the result is written to
	 * @param last  Unicode from last character, null for iteration cnt 0
	 * @param cnt   Iteration cnt, starting at 0
	 * @return index of the next character to be parsed, -1 if unicode chain was not
	 *         found or end of string was reached
	 */
	private int parseChars(String str, int index, List<Token> tokens, String last, int cnt) {
		if (cnt == tokenMax) {
			return -1;
		}

		if (index == str.length()) {
			return -1;
		}

		int codePoint = Character.codePointAt(str, index);
		int charCnt = Character.charCount(codePoint);
		String strHex = String.format("%04x", codePoint);

		String suggestion = strHex;
		if (last != null) {
			suggestion = last + SEPERATOR + suggestion;
		}

		int result = parseChars(str, index + charCnt, tokens, suggestion, cnt + 1);
		if (result == -1) {
			if (Misc.listContains(emojiList, suggestion)) {
				tokens.add(new Token(suggestion, true));
				return index + charCnt;
			} else {
				if (cnt == 0) {
					String replacement = fromUtf32toString(codePoint);
					boolean emoji = false;

					// See if it is an SoftBank encoded character
					String alternative = SoftBankConverter.getNewUnicode(suggestion);
					if (alternative != null) {
						if (Misc.listContains(emojiList, alternative)) {
							replacement = alternative;
							emoji = true;
						}
					}

					if (emoji) {
						tokens.add(new Token(replacement, true));
					} else {
						Token lastToken = null;
						if (tokens.size() > 0) {
							lastToken = tokens.get(tokens.size() - 1);
						}

						if (lastToken == null) {
							tokens.add(new Token(replacement, false));
						} else {
							if (lastToken.isEmoji()) {
								tokens.add(new Token(replacement, false));
							} else {
								lastToken.append(replacement);
							}
						}
					}

					logger.trace("{} {}", codePoint, alternative);

					return index + charCnt;
				}
			}

			return -1;
		} else {
			return result;
		}
	}
	
	private List<String> readEmojiList() throws IOException {
		List<String> list = new ArrayList<>();
		
		InputStream in = this.getClass().getResourceAsStream("files.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		String fileName;
		while((fileName=br.readLine())!=null) {
			String nr = fileName.replace(EMOJIPREFIX, "").replace(".png", "");
			list.add(nr);

			String[] excludes = { "0023", "002a", "0030", "0031", "0032", "0033", "0034", "0035", "0036", "0037",
					"0038", "0039" };

			for (String str : excludes) {
				list.remove(str);
			}
		}

		logger.info("Loaded {} entries}", list.size());

		return list;
	}
	
	private static String fromUtf32toString(int codePoint) {
		return new String(Character.toChars(codePoint));
	}
	
	public String getEmojiPrefix() {
		return this.EMOJIPREFIX;
	}
	
	public class Token {
		private TextStringBuilder tsb;
		private boolean emoji;

		public Token(String str, boolean emoji) {
			tsb = new TextStringBuilder(str);
			this.emoji = emoji;
		}

		public String getString() {
			return tsb.toString();
		}

		public void append(String str) {
			tsb.append(str);
		}

		public boolean isEmoji() {
			return this.emoji;
		}
	}
}
