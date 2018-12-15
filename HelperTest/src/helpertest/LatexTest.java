package helpertest;

import static org.junit.Assert.*;
import helper.Latex;

import org.junit.Test;

public class LatexTest {

	@Test
	public void testReplaceURL_Http_Only() {
		assertEquals("\\url{http://ab.cd.ef}", Latex.replaceURL("http://ab.cd.ef"));
	}

	@Test
	public void testReplaceURL_Https_Only() {
		assertEquals("\\url{https://ab.cd.ef/blub}", Latex.replaceURL("https://ab.cd.ef/blub"));
	}

	@Test
	public void testReplaceURL_Http_Sandwich() {
		assertEquals("aa \\url{http://def} ijk", Latex.replaceURL("aa http://def ijk"));
	}

	@Test
	public void testReplaceURL_Https_Newline() {
		assertEquals("\\url{https://bla.com/asd}\nabcdef", Latex.replaceURL("https://bla.com/asd\nabcdef"));
	}

	@Test
	public void testReplaceURL_Https_LatexNewline() {
		assertEquals("\\url{https://bla.com/asd}\\\\abcdef", Latex.replaceURL("https://bla.com/asd\\\\abcdef"));
	}

	@Test
	public void testReplaceURL_Https_LatexEscape() {
		assertEquals("\\url{https://bla.com/asd\\&a=1} abcdef", Latex.replaceURL("https://bla.com/asd\\&a=1 abcdef"));
	}

	@Test
	public void testReplaceURL_None() {
		assertEquals("abcdef", Latex.replaceURL("abcdef"));
	}

	@Test
	public void testReplaceURL_Https_WordsPrefix() {
		assertEquals("Some words \\url{https://bla.com}", Latex.replaceURL("Some words https://bla.com"));
	}

	@Test
	public void testReplaceURL_Http_Words_Https() {
		assertEquals("\\url{http://as.de} Some other word \\url{https://bla.com}",
				Latex.replaceURL("http://as.de Some other word https://bla.com"));
	}
}
