package messageparser;

import java.time.LocalDateTime;
import java.util.List;

public class MediaOmittedMessage implements IMessage {

	public LocalDateTime timepoint;
	public String sender;
	public List<String> relpaths;

	public MediaOmittedMessage(LocalDateTime timepoint, String sender,
			List<String> relpaths) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.relpaths = relpaths;
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
}
