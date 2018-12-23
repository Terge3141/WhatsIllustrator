package imagematcher;

import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.dom4j.Element;
import org.dom4j.Node;



public class FileEntry {
	private String relPath;
	private String fileName;
	private LocalDate timePoint;

	public FileEntry() {
	}

	public FileEntry(String fullpath, String prefix) throws ParseException {
		if (!fullpath.startsWith(prefix)) {
			throw new IllegalArgumentException(
					String.format("Fullpath '%s' does not start with '%s'", fullpath, prefix));
		}

		this.relPath = fullpath.substring(prefix.length() + 1);
		this.fileName=Paths.get(fullpath).getFileName().toString();
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

	public LocalDate getTimePoint() {
		return timePoint;
	}

	public void setTimePoint(LocalDate timePoint) {
		this.timePoint = timePoint;
	}

	private LocalDate getTimePointFromFilename(String filename) throws ParseException {

		String pattern = "IMG-[0-9]{8}-WA[0-9]{4}.jp.*";
		// TODO check the filename starts with pattern
		if (!filename.matches(pattern)) {
			throw new IllegalArgumentException(String.format("Invalid filename '%s'", filename));
		}

		String dateStr = filename.substring(4, 12);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate dateTime = LocalDate.parse(dateStr, formatter);

		return dateTime;
	}

	public static FileEntry fromNode(Node node) {
		FileEntry fe = new FileEntry();
		
		String tpStr = node.selectSingleNode("Timepoint").getText();
		fe.timePoint = LocalDateTime.parse(tpStr).toLocalDate();

		fe.fileName = node.selectSingleNode( "Filename").getText();
		fe.relPath = node.selectSingleNode( "Relpath").getText();

		return fe;
	}

	public void addNode(Element root) {
		Element fileEntry = root.addElement("FileEntry");

		Element timepoint = fileEntry.addElement("Timepoint");
		timepoint.setText(this.timePoint.atTime(0, 0).toString());

		Element filename = fileEntry.addElement("Filename");
		filename.setText(this.fileName);

		Element relpath = fileEntry.addElement("Relpath");
		relpath.setText(this.relPath);
	}
}
