package fopcreator;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	public static FOPTextMessage of(TextMessage message, DateUtils dateUtils, EmojiContainer emojiContainer) throws IOException {
		FOPTextMessage fopMessage = new FOPTextMessage();
		fopMessage.timepoint = dateUtils.formatTimeString(message.getTimepoint());
		fopMessage.sender = message.getSender();

		List<Token> emojiContainerTokens = emojiContainer.getTokens(message.content);
		fopMessage.tokens = FOPToken.ofEmojiContainer(emojiContainerTokens, emojiContainer.getEmojiPrefix());
		for(EmojiContainer.Token token : emojiContainerTokens) {
			if(token.isEmoji()) {
				Path dst = Paths.get("/tmp/emojis");
				System.out.println("Copy " + token.getString());
				emojiContainer.copyEmoji(token.getString(), dst);
			}
		}

		return fopMessage;
	}
}
