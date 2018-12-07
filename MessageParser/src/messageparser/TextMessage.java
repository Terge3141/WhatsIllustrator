package messageparser;

import java.util.Calendar;

public class TextMessage implements IMessage {
	
	
	public Calendar timepoint;
	public String sender;
	public String content;

	public TextMessage(Calendar timepoint, String sender,
			String Content) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.content = Content;
	}

	public Calendar getTimepoint() {
		return this.timepoint;
	}

	public String getSender() {
		return this.sender;
	}

	public String getContent() {
		return this.content;
	}
}
