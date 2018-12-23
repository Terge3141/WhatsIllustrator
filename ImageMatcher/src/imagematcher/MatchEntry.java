package imagematcher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

public class MatchEntry {
	private LocalDateTime timePoint;
	private List<FileEntry> fileMatches;
	private int cnt;
	private boolean imageType;

	private MatchEntry() {
	}

	public MatchEntry(LocalDateTime timepoint, List<FileEntry> fileMatches, int cnt) {
		this.timePoint = timepoint;
		this.fileMatches = fileMatches;
		this.cnt = cnt;
		this.imageType = true;
	}

	public static MatchEntry fromNode(Node node) {
		MatchEntry me = new MatchEntry();
		
		String tpStr=node.selectSingleNode("Timepoint").getText();
		me.timePoint=LocalDateTime.parse(tpStr);
		
		me.imageType=Boolean.parseBoolean(node.selectSingleNode("IsImage").getText());
		me.cnt=Integer.parseInt(node.selectSingleNode("Cnt").getText());
		
		me.fileMatches = new ArrayList<FileEntry>();
		Node fileMatchesNode=node.selectSingleNode("Filematches");
		for(Node fileEntryNode : fileMatchesNode.selectNodes("FileEntry")) {
			me.fileMatches.add(FileEntry.fromNode(fileEntryNode));
		}

		/*me.fileMatches = new ArrayList<FileEntry>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node subNode = nodeList.item(i);
			if (subNode.getNodeType() == Node.ELEMENT_NODE) {
				if (subNode.getNodeName().equals("Filematches")) {
					NodeList subNodeList = subNode.getChildNodes();
					for (int j = 0; j < subNodeList.getLength(); j++) {
						Node subSubNode = subNodeList.item(j);
						if (subSubNode.getNodeType() == Node.ELEMENT_NODE) {
							if (subSubNode.getNodeName().equals("FileEntry")) {
								me.fileMatches.add(FileEntry.fromNode(subSubNode));
							}
						}
					}
				}
			}
		}*/

		return me;
	}

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

	public String toXml() {
		throw new UnsupportedOperationException();
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