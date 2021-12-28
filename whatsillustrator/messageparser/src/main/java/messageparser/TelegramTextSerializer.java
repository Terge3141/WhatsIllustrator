package messageparser;

import java.lang.reflect.Type;
import com.google.gson.*;

public class TelegramTextSerializer implements JsonDeserializer<TelegramText>{

	public TelegramText deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		String text = null;
		if(json.isJsonArray()) {
			JsonArray arr = json.getAsJsonArray();
			// check if it is a link
			if(arr.get(0).isJsonObject()) {
				JsonObject jsonObject = arr.get(0).getAsJsonObject();
				JsonElement typeEl = jsonObject.get("type");
				if(typeEl!=null && "link".equals(typeEl.getAsString())) {			
					JsonElement textEl = jsonObject.get("text");
					if(textEl==null) {
						return new TelegramText("Bad link", false);
					}
					else {
						return new TelegramText(textEl.getAsString(), true);
					}
				}
			}
			text = "";
			for(int i=0; i<arr.size(); i++) {
				JsonElement element = arr.get(i);
				if(element.isJsonPrimitive()) {
					if(!text.equals("")) {
						text = text + "\n";
					}
					
					text = text + element.getAsString();
				}
			}
		} else if(json.isJsonPrimitive()) {
			text = json.getAsString();
		}
		else {
			throw new JsonParseException("Cannot parse");
		}
		
		TelegramText telegramText = new TelegramText();
		telegramText.text = text;
		return telegramText;
	}
}