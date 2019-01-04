package creator.plugins.fop;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import helper.DateUtils;
import messageparser.MediaMessage;

@XmlRootElement(name="mediamessage")
public class FOPMediaMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement(name = "time")
	private String timepoint;

	@XmlElement(name = "sender")
	private String sender;

	@XmlElement(name = "filename")
	private String filename;

	@XmlElement(name = "subscription")
	private String subscription;

	public FOPMediaMessage() {
	}

	public static FOPMediaMessage of(MediaMessage message, DateUtils dateUtils) {
		FOPMediaMessage fopMessage = new FOPMediaMessage();
		fopMessage.timepoint = dateUtils.formatTimeString(message.getTimepoint());
		fopMessage.sender = message.getSender();
		fopMessage.filename = message.getFilename();
		fopMessage.subscription = message.getSubscription();

		return fopMessage;
	}
}
