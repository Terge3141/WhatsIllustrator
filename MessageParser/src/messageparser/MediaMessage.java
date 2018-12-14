package messageparser;

import java.time.LocalDateTime;

public class MediaMessage implements IMessage {
	
	public LocalDateTime timepoint;
	public String sender;
	public String filename;
	public String subscription;
	
	public MediaMessage(LocalDateTime timepoint, String sender, String filename, String subscription)
    {
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
