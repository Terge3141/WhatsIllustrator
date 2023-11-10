package creator;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import messageparser.IMessage;
import messageparser.ImageMessage;
import messageparser.ImageStackMessage;

public class ImageMessageConcatenator {
	
	private List<ImageMessage> imList;
	private long maxTimeDifferenceSeconds = 0;
	
	public ImageMessageConcatenator() {
		resetImList();
	}
	
	public ImageMessageConcatenator(long maxTimeDifferenceSeconds) {
		this();
		this.maxTimeDifferenceSeconds = maxTimeDifferenceSeconds;
	}
	
	public List<IMessage> addMessage(IMessage msg) {
		List<IMessage> list = new ArrayList<IMessage>();

		if(msg instanceof ImageMessage) {
			ImageMessage im = (ImageMessage)msg;
			
			if(imList.size()==0) {
				imList.add(im);
			} else {
				ImageMessage ref = imList.get(0);
				if(timeDifferenceOkay(ref.getTimepoint(), im.getTimepoint())
						&& ref.getSender().equals(im.getSender())
						&& ref.getSubscription().equals(im.getSubscription())) {
					imList.add(im);
				} else {
					list = stackImages();
					imList.add(im);
				}
			}
		} else {
			list = stackImages();
			list.add(msg);
		}		
		
		return list;
	}
	
	public List<IMessage> flush() {
		return stackImages();
	}
	
	// Create one image stack message out of all image messages
	private List<IMessage> stackImages() {
		List<IMessage> list = new ArrayList<IMessage>();
		
		if(imList.size() == 1) {
			list.add(imList.get(0));
			
			imList = new ArrayList<ImageMessage>();
		} else if(imList.size() > 0) {
			LocalDateTime tp = imList.get(0).getTimepoint();
			String sender = imList.get(0).getSender();
			String subscription = imList.get(0).getSubscription();
			
			// TODO use streams?
			List<Path> filepaths = new ArrayList<Path>();
			for (ImageMessage im : imList) {
				filepaths.add(im.getFilepath());
			}
			
			ImageStackMessage ism = new ImageStackMessage(tp, sender, filepaths, subscription);
			list.add(ism);
			
			resetImList();		
		}
		
		return list;
	}
	
	private boolean timeDifferenceOkay(LocalDateTime tp1, LocalDateTime tp2) {
		long diff = tp1.until(tp2, ChronoUnit.SECONDS);
		return diff <= maxTimeDifferenceSeconds;
	}
	
	private void resetImList() {
		imList = new ArrayList<ImageMessage>();
	}
}
