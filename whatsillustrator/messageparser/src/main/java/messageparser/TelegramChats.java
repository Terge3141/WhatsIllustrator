package messageparser;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TelegramChats {
	public TelegramChat list[];
	
	public TelegramChat getChatByName(String name) {
		for(int i=0; i<list.length; i++) {
			TelegramChat chat = list[i];
			if(name.equalsIgnoreCase(chat.name)) {
				return chat;
			}
		}
		
		String chatNames = Arrays.asList(list)
				.stream()
				.map(chat -> chat.name)
				.filter(x -> x!=null)
				.collect(Collectors.joining(", "));
		
		throw new IllegalArgumentException(String.format("Chat '%s' could not be found. Available chats are: %s", name, chatNames));
	}
}
