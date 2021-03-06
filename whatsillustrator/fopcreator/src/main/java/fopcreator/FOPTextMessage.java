package fopcreator;

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

		List<Token> emojiParserTokens = emojiParser.getTokens(message.content);
		fopMessage.tokens = FOPToken.ofEmojiParser(emojiParserTokens, emojiParser.getEmojiPrefix());

		return fopMessage;
	}
}
