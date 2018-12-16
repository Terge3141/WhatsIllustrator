package messageparser.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import messageparser.NameLookup;

class NameLookupTest {

	@Test
	void testTryLookup()  {
		String xml = "<NameReplacer>"
				+ "<ReplaceItem>"
				+ "<oldName>Doc Brown</oldName>"
				+ "<newName>Doc</newName>"
				+ "</ReplaceItem>"
				+ "<ReplaceItem>"
				+ "<oldName>Martin McFly</oldName>"
				+ "<newName>Marti</newName>"
				+ "</ReplaceItem>"
				+ "</NameReplacer>";
		
		NameLookup nl=NameLookup.fromXmlString(xml);
		assertEquals("Doc", nl.tryLookup("Doc Brown"));
		assertEquals("Marti", nl.tryLookup("Martin McFly"));
		assertEquals("other",  nl.tryLookup("other"));
	}

}
