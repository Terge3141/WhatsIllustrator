package imagematcher;

import helper.XmlUtils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

	public static MatchEntry fromNode(Node node) throws ParseException {
		MatchEntry me = new MatchEntry();

		NodeList nodeList = node.getChildNodes();

		String tpStr = XmlUtils.GetTextNode(nodeList, "Timepoint");
		me.timePoint = LocalDateTime.parse(tpStr);

		me.imageType = Boolean.parseBoolean(XmlUtils.GetTextNode(nodeList, "IsImage"));
		me.cnt = Integer.parseInt(XmlUtils.GetTextNode(nodeList, "Cnt"));

		me.fileMatches = new ArrayList<FileEntry>();
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
		}

		return me;
	}

	public Element getNode(Document doc) {
		Element matchEntry = doc.createElement("MatchEntry");

		Element timepoint = doc.createElement("Timepoint");
		timepoint.setTextContent(this.timePoint.toString());
		matchEntry.appendChild(timepoint);

		Element isimage = doc.createElement("IsImage");
		isimage.setTextContent(this.imageType ? "true" : "false");
		matchEntry.appendChild(isimage);

		Element filematches = doc.createElement("Filematches");
		for (FileEntry fileEntry : fileMatches) {
			filematches.appendChild(fileEntry.getNode(doc));
		}
		matchEntry.appendChild(filematches);

		Element cnt = doc.createElement("Cnt");
		cnt.setTextContent(Integer.toString(this.cnt));
		matchEntry.appendChild(cnt);

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