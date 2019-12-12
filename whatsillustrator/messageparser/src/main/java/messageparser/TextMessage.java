package messageparser;

import java.time.LocalDateTime;

public class TextMessage implements IMessage {

	public LocalDateTime timepoint;
	public String sender;
	public String content;

	public TextMessage(LocalDateTime timepoint, String sender, String Content) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.content = Content;
	}

	public LocalDateTime getTimepoint() {
		return this.timepoint;
	}

	public String getSender() {
		return this.sender;
	}

	public String getContent() {
		return this.content;
	}
}
