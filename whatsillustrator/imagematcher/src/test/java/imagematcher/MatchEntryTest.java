package imagematcher;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class MatchEntryTest {

	@Test
	void testFromNode() {
		String xml = TestHelper.createMatchEntryXmlString(2023, 1, 7, 15, 21);
		
		MatchEntry matchEntry = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			matchEntry = MatchEntry.fromNode(document.getDocumentElement());
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			fail(e);
		}
		
		LocalDateTime dtExpected = LocalDateTime.of(2023, 1, 7, 15, 21);
		assertEquals(dtExpected, matchEntry.getTimePoint());
		
		assertTrue(matchEntry.isImageType());
		assertEquals(23, matchEntry.getCnt());
		
		List<FileEntry> fes = matchEntry.getFileMatches();
		assertEquals(2, fes.size());

		// only smoke tests, detailed tests are done in FileEntry unit tests
		assertEquals(LocalDate.of(2023, 1, 6), fes.get(0).getTimePoint());
		assertEquals(LocalDate.of(2023, 1, 7), fes.get(1).getTimePoint());
	}

	@Test
	void testAddNode() {
		List<FileEntry> fesInput = new ArrayList<FileEntry>();
		try {
			fesInput.add(new FileEntry("asd/fgh/IMG-20230107-WA0000.jpg", "asd"));
			fesInput.add(new FileEntry("asd/fgh/IMG-20230107-WA0001.jpg", "asd"));
		} catch (ParseException e) {
			fail(e);
		}
		
		LocalDateTime dt = LocalDateTime.of(2023, 1, 7, 15, 47);
		
		MatchEntry matchEntry = new MatchEntry(dt, fesInput, 2);
		
		DocumentBuilder builder = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			fail(e);
		}
		Document document = builder.newDocument();
		Element root = document.createElement("Parent");
		document.appendChild(root);
		matchEntry.addNode(root);
		
		TestHelper.checkStringNode(root, "/Parent/MatchEntry/Timepoint", "2023-01-07T15:47");
		TestHelper.checkStringNode(root, "/Parent/MatchEntry/IsImage", "true");
		TestHelper.checkStringNode(root, "/Parent/MatchEntry/Cnt", "2");
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList fes = null;
		try {
			fes = (NodeList)xpath.compile("/Parent/MatchEntry/Filematches/FileEntry").evaluate(root, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			fail(e);
		}
		// only smoke tests, detailed tests are done in FileEntry unit tests
		assertEquals(2, fes.getLength());
	}
}
