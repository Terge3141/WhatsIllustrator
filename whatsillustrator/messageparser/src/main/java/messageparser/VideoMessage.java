package messageparser;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class VideoMessage implements IMessage {

	private LocalDateTime timepoint;
	private String sender;
	private Path filepath;
	private String subscription;
	
	public VideoMessage(LocalDateTime timepoint, String sender, Path filepath, String subscription) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.filepath = filepath;
		this.subscription = subscription;
	}

	public LocalDateTime getTimepoint() {
		return this.timepoint;
	}

	public String getSender() {
		return this.sender;
	}

	public Path getFilepath() {
		return this.filepath;
	}

	public String getSubscription() {
		return this.subscription;
	}

}

