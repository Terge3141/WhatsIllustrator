package imagematcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import helper.FileHandler;
import helper.Misc;

public class ImageMatcher {

	private List<MatchEntry> matchList;
	private List<FileEntry> fileList;

	private boolean searchMode;

	public ImageMatcher() {
		this.matchList = new ArrayList<MatchEntry>();
	}

	public static ImageMatcher fromXmlString(String xml)
			throws ParserConfigurationException, SAXException, IOException, ParseException {
		ImageMatcher im = new ImageMatcher();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));
		Document doc = dBuilder.parse(stream);
		Element root = doc.getDocumentElement();
		NodeList nodeList = root.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getNodeName().equals("MatchEntry")) {
					im.matchList.add(MatchEntry.fromNode(node));
				}
			}
		}

		return im;
	}

	public static ImageMatcher fromXmlFile(String path)
			throws ParserConfigurationException, SAXException, IOException, ParseException {
		return fromXmlString(Misc.readAllText(path));
	}

	public String toXmlString() throws ParserConfigurationException, TransformerFactoryConfigurationError,
			TransformerException, IOException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element root = doc.createElement("ArrayOfMatchEntry");

		for (MatchEntry entry : this.matchList) {
			root.appendChild(entry.getNode(doc));
			entry.getNode(doc);
		}

		doc.appendChild(root);

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-16");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
		DOMSource source = new DOMSource(doc);
		StringWriter stringWriter = new StringWriter();
		StreamResult streamResult = new StreamResult(stringWriter);
		transformer.transform(source, streamResult);

		Misc.writeAllText("/tmp/out.xml", stringWriter.toString());

		return stringWriter.toString();
	}

	public void toXmlFile(String path) throws IOException, ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException {
		Misc.writeAllText(path, toXmlString());
	}

	public MatchEntry pick(LocalDateTime timepoint, int cnt) {
		List<MatchEntry> list = find(timepoint, cnt);

		LocalDate tpDate = timepoint.toLocalDate();
		long scnt = list.size();
		if (scnt != 1) {
			if (this.searchMode) {
				Stream<FileEntry> matches = this.fileList.stream().filter(x -> x.getTimePoint().equals(tpDate));
				MatchEntry matchEntry = new MatchEntry(timepoint, matches.collect(Collectors.toList()), cnt);
				this.matchList.add(matchEntry);

				return matchEntry;
			} else {
				throw new IllegalArgumentException(String.format(
						"Invalid number of entries found (%d), 1 expected timepoint %s, cnt %d", scnt, timepoint, cnt));
			}
		}

		MatchEntry matchEntry = list.get(0);
		int fmCnt = matchEntry.getFileMatches().size();
		if (fmCnt > 1) {
			System.out.format("Warning, searchmode is of put more than one entry (%d) found for timepoint %s, cnt %d\n",
					fmCnt, timepoint, cnt);
		}

		return matchEntry;
	}

	public MatchEntry pick(LocalDateTime timepoint) {
		return pick(timepoint, 0);
	}

	// exclude all other possibilities for a given date except the one given
	public void excludeExcept(LocalDateTime timepoint, String relPath, int cnt) {
		List<MatchEntry> list = find(timepoint, cnt);
		int entryCnt = list.size();

		if (entryCnt != 1) {
			throw new IllegalArgumentException(
					String.format("Exactly one match entry expected for timepoint %s and cnt %d but %d found",
							timepoint, cnt, entryCnt));
		}

		MatchEntry matchEntry = list.get(0);
		List<FileEntry> fileMatches = matchEntry.getFileMatches();
		List<FileEntry> query = fileMatches.stream().filter(x -> x.getRelPath().equals(relPath))
				.collect(Collectors.toList());

		// TODO test
		int queryCnt = query.size();
		if (queryCnt != 1) {
			throw new IllegalArgumentException(
					String.format("Exactly one file entry expected for timepoint %s, cnt %d, relpath %s but %d found",
							timepoint, cnt, relPath, queryCnt));
		}

		matchEntry.setFileMatches(Arrays.asList(query.get(0)));
	}

	// excludeStr = timepoint;Sent/relpath;cnt
	public void excludeExcept(String excludeStr) {
		String[] tokens = excludeStr.split(";");
		if (tokens.length != 3) {
			throw new IllegalArgumentException(String.format("Exclude string '%s' has incorrect format", excludeStr));
		}

		LocalDateTime timepoint = LocalDateTime.parse(tokens[0]);
		String relPath = tokens[1];
		int cnt = Integer.parseInt(tokens[2]);
		
		excludeExcept(timepoint, relPath, cnt);
	}

	public void loadFiles(String dir) throws IOException, ParseException {
		List<String> files = FileHandler.listDir(dir, ".*jp.*");
		this.fileList = new ArrayList<FileEntry>();

		for (String file : files) {
			FileEntry entry = new FileEntry(file, dir);

			if (this.fileList.stream().filter(x -> x.getFileName().equals(entry.getFileName())).count() > 0) {
				System.out.format("Skipping %s\n", entry.getRelPath());
			} else {
				this.fileList.add(entry);
			}
		}
	}

	public List<MatchEntry> getMatchList() {
		return matchList;
	}

	public void setMatchList(List<MatchEntry> matchList) {
		this.matchList = matchList;
	}

	public boolean isSearchMode() {
		return searchMode;
	}

	public void setSearchMode(boolean searchMode) {
		this.searchMode = searchMode;
	}

	public List<FileEntry> getFileList() {
		return fileList;
	}

	public void setFileList(List<FileEntry> fileList) {
		this.fileList = fileList;
	}

	private List<MatchEntry> find(LocalDateTime timepoint, int cnt) {
		Stream<MatchEntry> stream = this.matchList.stream()
				.filter(x -> (x.getTimePoint().equals(timepoint) && x.getCnt() == cnt));
		List<MatchEntry> list = stream.collect(Collectors.toList());
		return list;
	}
}
