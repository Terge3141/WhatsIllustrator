package messageparser;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class ImageStackMessage implements IMessage {
	private LocalDateTime timepoint;
	private String sender;
	private List<Path> filepaths;
	private String subscription;

	public ImageStackMessage(LocalDateTime timepoint, String sender, List<Path> filepaths, String subscription) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.filepaths = filepaths;
		this.subscription = subscription;
	}

	public LocalDateTime getTimepoint() {
		return this.timepoint;
	}

	public String getSender() {
		return this.sender;
	}

	public List<Path> getFilepaths() {
		return this.filepaths;
	}

	public String getSubscription() {
		return this.subscription;
	}
}
