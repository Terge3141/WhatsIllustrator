package fopcreator;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import emojicontainer.EmojiContainer;
import emojicontainer.EmojiContainer.Token;
import helper.DateUtils;
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

	public static FOPTextMessage of(TextMessage message, DateUtils dateUtils, EmojiContainer emojiContainer) {
		FOPTextMessage fopMessage = new FOPTextMessage();
		fopMessage.timepoint = dateUtils.formatTimeString(message.getTimepoint());
		fopMessage.sender = message.getSender();

		List<Token> emojiContainerTokens = emojiContainer.getTokens(message.content);
		fopMessage.tokens = FOPToken.ofEmojiContainer(emojiContainerTokens, emojiContainer.getEmojiPrefix());

		return fopMessage;
	}
}
