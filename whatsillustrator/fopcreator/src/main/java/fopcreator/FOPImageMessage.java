package fopcreator;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import emojicontainer.EmojiContainer;
import emojicontainer.EmojiContainer.Token;
import helper.DateUtils;
import messageparser.ImageMessage;
import messageparser.MediaOmittedMessage;
import messageparser.VideoMessage;

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

	public static FOPImageMessage of(ImageMessage message, DateUtils dateUtils, EmojiContainer emojiContainer) {
		FOPImageMessage fopMessage = new FOPImageMessage();
		fopMessage.timepoint = dateUtils.formatTimeString(message.getTimepoint());
		fopMessage.sender = message.getSender();
		fopMessage.src = message.getFilepath().toString();

		List<Token> emojiParserTokens = emojiContainer.getTokens(message.getSubscription());
		fopMessage.tokens = FOPToken.ofEmojiContainer(emojiParserTokens, emojiContainer.getEmojiPrefix());

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
	
	public static List<FOPImageMessage> of(VideoMessage message, DateUtils dateUtils, List<Path> paths) {
		List<FOPImageMessage> list = new ArrayList<FOPImageMessage>();
		for (Path absPath : paths) {
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
