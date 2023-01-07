package imagematcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dom4j.Element;
import org.dom4j.Node;

public class TestHelper {
	public static String xmlWrap(String tag, String value) {
		return String.format("<%s>%s</%s>", tag, value, tag);
	}
	
	public static void checkStringNode(Element element, String xpath, String expectedValue) {
		Node node = element.selectSingleNode(xpath);
		assertEquals(expectedValue, node.getStringValue());
	}
	
	public static String createFileEntryXmlString(int year, int month, int day, int wa) {
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
	
	public static String createMatchEntryXmlString(int year, int month, int day, int hh, int mm) {
		String fm = ""
				+ createFileEntryXmlString(2023, 1, 6, 1)
				+ createFileEntryXmlString(2023, 1, 7, 2);
		fm = xmlWrap("Filematches", fm);
		
		String tp = String.format("%04d-%02d-%02dT%02d:%02d",
				year, month, day, hh, mm);
		
		String xml = ""
				+ xmlWrap("Timepoint", tp)
				+ xmlWrap("IsImage", "true")
				+ fm
				+ xmlWrap("Cnt", "23");
		xml = xmlWrap("MatchEntry", xml);
		
		return xml;
	}
}
