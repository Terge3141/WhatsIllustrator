package emojicontainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.text.TextStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fellbaum.jemoji.Emoji;
import net.fellbaum.jemoji.EmojiManager;
import net.fellbaum.jemoji.IndexedEmoji;

public class EmojiContainer {
	
	private static final String SEPERATOR = "_";
	private final String EMOJIPREFIX = "emoji_u";
	private static Logger logger = LogManager.getLogger(EmojiContainer.class);

	private Map<Integer, String> softbankMap;
	
	public EmojiContainer() throws IOException {
		this.softbankMap = readSoftBankList();
	}
	
	/**
	 * Replaces The emojis for a given string.
	 * 
	 * @param str The string to replaced
	 * @param emojiFormatFunction The lambda function that is called when an emoji is
	 * found. The unicode(s) are passed to the function
	 * and the formatted code should be returned.
	 * @return The replaced string
	 */
	public String replaceEmojis(String str, Function<String, String> emojiFormatFunction) {
		String buf = str;
		buf = replaceSoftBankChars(buf);
		buf = EmojiManager.replaceAllEmojis(buf, x -> replaceEmoji(x, emojiFormatFunction));
		return buf;
	}
	
	public List<Token> getTokens(String str) {
		List<IndexedEmoji> emojis = EmojiManager.extractEmojisInOrderWithIndex(str);
		
		List<Token> tokens = new ArrayList<Token>();
		int start = 0;
		for (IndexedEmoji indexedEmoji : emojis) {
			int index = indexedEmoji.getCharIndex();
			// normal string
			if(index > start) {
				tokens.add(new Token(str.substring(start, index), false));
			}
			
			String emojiCode = replaceEmoji(indexedEmoji.getEmoji(), x -> x);
			
			tokens.add(new Token(emojiCode, true));
			
			start = index + indexedEmoji.getEmoji().getEmoji().length();
		}
		
		if(start < str.length()) {
			tokens.add(new Token(str.substring(start), false));
		}
		
		return tokens;
	}
	
	public Path copyEmoji(String id, Path dst) throws IOException {
		String filename = getFilename(id);
		if(Files.isDirectory(dst)) {
			dst = dst.resolve(filename);
		}
		
		System.out.println(filename);
		String str = filename.substring(7, filename.length() - 4);
		StringTokenizer st = new StringTokenizer(str, "_");
		String emoji = "";
		while(st.hasMoreTokens()) {
			emoji = emoji + fromUtf32toString(Integer.parseInt(st.nextToken(), 16));
		}
				
		System.out.println(emoji);
		
		InputStream in = this.getClass().getResourceAsStream("/" + filename);
		// emoji does not exist, use question mark instead
		if(in==null) {
			in = this.getClass().getResourceAsStream("/" + getFilename("2753"));
		}
		
		Files.copy(in, dst, StandardCopyOption.REPLACE_EXISTING);
		
		return dst;
	}
	
	private String getFilename(String id) {
		return this.EMOJIPREFIX + id + ".png";
	}
	
	private String replaceEmoji(Emoji emoji, Function<String, String> emojiFormatFunction) {
		String str = emoji.getEmoji().codePoints().mapToObj(operand -> Integer.toHexString(operand)).collect(Collectors.joining(SEPERATOR));
		return emojiFormatFunction.apply(str);
	}
	
	private String replaceSoftBankChar(int codePoint) {
		if(softbankMap.containsKey(codePoint)) {
			return softbankMap.get(codePoint);
		} else {
			return new String(Character.toChars(codePoint));
		}
	}
	
	private String replaceSoftBankChars(String str) {
		return str.chars()
			.mapToObj(x -> replaceSoftBankChar(x))
			.collect(Collectors.joining());
	}
	
	private Map<Integer, String> readSoftBankList() {
		InputStream in = this.getClass().getResourceAsStream("/emojicontainer/data_softbank_map.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		return 
			br.lines()
			.map(x -> {return x.trim();}) // remove white spaces
			.filter(x -> !x.startsWith("#") && (x.length() > 0)) // no comments and empty lines
			.map(x -> {return getPair(x);}) // convert to two columns
			.collect(Collectors.toMap(x -> Integer.parseInt(x[0], 16), x -> convertCodePoints(x[1])));
	}
	
	private String[] getPair(String line){
		StringTokenizer st = new StringTokenizer(line, " ");
		return new String[] {st.nextToken(), st.nextToken()};
	}
	
	// converts one or more unicode hex values to a utf16-string
	// input example "1f466-1f3ff"
	private String convertCodePoints(String codesPointsStr) {
		return Collections.list(new StringTokenizer(codesPointsStr, "-")).stream()
			.map(token -> (String)token) // convert to String
			.map(x -> Integer.parseInt(x, 16)) // to hex value
			.map(x -> new String(Character.toChars(x))) // string unicode hex value
			.collect(Collectors.joining());
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
