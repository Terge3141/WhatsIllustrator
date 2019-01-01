package messageparser;

import java.time.LocalDateTime;

public class ImageMessage implements IMessage {

	private LocalDateTime timepoint;
	private String sender;
	private String filename;
	private String subscription;

	public ImageMessage(LocalDateTime timepoint, String sender, String filename, String subscription) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.filename = filename;
		this.subscription = subscription;
	}

	public LocalDateTime getTimepoint() {
		return this.timepoint;
	}

	public String getSender() {
		return this.sender;
	}

	public String getFilename() {
		return this.filename;
	}

	public String getSubscription() {
		return this.subscription;
	}

}
