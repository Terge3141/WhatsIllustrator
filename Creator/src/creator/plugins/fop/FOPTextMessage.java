package creator.plugins.fop;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
	private String content;

	public FOPTextMessage() {
	}

	public static FOPTextMessage of(TextMessage message, DateUtils dateUtils) {
		FOPTextMessage fopMessage = new FOPTextMessage();
		fopMessage.timepoint = dateUtils.formatTimeString(message.getTimepoint());
		fopMessage.sender = message.getSender();
		fopMessage.content = message.getContent();

		return fopMessage;
	}
}
