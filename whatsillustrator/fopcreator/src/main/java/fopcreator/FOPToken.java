package fopcreator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import helper.EmojiParser.Token;

@XmlRootElement(name = "FOPToken")
public class FOPToken implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement(name = "emoji")
	private String emoji;

	@XmlElement(name = "normal")
	private String normal;

	public FOPToken() {
	}

	public static FOPToken of(String str, boolean isEmoji) {
		FOPToken token = new FOPToken();
		if (isEmoji) {
			token.emoji = str;
			token.normal = null;
		} else {
			token.emoji = null;
			token.normal = str;
		}

		return token;
	}

	public static List<FOPToken> ofEmojiParser(List<Token> tokens, String emojiPrefix) {

		List<FOPToken> fopTokens = new ArrayList<FOPToken>();

		for (Token emojiParserToken : tokens) {
			String str = emojiParserToken.isEmoji()
					? String.format("%s%s.png", emojiPrefix, emojiParserToken.getString())
					: emojiParserToken.getString();
			fopTokens.add(FOPToken.of(str, emojiParserToken.isEmoji()));
		}

		return fopTokens;
	}
}
