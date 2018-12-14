package helper;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

public class EmojiParser {

	private List<String> emojiList;
	private IEmojiFormatFunction emojiFormatFunction;

	private int tokenMax;

	private static final String SEPERATOR = "_";
	
	private PrintWriter debug = null;

	public EmojiParser(List<String> emojiList,
			IEmojiFormatFunction emojiFormatFunction) {
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

	public String replaceEmojis(String str) {
		StringBuilder sb = new StringBuilder();
		int index = 0;
		while (index < str.length()) {
			index = parseChars(str, index, sb);
		}

		return sb.toString();
	}

	private int parseChars(String str, int index, StringBuilder sb) {
		return parseChars(str, index, sb, null, 0);
	}

	private int parseChars(String str, int index, StringBuilder sb,
			String last, int cnt) {
		if (cnt == tokenMax)
        {
            return -1;
        }

        if (index == str.length())
        {
            return -1;
        }
        
        int codePoint = Character.codePointAt(str, index);
        int charCnt= Character.charCount(codePoint);
        String strHex = String.format("%04x", codePoint);
        
        String suggestion = strHex;
        if (last != null)
        {
            suggestion = last + SEPERATOR + suggestion;
        }

        int result = parseChars(str, index + charCnt, sb, suggestion, cnt + 1);
        if (result == -1)
        {
            if (Misc.listContains(emojiList, suggestion))
            {
            	sb.append(emojiFormatFunction.format(suggestion));
                return index + charCnt;
            }
            else
            {
                if (cnt == 0)
                {
                	String replacement=fromUtf32toString(codePoint);

                    // See if it is an SoftBank encoded character
                    String alternative = SoftBankConverter.getNewUnicode(suggestion);
                    if (alternative != null)
                    {
                        if (Misc.listContains(emojiList, alternative))
                        {
                            replacement = emojiFormatFunction.format(alternative);
                        }
                    }

                    
                    sb.append(replacement);
                    if (this.debug != null)
                    {
                    	this.debug.format("%d %s", codePoint, alternative);
                    }
                    
                    return index + charCnt;
                }
            }

            return -1;
        }
        else
        {
            return result;
        }
	}
	
	public static String fromUtf32toString(int codePoint) {
		/*if (Character.charCount(codePoint) == 1) {
			return String.valueOf(codePoint);
		} else {
			return new String(Character.toChars(codePoint));
		}*/
		return new String(Character.toChars(codePoint));
	}
	
	
}
