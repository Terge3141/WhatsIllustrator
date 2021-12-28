package messageparser;

public class TelegramText {
	public String text;
	public boolean link;
	
	public TelegramText() {
	}
	
	public TelegramText(String text, boolean link) {
		this.text = text;
		this.link = link;
	}
	
	public TelegramText(String text) {
		this.text = text;
		this.link = false;
	}
	
	@Override
	public String toString() {
		return text;
	}
}