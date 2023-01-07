package imagematcher;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDate;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;

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

		SAXReader reader = new SAXReader();
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));
		Document document = null;
		try {
			document = reader.read(stream);
		} catch (DocumentException e) {
			fail(e);
		}
		
		FileEntry fileEntry = FileEntry.fromNode(document.selectSingleNode("FileEntry"));
		
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
		
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Parent");
		fileEntry.addNode(root);
		checkStringNode(root, "//Parent/FileEntry/Timepoint", "2023-01-07T00:00");
		checkStringNode(root, "//Parent/FileEntry/Filename", "IMG-20230107-WA0000.jpg");
		checkStringNode(root, "//Parent/FileEntry/Relpath", "asd/fgh/IMG-20230107-WA0000.jpg");
	}
	
	private void checkStringNode(Element element, String xpath, String expectedValue) {
		Node node = element.selectSingleNode(xpath);
		assertEquals(expectedValue, node.getStringValue());
	}
}