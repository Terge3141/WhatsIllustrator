package messageparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.text.TextStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.*;

public class TelegramParser {
	
	private static Logger logger = LogManager.getLogger(TelegramParser.class);
	
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
		
		if(this.index%100==0) {
			logger.info("{}/{} messages parsed", this.index, this.telegramChat.messages.length);
		}
		
		TelegramMessage message = this.telegramChat.messages[this.index];
		this.index++;
		
		LocalDateTime date = null;
		try {
			// TODO: Replace dirty hack
			String datestr = message.date.replace('T', ' ');
			//System.out.println(datestr);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			date = LocalDateTime.parse(datestr, formatter);
			//System.out.println(date);
		} catch (DateTimeParseException dtpe) {
			throw new IllegalArgumentException(message.date);
		}
		
		String from = message.from;
		from = from + ": " + message.id;
		String text = message.text.text;
		
		if(message.type.equals(JSON_MESSAGE)) {
			if(message.photo!=null) {
				return new ImageMessage(date, from, message.photo, text);
			}
			else if("sticker".equals(message.media_type)) {
				// This will be replaced by the actual sticker once https://github.com/haraldk/TwelveMonkeys
				// supports alpha for webp images
				return new TextMessage(date, from, message.sticker_emoji);
			}
			else if("image/jpeg".equals(message.mime_type)) {
				return new ImageMessage(date, from, message.file, text);
			}
			else if("video_file".equals(message.media_type) || "animation".equals(message.media_type)) {
				if(message.thumbnail!=null) {
					return new ImageMessage(date, from, message.thumbnail, text);
				}
				else {
					return new TextMessage(date, from, "video - no thumbnail available");
				}
			}
			else if("voice_message".equals(message.media_type)) {
				return new TextMessage(date, from, "voice message of " + message.duration_seconds + "s");
			}
			else if("audio_file".equals(message.media_type)) {
				return new TextMessage(date, from, "audio file of " + message.duration_seconds + "s");
			}
			else if("video_message".equals(message.media_type)) {
				return new ImageMessage(date, from, message.thumbnail, text);
			}
			else if("application/pdf".equals(message.mime_type)) {
				return new ImageMessage(date, from, message.thumbnail, text);
			}
			else if(message.location_information!=null) {
				TextStringBuilder sb = new TextStringBuilder();
				sb.appendln("Latitude: %s", message.location_information.latitude);
				sb.appendln("Longitude: %s", message.location_information.longitude);
				return new TextMessage(date, from, sb.toString());
			}
			else {
				if("".equals(text)) {
					logger.info("Text message has empty text, id {}", message.id);
				}
				return new TextMessage(date, from, text);
			}
		}
		else {
			return nextMessage();
		}
	}
}
