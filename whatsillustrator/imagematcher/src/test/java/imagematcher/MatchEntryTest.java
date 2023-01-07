package imagematcher;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;

class MatchEntryTest {

	@Test
	void testFromNode() {
		String fm = ""
				+ createFileEntryXmlString(2023, 1, 6, 1)
				+ createFileEntryXmlString(2023, 1, 7, 2);
		fm = xmlWrap("Filematches", fm);
		
		String xml = ""
				+ xmlWrap("Timepoint", "2023-01-07T15:21")
				+ xmlWrap("IsImage", "true")
				+ fm
				+ xmlWrap("Cnt", "23");
		xml = xmlWrap("MatchEntry", xml);
		
		SAXReader reader = new SAXReader();
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));
		Document document = null;
		try {
			document = reader.read(stream);
		} catch (DocumentException e) {
			fail(e);
		}
		MatchEntry matchEntry = MatchEntry.fromNode(document.selectSingleNode("MatchEntry"));
		
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
		
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Parent");
		matchEntry.addNode(root);
		
		checkStringNode(root, "//Parent/MatchEntry/Timepoint", "2023-01-07T15:47");
		checkStringNode(root, "//Parent/MatchEntry/IsImage", "true");
		checkStringNode(root, "//Parent/MatchEntry/Cnt", "2");
		
		List<Node> fes = root.selectNodes("//Parent/MatchEntry/Filematches/FileEntry");
		// only smoke tests, detailed tests are done in FileEntry unit tests
		assertEquals(2, fes.size());
	}

	private String createFileEntryXmlString(int year, int month, int day, int wa) {
		String yearstr = String.format("%04d", year);
		String monthstr = String.format("%02d", month);
		String daystr = String.format("%02d", day);
		String wastr = String.format("%04d", wa);
		
		String tp = String.format("%s-%s-%sT00:00", yearstr, monthstr, daystr);
		String fn = String.format("IMG-%s%s%s-WA%s.jpg", yearstr, monthstr, daystr, wastr);
		String rp = String.format("asd/fgh/%s", fn);
		
		String xml = ""
				+ xmlWrap("Timepoint", tp)
				+ xmlWrap("Filename", fn)
				+ xmlWrap("Relpath", rp);
		
		xml = xmlWrap("FileEntry", xml);
		
		return xml;	
	}
	
	private String xmlWrap(String tag, String value) {
		return String.format("<%s>%s</%s>", tag, value, tag);
	}
	
	private void checkStringNode(Element element, String xpath, String expectedValue) {
		Node node = element.selectSingleNode(xpath);
		assertEquals(expectedValue, node.getStringValue());
	}
}
