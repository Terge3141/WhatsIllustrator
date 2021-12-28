package messageparser;

import java.time.LocalDateTime;

public class LinkMessage implements IMessage {

	private LocalDateTime timepoint;
	private String sender;
	private String url;

	public LinkMessage(LocalDateTime timepoint, String sender, String url) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.url = url;
	}

	public LocalDateTime getTimepoint() {
		return this.timepoint;
	}
	
	public String getSender() {
		return this.sender;
	}

	public String getUrl() {
		return this.url;
	}
}
