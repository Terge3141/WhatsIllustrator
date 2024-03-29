package messageparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.text.TextStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.gson.*;

import configurator.Global;
import helper.Xml;

public class TelegramParser implements IParser {
	
	private static Logger logger = LogManager.getLogger(TelegramParser.class);
	
	private int index = 0;
	private TelegramChat telegramChat;
	private Path messagePath;
	private Path messageDir;
	private LocalDateTime dtmin;
	private LocalDateTime dtmax; 
	
	private static final String JSON_MESSAGE = "message";
	
	public TelegramParser() {
	}
	
	public void init(String xmlConfig, Global globalConfig) throws ParserException {
		String json;
		String chatName;
		
		GsonBuilder gsonBuilder;
		boolean chatOnly;
		try {
			Document document = Xml.documentFromString(xmlConfig);
			this.messagePath = Xml.getPathFromNode(document, "//messagepath");
			try {
				json = Files.readString(this.messagePath);
			} catch (IOException e) {
				String msg = String.format("Could not read message file '%s'", this.messagePath);
				throw new ParserException(msg, e);
			}
			this.messageDir = messagePath.getParent();
			chatName = Xml.getTextFromNode(document, "//chatname");
			gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(TelegramText.class, new TelegramTextSerializer());
			chatOnly = Xml.getBooleanFromNode(document, "//chatonly");
			
			dtmin = str2Dt(Xml.getTextFromNode(document, "//mindate"));
			dtmax = str2Dt(Xml.getTextFromNode(document, "//maxdate"));
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			throw new ParserException("Could not read xml configuration", e);
		}
		
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
		TelegramMessage message = null;
		LocalDateTime date = null;
		
		boolean found = false;		
		while(!found) {
			
			found = true;
			
			if(this.index >= this.telegramChat.messages.length) {
				return null;
			}		
			
			if(this.index%100==0) {
				logger.info("{}/{} messages parsed", this.index, this.telegramChat.messages.length);
			}
			
			message = this.telegramChat.messages[this.index];
			this.index++;
			
			try {
				String datestr = message.date;
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				date = LocalDateTime.parse(datestr, formatter);
			} catch (DateTimeParseException dtpe) {
				throw new IllegalArgumentException(message.date);
			}
			
			if(dtmin!=null && date.isBefore(dtmin)) {
				found = false;
			}
			
			if(dtmax!=null && date.isAfter(dtmax)) {
				found = false;
			}
		}
			
		String from = message.from;
		//from = from + ": " + message.id;
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
				return new TextMessage(date, from, text + " (attached PDF document)");
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
	
	private LocalDateTime str2Dt(String dateStr) {
		if(dateStr==null) {
			return null;
		}
		
		return LocalDateTime.parse(dateStr);
	}
}
