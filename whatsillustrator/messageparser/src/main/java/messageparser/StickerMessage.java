package messageparser;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class StickerMessage implements IMessage {
	private LocalDateTime timepoint;
	private String sender;
	private Path filepath;
	
	public StickerMessage(LocalDateTime timepoint, String sender, Path filepath) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.filepath = filepath;
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
}
