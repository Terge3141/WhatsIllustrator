package creator.plugins.fop;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import helper.DateUtils;
import messageparser.TextMessage;

@XmlRootElement
public class FOPTextMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement
	private String timepoint;

	@XmlElement
	private String sender;

	@XmlElement
	private String content;

	public FOPTextMessage() {
	}

	public FOPTextMessage(TextMessage message, DateUtils dateUtils) {
		this.timepoint = dateUtils.formatTimeString(message.getTimepoint());
		this.sender = message.getSender();
		this.content = message.getContent();
	}
}
