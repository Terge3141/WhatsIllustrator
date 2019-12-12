package imagematcher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

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
	 */
	public static MatchEntry fromNode(Node node) {
		MatchEntry me = new MatchEntry();

		String tpStr = node.selectSingleNode("Timepoint").getText();
		me.timePoint = LocalDateTime.parse(tpStr);

		me.imageType = Boolean.parseBoolean(node.selectSingleNode("IsImage").getText());
		me.cnt = Integer.parseInt(node.selectSingleNode("Cnt").getText());

		me.fileMatches = new ArrayList<FileEntry>();
		Node fileMatchesNode = node.selectSingleNode("Filematches");
		for (Node fileEntryNode : fileMatchesNode.selectNodes("FileEntry")) {
			me.fileMatches.add(FileEntry.fromNode(fileEntryNode));
		}

		return me;
	}

	/**
	 * Adds the object information to a given root node
	 * @param root The root node
	 */
	public Element addNode(Element root) {
		Element matchEntry = root.addElement("MatchEntry");

		Element timepoint = matchEntry.addElement("Timepoint");
		timepoint.addText(this.timePoint.toString());

		Element isimage = matchEntry.addElement("IsImage");
		isimage.addText(this.imageType ? "true" : "false");

		Element filematches = matchEntry.addElement("Filematches");
		for (FileEntry fileEntry : fileMatches) {
			fileEntry.addNode(filematches);
		}

		Element cnt = matchEntry.addElement("Cnt");
		cnt.addText(Integer.toString(this.cnt));

		return matchEntry;
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