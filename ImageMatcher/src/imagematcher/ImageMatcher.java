package imagematcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ImageMatcher {

	private List<MatchEntry> matchList;

	private boolean searchMode;

	public ImageMatcher() {
		this.matchList = new ArrayList<MatchEntry>();
	}

	public static ImageMatcher FromXml(String xml)
			throws ParserConfigurationException, SAXException, IOException,
			ParseException {
		ImageMatcher im = new ImageMatcher();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		InputStream stream = new ByteArrayInputStream(
				xml.getBytes(StandardCharsets.UTF_8));
		Document doc = dBuilder.parse(stream);
		Element root = doc.getDocumentElement();
		NodeList nodeList = root.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getNodeName().equals("MatchEntry")) {
					System.out.println(node.getNodeName());
					im.matchList.add(MatchEntry.fromNode(node));
				}
			}
		}

		return im;
	}

	public MatchEntry Pick(Calendar timepoint, int cnt) {
		List<MatchEntry> matches = new ArrayList<>();
		Iterator<MatchEntry> it = matchList.iterator();
		while (it.hasNext()) {
			MatchEntry entry = it.next();
			if (entry.getTimePoint().equals(timepoint) && entry.getCnt() == cnt) {
				matches.add(entry);
			}
		}

		if (matches.size() != 1) {
			if (searchMode) {
				/*
				 * MatchEntry matchEntry=new MatchEntry(timepoint, matches,
				 * cnt); this.matchList.add(matchEntry); return matchEntry;
				 */
			}
		}
		/*
		 * var query = _matchList.Where(x => (x.Timepoint.Equals(timepoint) &&
		 * x.Cnt == cnt)); int qcnt = query.Count(); if (qcnt != 1) { if
		 * (SearchMode) { var matches = FileList.Where(x =>
		 * DateEqual(x.Timepoint, timepoint)).ToList(); var matchEntry = new
		 * MatchEntry(timepoint, matches, cnt); _matchList.Add(matchEntry);
		 * return matchEntry; } else { throw new
		 * ArgumentException($"Invalid number of entries found ({qcnt}), 1 expected"
		 * ); } }
		 */

		// return query.First();
		throw new UnsupportedOperationException();
	}

	public MatchEntry Pick(Calendar timepoint) {
		return Pick(timepoint, 0);
	}

	public void LoadMatches(String todo) {
		throw new UnsupportedOperationException();
	}

	public void LoadFiles(String todo) {
		throw new UnsupportedOperationException();
	}

	public void Save(String path) {
		// throw new UnsupportedOperationException();
	}

}
