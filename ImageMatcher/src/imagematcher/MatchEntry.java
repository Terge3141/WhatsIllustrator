package imagematcher;

import helper.XmlUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MatchEntry {
	private Calendar timePoint;
	private List<FileEntry> fileMatches;
	private int cnt;
	private boolean isImage;

	private MatchEntry() {
	}

	public MatchEntry(Calendar timepoint, List<FileEntry> fileMatches, int cnt) {
		this.timePoint = timepoint;
		this.fileMatches = fileMatches;
		this.cnt = cnt;
		this.isImage = true;
	}

	public static MatchEntry fromNode(Node node) throws ParseException {
		MatchEntry me = new MatchEntry();

		NodeList nodeList = node.getChildNodes();

		String tpStr = XmlUtils.GetTextNode(nodeList, "Timepoint");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss");
		me.timePoint = Calendar.getInstance();
		me.timePoint.setTime(sdf.parse(tpStr));

		me.isImage = Boolean.parseBoolean(XmlUtils.GetTextNode(nodeList,
				"IsImage"));
		me.cnt = Integer.parseInt(XmlUtils.GetTextNode(nodeList, "Cnt"));

		me.fileMatches = new ArrayList<FileEntry>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node subNode = nodeList.item(i);
			if (subNode.getNodeType() == Node.ELEMENT_NODE) {
				if (subNode.getNodeName().equals("FileEntry")) {
					System.out.println(subNode.getNodeName());
					me.fileMatches.add(FileEntry.fromNode(subNode));
				}
			}
		}

		return me;
	}

	public Calendar getTimePoint() {
		return timePoint;
	}

	public int getCnt() {
		return cnt;
	}
}