package imagematcher;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class FileEntryTest {

	@Test
	void testFileEntryStringString() {
		FileEntry fileEntry = null;
		try {
			fileEntry = new FileEntry("asd/fgh/IMG-20230107-WA1234.jpg", "asd");
		} catch (ParseException e) {
			fail("No exception expected");
		}
		
		assertEquals("fgh/IMG-20230107-WA1234.jpg", fileEntry.getRelPath());
		
		assertEquals("IMG-20230107-WA1234.jpg", fileEntry.getFileName());
		
		LocalDate dateExpected = LocalDate.of(2023, 01, 07);
		assertEquals(dateExpected, fileEntry.getTimePoint());
	}
	
	@Test
	void testFileEntryStringStringIncorrectPath() {
		try {
			new FileEntry("asd/fgh", "asf");
			fail("Exception expected");
		} catch (ParseException e) {
			fail("No ParseException expected");
		} catch (IllegalArgumentException e) {
		}
	}
	
	@Test
	void testFileEntryStringStringIncorrectFilename() {
		try {
			new FileEntry("asd/fgh.jpg", "asd");
			fail("Exception expected");
		} catch (ParseException e) {
			fail("No ParseException expected");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	void testGetSetRelPath() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setRelPath("asdfgh");
		assertEquals("asdfgh", fileEntry.getRelPath());
	}

	@Test
	void testGetSetFileName() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setFileName("bla.jpg");
		assertEquals("bla.jpg", fileEntry.getFileName());
	}

	@Test
	void testGetSetTimePoint() {
		LocalDate dateExpected = LocalDate.of(2023, 1, 7);
		FileEntry fileEntry = new FileEntry();
		fileEntry.setTimePoint(dateExpected);
		assertEquals(dateExpected, fileEntry.getTimePoint());
	}

	@Test
	void testFromNode() {
		String xml = ""
				+ "<FileEntry>"
				+ "<Timepoint>2023-01-07T00:00</Timepoint>"
				+ "<Filename>IMG-20120804-WA0000.jpg</Filename>"
				+ "<Relpath>asd/fgh/IMG-20120804-WA0000.jpg</Relpath>"
				+ "</FileEntry>";

		FileEntry fileEntry = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			fileEntry = FileEntry.fromNode(document);
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			fail(e);
		} 
		
		LocalDate dateExpected = LocalDate.of(2023, 1, 7);
		assertEquals(dateExpected, fileEntry.getTimePoint());
		
		assertEquals("IMG-20120804-WA0000.jpg", fileEntry.getFileName());
		assertEquals("asd/fgh/IMG-20120804-WA0000.jpg", fileEntry.getRelPath());
	}

	@Test
	void testAddNode() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setTimePoint(LocalDate.of(2023, 1, 7));
		fileEntry.setFileName("IMG-20230107-WA0000.jpg");
		fileEntry.setRelPath("asd/fgh/IMG-20230107-WA0000.jpg");
		
		DocumentBuilder builder = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			fail(e);
		}
		Document document = builder.newDocument();
		Element root = document.createElement("Parent");
		fileEntry.addNode(root);
		TestHelper.checkStringNode(root, "//Parent/FileEntry/Timepoint", "2023-01-07T00:00");
		TestHelper.checkStringNode(root, "//Parent/FileEntry/Filename", "IMG-20230107-WA0000.jpg");
		TestHelper.checkStringNode(root, "//Parent/FileEntry/Relpath", "asd/fgh/IMG-20230107-WA0000.jpg");
	}
}