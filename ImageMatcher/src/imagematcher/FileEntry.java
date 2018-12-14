package imagematcher;

import helper.FileHandler;
import helper.XmlUtils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FileEntry {
	private String relPath;
	private String fileName;
	private LocalDateTime timePoint;

	public FileEntry() {
	}

	public FileEntry(String fullpath, String prefix) throws ParseException {
		if (!fullpath.startsWith(prefix)) {
			throw new IllegalArgumentException(
					String.format("Fullpath '%s' does not start with '%s'", fullpath, prefix));
		}

		this.relPath = fullpath.substring(prefix.length() + 1);
		this.fileName = FileHandler.getFileName(fullpath);
		this.timePoint = getTimePointFromFilename(this.fileName);
	}

	public String getRelPath() {
		return relPath;
	}

	public void setRelPath(String relPath) {
		this.relPath = relPath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public LocalDateTime getTimePoint() {
		return timePoint;
	}

	public void setTimePoint(LocalDateTime timePoint) {
		this.timePoint = timePoint;
	}

	private LocalDateTime getTimePointFromFilename(String filename) throws ParseException {

		String pattern = "IMG-[0-9]{8}-WA[0-9]{4}.jp";
		// TODO check the filename starts with pattern
		if (!filename.matches(pattern)) {
			throw new IllegalArgumentException(String.format("Invalid filename '%s'", filename));
		}

		String dateStr = filename.substring(4, 8);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);

		return dateTime;
	}

	public static FileEntry fromNode(Node node) throws ParseException {
		FileEntry fe = new FileEntry();

		NodeList nodeList = node.getChildNodes();

		String tpStr = XmlUtils.GetTextNode(nodeList, "Timepoint");
		fe.timePoint = LocalDateTime.parse(tpStr);

		fe.fileName = XmlUtils.GetTextNode(nodeList, "Filename");
		fe.relPath = XmlUtils.GetTextNode(nodeList, "Relpath");

		return fe;
	}

	public Element getNode(Document doc) {
		Element fileEntry = doc.createElement("FileEntry");

		Element timepoint = doc.createElement("Timepoint");
		timepoint.setTextContent(this.timePoint.toString());
		fileEntry.appendChild(timepoint);

		Element filename = doc.createElement("Filename");
		filename.setTextContent(this.fileName);
		fileEntry.appendChild(filename);

		Element relpath = doc.createElement("Relpath");
		relpath.setTextContent(this.relPath);
		fileEntry.appendChild(relpath);

		return fileEntry;
	}
}
