package helper;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.text.TextStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parses a string for emojis and replaces them using a lambda function which is
 * passed as an argument
 * 
 * @author Michael Elvers
 *
 */
public class EmojiParser {

	private static Logger logger = LogManager.getLogger();

	private List<String> emojiList;
	private Function<String, String> emojiFormatFunction;

	private int tokenMax;

	private static final String SEPERATOR = "_";

	/**
	 * Constructor
	 * 
	 * @param emojiList           A list of emoji codes that can be replaced. If an
	 *                            emoji is made of more than one unicode the
	 *                            unicodes are separated by the character '_', for
	 *                            example 270d_1f3fd
	 * @param emojiFormatFunction The lambda function that is when an emoji is
	 *                            found. The unicode(s) are passed to the function
	 *                            and the formatted code is returned.
	 */
	public EmojiParser(List<String> emojiList, Function<String, String> emojiFormatFunction) {
		this.emojiList = emojiList;
		this.emojiFormatFunction = emojiFormatFunction;

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
	 * @return The replaced string
	 */
	public String replaceEmojis(String str) {
		TextStringBuilder tsb = new TextStringBuilder();
		int index = 0;
		while (index < str.length()) {
			index = parseChars(str, index, tsb);
		}

		return tsb.toString();
	}

	private int parseChars(String str, int index, TextStringBuilder tsb) {
		return parseChars(str, index, tsb, null, 0);
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
	private int parseChars(String str, int index, TextStringBuilder tsb, String last, int cnt) {
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

		int result = parseChars(str, index + charCnt, tsb, suggestion, cnt + 1);
		if (result == -1) {
			if (Misc.listContains(emojiList, suggestion)) {
				tsb.append(emojiFormatFunction.apply(suggestion));
				return index + charCnt;
			} else {
				if (cnt == 0) {
					String replacement = fromUtf32toString(codePoint);

					// See if it is an SoftBank encoded character
					String alternative = SoftBankConverter.getNewUnicode(suggestion);
					if (alternative != null) {
						if (Misc.listContains(emojiList, alternative)) {
							replacement = emojiFormatFunction.apply(alternative);
						}
					}

					tsb.append(replacement);
					logger.debug("%d %s", codePoint, alternative);

					return index + charCnt;
				}
			}

			return -1;
		} else {
			return result;
		}
	}

	private static String fromUtf32toString(int codePoint) {
		return new String(Character.toChars(codePoint));
	}
}
