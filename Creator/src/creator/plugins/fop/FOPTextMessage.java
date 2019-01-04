package creator.plugins.fop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import helper.DateUtils;
import helper.EmojiParser;
import helper.EmojiParser.Token;
import messageparser.TextMessage;

@XmlRootElement(name = "textmessage")
public class FOPTextMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement(name = "time")
	private String timepoint;

	@XmlElement(name = "sender")
	private String sender;

	@XmlElement(name = "text")
	private List<FOPToken> tokens;

	public FOPTextMessage() {
	}

	public static FOPTextMessage of(TextMessage message, DateUtils dateUtils, EmojiParser emojiParser) {
		FOPTextMessage fopMessage = new FOPTextMessage();
		fopMessage.timepoint = dateUtils.formatTimeString(message.getTimepoint());
		fopMessage.sender = message.getSender();

		fopMessage.tokens = new ArrayList<FOPToken>();
		List<Token> emojiParserTokens = emojiParser.getTokens(message.content);
		for (Token emojiParserToken : emojiParserTokens) {
			String str = emojiParserToken.isEmoji()
					? String.format("%s%s.png", emojiParser.getEmojiPrefix(), emojiParserToken.getString())
					: emojiParserToken.getString();
			fopMessage.tokens.add(FOPToken.ofEmoji(str, emojiParserToken.isEmoji()));
		}

		return fopMessage;
	}
}
