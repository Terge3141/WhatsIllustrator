package messageparser;

import java.time.LocalDateTime;

public interface IMessage {
	LocalDateTime getTimepoint();
	String getSender();
}
