package creator.plugins.fop;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import helper.DateUtils;
import messageparser.ImageMessage;
import messageparser.MediaOmittedMessage;

@XmlRootElement(name = "imagemessage")
public class FOPImageMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement(name = "time")
	private String timepoint;

	@XmlElement(name = "sender")
	private String sender;

	@XmlElement(name = "src")
	private String src;

	@XmlElement(name = "subscription")
	private String subscription;

	public FOPImageMessage() {
	}
	
	public static FOPImageMessage of(ImageMessage message, DateUtils dateUtils, Path imagedir) {
		FOPImageMessage fopMessage = new FOPImageMessage();
		fopMessage.timepoint = dateUtils.formatTimeString(message.getTimepoint());
		fopMessage.sender = message.getSender();
		fopMessage.src = imagedir.resolve(message.getFilename()).toString();
		fopMessage.subscription = message.getSubscription();
		
		return fopMessage;
	}
	
	public static List<FOPImageMessage> of(MediaOmittedMessage message, DateUtils dateUtils, Path imagedir){
		List<FOPImageMessage> list=new ArrayList<FOPImageMessage>();
		for(String relPath:message.getRelpaths()) {
			FOPImageMessage fopMessage = new FOPImageMessage();
			fopMessage.timepoint = dateUtils.formatTimeString(message.getTimepoint());
			fopMessage.sender = message.getSender();
			fopMessage.src=imagedir.resolve(relPath).toString();
			
			list.add(fopMessage);
		}
		
		return list;
	}
}
