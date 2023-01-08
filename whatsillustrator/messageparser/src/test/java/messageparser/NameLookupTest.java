package messageparser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NameLookupTest {

	@Test
	void testTryLookupExists() {
		String xml = createXml(4);
		NameLookup nl = NameLookup.fromXmlString(xml);
		assertEquals("new0", nl.tryLookup("old0"));
		assertEquals("new1", nl.tryLookup("old1"));
		assertEquals("new2", nl.tryLookup("old2"));
		assertEquals("new3", nl.tryLookup("old3"));
	}
	
	@Test
	void testTryLookupNotExists() {
		String xml = createXml(4);
		NameLookup nl = NameLookup.fromXmlString(xml);
		assertEquals("old4", nl.tryLookup("old4"));
	}

	private String createXml(int cnt) {
		String xml = "";
		for(int i=0; i<cnt; i++) {
			String tmp = ""
					+ xmlWrap("oldName", String.format("old%d", i))
					+ xmlWrap("newName", String.format("new%d", i));
			xml += xmlWrap("ReplaceItem", tmp);
		}
		
		return xmlWrap("NameLookup", xml);
	}
	
	private String xmlWrap(String tag, String value) {
		return String.format("<%s>%s</%s>", tag, value, tag);
	}
}
