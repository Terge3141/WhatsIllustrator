package messageparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.google.gson.*;

public class TelegramParser {
	
	private int index = 0;
	private TelegramChat telegramChat;
	
	private static final String JSON_MESSAGE = "message";
	
	public static TelegramParser of(Path messagePath)
			throws IOException {
		String json = Files.readString(messagePath);
		return new TelegramParser(json);
	}
	
	public TelegramParser(String json) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(TelegramText.class, new TelegramTextSerializer());
		
		Gson gson = gsonBuilder.create(); 
		telegramChat = gson.fromJson(json, TelegramChat.class);
	}
	
	public IMessage nextMessage() {
		if(this.index >= this.telegramChat.messages.length) {
			return null;
		}		
		TelegramMessage message = this.telegramChat.messages[this.index];
		this.index++;
		
		LocalDateTime date = null;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-mm-ddTHH:mm:ss");
			date = LocalDateTime.parse(message.date, formatter);
		} catch (DateTimeParseException dtpe) {
			throw new IllegalArgumentException(message.toString());
		}
		
		if(message.type.equals(JSON_MESSAGE)) {
			return new TextMessage(date, message.from, message.text.text);
		}
		else {
			return nextMessage();
		}
	}
}
