package messageparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.text.TextStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.google.gson.*;

import configurator.Global;

public class TelegramParser implements IParser {
	
	private static Logger logger = LogManager.getLogger(TelegramParser.class);
	
	private int index = 0;
	private TelegramChat telegramChat;
	private Path messagePath;
	private Path messageDir;
	
	private static final String JSON_MESSAGE = "message";
	
	public TelegramParser() {
	}
	
	public void init(String xmlConfig, Global globalConfig) throws IOException, DocumentException {
		SAXReader reader = new SAXReader();
		InputStream stream = new ByteArrayInputStream(xmlConfig.getBytes(StandardCharsets.UTF_16));
		Document document = reader.read(stream);
		
		this.messagePath = Paths.get(document.selectSingleNode("//messagepath").getStringValue());
		String json = Files.readString(this.messagePath);
		this.messageDir = messagePath.getParent();
		
		String chatName = document.selectSingleNode("//chatname").getStringValue();
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(TelegramText.class, new TelegramTextSerializer());
		
		boolean chatOnly = Boolean.parseBoolean(document.selectSingleNode("//chatonly").getStringValue());
		Gson gson = gsonBuilder.create();
		if(chatOnly) {
			this.telegramChat = gson.fromJson(json, TelegramChat.class);
		}
		else {
			TelegramResult telegramResult = gson.fromJson(json, TelegramResult.class);
			this.telegramChat = telegramResult.chats.getChatByName(chatName);
		}
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
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			date = LocalDateTime.parse(datestr, formatter);
		} catch (DateTimeParseException dtpe) {
			throw new IllegalArgumentException(message.date);
		}
		
		String from = message.from;
		from = from + ": " + message.id;
		String text = message.text.text;
		
		if(message.type.equals(JSON_MESSAGE)) {
			if(message.photo!=null) {
				return new ImageMessage(date, from, fullPath(message.photo), text);
			}
			else if("sticker".equals(message.media_type)) {
				// This will be replaced by the actual sticker once https://github.com/haraldk/TwelveMonkeys
				// supports alpha for webp images
				return new TextMessage(date, from, message.sticker_emoji);
			}
			else if("image/jpeg".equals(message.mime_type)) {
				return new ImageMessage(date, from, fullPath(message.file), text);
			}
			else if("video_file".equals(message.media_type) || "animation".equals(message.media_type)) {
				return new VideoMessage(date, from, fullPath(message.file), text);
			}
			else if("voice_message".equals(message.media_type)) {
				return new TextMessage(date, from, "voice message of " + message.duration_seconds + "s");
			}
			else if("audio_file".equals(message.media_type)) {
				return new TextMessage(date, from, "audio file of " + message.duration_seconds + "s");
			}
			else if("video_message".equals(message.media_type)) {
				return new ImageMessage(date, from, fullPath(message.thumbnail), text);
			}
			else if("application/pdf".equals(message.mime_type)) {
				return new ImageMessage(date, from, fullPath(message.thumbnail), text);
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
	
	public String getNameSuggestion() {
		return "Telegram " + this.telegramChat.name;
	}
	
	private Path fullPath(String relativePath) {
		return this.messageDir.resolve(relativePath);
	}
}
