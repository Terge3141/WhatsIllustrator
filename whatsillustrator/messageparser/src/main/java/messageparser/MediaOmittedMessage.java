package messageparser;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class MediaOmittedMessage implements IMessage {

	private LocalDateTime timepoint;
	private String sender;
	private List<Path> abspaths;
	private int cnt;

	public MediaOmittedMessage(LocalDateTime timepoint, String sender, List<Path> abspaths, int cnt) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.abspaths = abspaths;
		this.cnt = cnt;
	}

	public LocalDateTime getTimepoint() {
		return this.timepoint;
	}

	public String getSender() {
		return this.sender;
	}

	public List<Path> getAbspaths() {
		return this.abspaths;
	}

	public int getCnt() {
		return this.cnt;
	}
}
