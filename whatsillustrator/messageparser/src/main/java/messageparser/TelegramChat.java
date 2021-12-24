package messageparser;

public class TelegramChat {
	public String name;
	public String type;
	public int id;
	
	public TelegramMessage messages[];
	
	public void print() {
		System.out.println("Name: " + name);
		System.out.println("Type: " + type);
		System.out.println("Id: " + id);
		int msgcnt = 0;
		if(messages!=null) {
			msgcnt=messages.length;
		}
		System.out.println("Msgcnt: " + msgcnt);
	}
}