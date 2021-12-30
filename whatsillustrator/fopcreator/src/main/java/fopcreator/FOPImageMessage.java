package fopcreator;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import helper.DateUtils;
import helper.EmojiParser;
import helper.EmojiParser.Token;
import messageparser.ImageMessage;
import messageparser.MediaOmittedMessage;

@XmlRootElement(name = "imagemessage")
public class FOPImageMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement(name = "time")
	private String timepoint;

	@XmlElement(name = "sender")
	private String sender;

	@XmlElement(name = "src")
	private String src;

	@XmlElement(name = "subscription")
	private List<FOPToken> tokens;

	public FOPImageMessage() {
	}

	public static FOPImageMessage of(ImageMessage message, DateUtils dateUtils, EmojiParser emojiParser) {
		FOPImageMessage fopMessage = new FOPImageMessage();
		fopMessage.timepoint = dateUtils.formatTimeString(message.getTimepoint());
		fopMessage.sender = message.getSender();
		fopMessage.src = message.getFilepath().toString();

		List<Token> emojiParserTokens = emojiParser.getTokens(message.getSubscription());
		fopMessage.tokens = FOPToken.ofEmojiParser(emojiParserTokens, emojiParser.getEmojiPrefix());

		return fopMessage;
	}

	public static List<FOPImageMessage> of(MediaOmittedMessage message, DateUtils dateUtils) {
		List<FOPImageMessage> list = new ArrayList<FOPImageMessage>();
		for (Path absPath : message.getAbspaths()) {
			FOPImageMessage fopMessage = new FOPImageMessage();
			fopMessage.timepoint = dateUtils.formatTimeString(message.getTimepoint());
			fopMessage.sender = message.getSender();
			fopMessage.src = absPath.toString();
			fopMessage.tokens = null;

			list.add(fopMessage);
		}

		return list;
	}
}
