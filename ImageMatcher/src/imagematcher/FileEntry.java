package imagematcher;

import helper.FileHandler;
import helper.XmlUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FileEntry {
	private String relPath;
	private String fileName;
	private Calendar timePoint;
	
	public FileEntry(){
	}

	public FileEntry(String fullpath, String prefix) throws ParseException {
		if (!fullpath.startsWith(prefix)) {
			throw new IllegalArgumentException(String.format(
					"Fullpath '%s' does not start with '%s'", fullpath,
					prefix));
		}

		this.relPath = fullpath.substring(prefix.length() + 1);
		this.fileName = FileHandler.getFileName(fullpath);
		this.timePoint = GetTimepoint(this.fileName);
	}

	public String getRelPath() {
		return relPath;
	}

	public String getFileName() {
		return fileName;
	}

	public Calendar getTimePoint() {
		return timePoint;
	}

	private Calendar GetTimepoint(String filename) throws ParseException {

		String pattern = "IMG-[0-9]{8}-WA[0-9]{4}.jp";
		// TODO check the filename starts with pattern
		if (!filename.matches(pattern)) {
			throw new IllegalArgumentException(String.format(
					"Invalid filename '%s'", filename));
		}

		String dateStr = filename.substring(4, 8);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sdf.parse(dateStr));

		return calendar;
	}

	public static FileEntry fromNode(Node node) throws ParseException {
		FileEntry fe=new FileEntry();
		
		NodeList nodeList=node.getChildNodes();
		
		String tpStr = XmlUtils.GetTextNode(nodeList, "Timepoint");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss");
		fe.timePoint=Calendar.getInstance();
		fe.timePoint.setTime(sdf.parse(tpStr));
		
		fe.fileName=XmlUtils.GetTextNode(nodeList, "Filename");
		fe.relPath=XmlUtils.GetTextNode(nodeList, "Relpath");
		
		return fe;
	}
}
