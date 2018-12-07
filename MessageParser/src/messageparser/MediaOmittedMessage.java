package messageparser;

import java.util.Calendar;
import java.util.List;

public class MediaOmittedMessage implements IMessage {

	public Calendar timepoint;
	public String sender;
	public List<String> relpaths;

	public MediaOmittedMessage(Calendar timepoint, String sender,
			List<String> relpaths) {
		this.timepoint = timepoint;
		this.sender = sender;
		this.relpaths = relpaths;
	}

	public Calendar getTimepoint() {
		return this.timepoint;
	}

	public String getSender() {
		return this.sender;
	}

	public List<String> getRelpaths() {
		return this.relpaths;
	}
}
