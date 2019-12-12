package messageparser;

import java.time.LocalDateTime;
import java.util.List;

public class MediaOmittedMessage implements IMessage {

	private LocalDateTime timepoint;
	private String sender;
	private List<String> relpaths;
	private int cnt;

	public MediaOmittedMessage(LocalDateTime timepoint, String sender, List<String> relpaths, int cnt) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.relpaths = relpaths;
		this.cnt = cnt;
	}

	public LocalDateTime getTimepoint() {
		return this.timepoint;
	}

	public String getSender() {
		return this.sender;
	}

	public List<String> getRelpaths() {
		return this.relpaths;
	}

	public int getCnt() {
		return this.cnt;
	}
}
