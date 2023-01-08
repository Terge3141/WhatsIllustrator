package imagematcher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles a list of possible file matches for a given date and time
 * 
 * @author Michael Elvers
 *
 */
public class MatchEntry {
	private LocalDateTime timePoint;
	private List<FileEntry> fileMatches;
	private int cnt;
	private boolean imageType;

	/**
	 * Constructor
	 * 
	 * @param timepoint   Date and time of the message entry
	 * @param fileMatches List of possible matching files
	 * @param cnt         Instance count, e.g. if two match entries exist for the
	 *                    timepoint, first one has cnt=0, second one has cnt=1
	 */
	public MatchEntry(LocalDateTime timepoint, List<FileEntry> fileMatches, int cnt) {
		this.timePoint = timepoint;
		this.fileMatches = fileMatches;
		this.cnt = cnt;
		this.imageType = true;
	}

	private MatchEntry() {
	}

	/**
	 * Creates a MatchEntry object for a given xml node.
	 * @param node Node containing the match entry information
	 * @return The created MatchEntry
	 * @throws XPathExpressionException 
	 */
	public static MatchEntry fromNode(Node node) throws XPathExpressionException {
		MatchEntry me = new MatchEntry();

		String tpStr = getTextFromNode(node, "Timepoint");
		me.timePoint = LocalDateTime.parse(tpStr);

		me.imageType = Boolean.parseBoolean(getTextFromNode(node, "IsImage"));
		me.cnt = Integer.parseInt(getTextFromNode(node, "Cnt"));

		me.fileMatches = new ArrayList<FileEntry>();
		Node fileMatchesNode = selectNode(node, "Filematches");
		NodeList fileEntryNodes = selectNodes(fileMatchesNode, "FileEntry");
		
		for (int i=0; i<fileEntryNodes.getLength(); i++) {
			Node fileEntryNode = fileEntryNodes.item(i);
			me.fileMatches.add(FileEntry.fromNode(fileEntryNode));
		}

		return me;
	}
	
	private static NodeList selectNodes(Node node, String xPathExpression) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		return (NodeList)xpath.compile(xPathExpression).evaluate(node, XPathConstants.NODESET);
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
		Element matchEntry = doc.createElement("MatchEntry");
		root.appendChild(matchEntry);

		addTextElement(matchEntry, "Timepoint", this.timePoint.toString());

		addTextElement(matchEntry, "IsImage", this.imageType ? "true" : "false");

		Element filematches = doc.createElement("Filematches");
		matchEntry.appendChild(filematches);
		for (FileEntry fileEntry : this.fileMatches) {
			fileEntry.addNode(filematches);
		}

		addTextElement(matchEntry, "Cnt", Integer.toString(this.cnt));
	}
	
	private void addTextElement(Element el, String name, String value) {
		Document doc = el.getOwnerDocument();
		Element te = doc.createElement(name);
		te.setTextContent(value);
		el.appendChild(te);
	}

	public LocalDateTime getTimePoint() {
		return timePoint;
	}

	public int getCnt() {
		return cnt;
	}

	public List<FileEntry> getFileMatches() {
		return fileMatches;
	}

	public void setFileMatches(List<FileEntry> fileMatches) {
		this.fileMatches = fileMatches;
	}

	public boolean isImageType() {
		return this.imageType;
	}
}