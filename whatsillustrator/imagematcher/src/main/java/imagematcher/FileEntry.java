package imagematcher;

import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Contains information for a single image file.
 * 
 * @author Michael Elvers
 *
 */
public class FileEntry {
	private String relPath;
	private String fileName;
	private LocalDate timePoint;

	/**
	 * Constructor
	 * 
	 * @param fullpath The full path to the image. The timepoint is determined from
	 *                 the filename.
	 * @param imagedir The directory where the images are found
	 * @throws ParseException If the image filename does not contain a correct name
	 *                        information
	 */
	public FileEntry(String fullpath, String imagedir) throws ParseException {
		if (!fullpath.startsWith(imagedir)) {
			throw new IllegalArgumentException(
					String.format("Fullpath '%s' does not start with '%s'", fullpath, imagedir));
		}

		this.relPath = fullpath.substring(imagedir.length() + 1);
		this.fileName = Paths.get(fullpath).getFileName().toString();
		this.timePoint = getTimePointFromFilename(this.fileName);
	}

	public FileEntry() {
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

	/**
	 * Creates a FileEntry object for a given xml node.
	 * @param node Node containing the file entry information
	 * @return The created FileEntry
	 * @throws XPathExpressionException 
	 */
	public static FileEntry fromNode(Node node) throws XPathExpressionException {
		FileEntry fe = new FileEntry();

		String tpStr = getTextFromNode(node, "Timepoint");
		fe.timePoint = LocalDateTime.parse(tpStr).toLocalDate();

		fe.fileName = getTextFromNode(node, "Filename");
		fe.relPath = getTextFromNode(node, "Relpath");

		return fe;
	}
	
	private static Node selectNode(Node node, String xPathExpression) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		return (Node)xpath.compile(xPathExpression).evaluate(node, XPathConstants.NODE);
	}
	
	private static String getTextFromNode(Node parent, String xPathExpression) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		Node node = (Node)xpath.compile(xPathExpression).evaluate(parent, XPathConstants.NODE);
		return node.getTextContent();
	}

	/**
	 * Adds the object information to a given root node
	 * @param root The root node
	 */
	public void addNode(Element root) {
		Document doc = root.getOwnerDocument();
		Element fileEntry = doc.createElement("FileEntry");

		addTextElement(fileEntry, "Timepoint", this.timePoint.atTime(0, 0).toString());
		addTextElement(fileEntry, "Filename", this.fileName);
		addTextElement(fileEntry, "Relpath", this.relPath);
	}
	
	private void addTextElement(Element el, String name, String value) {
		Document doc = el.getOwnerDocument();
		Element te = doc.createElement(name);
		te.setTextContent(value);
		el.appendChild(te);
	}
	
	private LocalDate getTimePointFromFilename(String filename) throws ParseException {

		String pattern = "^IMG-[0-9]{8}-WA[0-9]{4}.jp.*";
		if (!filename.matches(pattern)) {
			throw new IllegalArgumentException(String.format("Invalid filename '%s'", filename));
		}

		String dateStr = filename.substring(4, 12);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate dateTime = LocalDate.parse(dateStr, formatter);

		return dateTime;
	}
}
