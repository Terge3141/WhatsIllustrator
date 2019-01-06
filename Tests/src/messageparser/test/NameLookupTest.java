package messageparser.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import helper.Misc;
import messageparser.NameLookup;

public class NameLookupTest {
	
	@BeforeClass
	public static void setLogger()  {
		Misc.setStdoutLogger();
	}

	@Test
	public void testTryLookup()  {
		String xml = "<NameLookup>"
				+ "<ReplaceItem>"
				+ "<oldName>Doc Brown</oldName>"
				+ "<newName>Doc</newName>"
				+ "</ReplaceItem>"
				+ "<ReplaceItem>"
				+ "<oldName>Martin McFly</oldName>"
				+ "<newName>Marti</newName>"
				+ "</ReplaceItem>"
				+ "</NameLookup>";
		
		NameLookup nl=NameLookup.fromXmlString(xml);
		assertEquals("Doc", nl.tryLookup("Doc Brown"));
		assertEquals("Marti", nl.tryLookup("Martin McFly"));
		assertEquals("other",  nl.tryLookup("other"));
	}

}
