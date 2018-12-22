package thebook.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import helper.Misc;
import program.BookCreator;

public class BookCreatorTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void testWriteTex() throws IOException, ParserConfigurationException, SAXException, ParseException, TransformerFactoryConfigurationError, TransformerException {
		String line="16/03/2018, 21:46 - Firstname Surname: This is my message\n16/03/2018, 21:47 - Firstname Surname: This is my message2";
		assertEquals(1, 1);
		// TODO change
		//String dir = folder.newFolder("testWriteTex").toString();
		String dir = "/tmp/testWriteText"; Files.createDirectories(Paths.get(dir));
		Path inputDir=Paths.get(dir, "input");
		Path outputDir=Paths.get(dir, "output");
		Path emojiDir =Paths.get(dir, "emojis");
		
		Path chatDir=inputDir.resolve("chat");
		
		Files.createDirectories(inputDir);
		Files.createDirectories(outputDir);
		Files.createDirectories(emojiDir);
		Files.createDirectories(chatDir);
		
		Misc.writeAllText(chatDir.resolve("WhatsApp Chat with Firstname Surname.txt").toString(), line);
		
		BookCreator bk=new BookCreator(inputDir.toString(), outputDir.toString(), emojiDir.toString());
		bk.setHeader("header");
		bk.setFooter("footer");
		bk.writeTex();
		
		Path texFile=outputDir.resolve("WhatsApp Chat with Firstname Surname.tex");
		assertTrue(Files.exists(texFile));
		
		List<String> texLines=Files.readAllLines(texFile);
		assertEquals(5, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}Freitag, der 16. M\\\"arz 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (21:46): This is my message\\\\", texLines.get(2));
		assertEquals("\\textbf{Firstname Surname} (21:47): This is my message2\\\\", texLines.get(3));
		assertEquals("footer", texLines.get(4));
	}

}
