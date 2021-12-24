package messageparser;

public class TelegramMessage {
	public int id;
	public String type;
	
	public String date;
	public String duration_seconds;
	public String edited;
	public String file;
	public String forwarded_from;
	public String from;
	public String from_id;
	public String height;
	public String live_location_period_seconds;
	//public String location_information; // TODO
	public String media_type;
	public String mime_type;
	public String photo;
	// TODO
	/*public String reply_to_message_id;
	public String sticker_emoji;*/
	public TelegramText text;
	public String thumbnail;
	public String width;
}