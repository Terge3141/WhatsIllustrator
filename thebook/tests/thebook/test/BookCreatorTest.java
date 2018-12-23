package thebook.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import program.BookCreator;

public class BookCreatorTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testWriteTex_DE() throws IOException, ParserConfigurationException, SAXException, ParseException,
			TransformerFactoryConfigurationError, TransformerException {
		Path texFile = createTexFile("de");

		List<String> texLines = Files.readAllLines(texFile);
		assertEquals(5, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}Freitag, der 16. M\\\"arz 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46): This is my message\\\\", texLines.get(2));
		assertEquals("\\textbf{Firstname Surname} (21:47): This is my message2\\\\", texLines.get(3));
		assertEquals("footer", texLines.get(4));
	}

	@Test
	public void testWriteTex_EN() throws IOException, ParserConfigurationException, SAXException, ParseException,
			TransformerFactoryConfigurationError, TransformerException {
		Path texFile = createTexFile("en");

		List<String> texLines = Files.readAllLines(texFile);
		assertEquals(5, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}16 March 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46 AM): This is my message\\\\", texLines.get(2));
		assertEquals("\\textbf{Firstname Surname} (09:47 PM): This is my message2\\\\", texLines.get(3));
		assertEquals("footer", texLines.get(4));
	}

	@Test
	public void testWriteTex_Default() throws IOException, ParserConfigurationException, SAXException, ParseException,
			TransformerFactoryConfigurationError, TransformerException {
		Path texFile = createTexFile(null);

		List<String> texLines = Files.readAllLines(texFile);
		assertEquals(5, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}16 March 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46 AM): This is my message\\\\", texLines.get(2));
		assertEquals("\\textbf{Firstname Surname} (09:47 PM): This is my message2\\\\", texLines.get(3));
		assertEquals("footer", texLines.get(4));
	}

	private Path createTexFile(String locale) throws IOException, ParserConfigurationException, SAXException,
			ParseException, TransformerFactoryConfigurationError, TransformerException {
		List<String> chatLines = new ArrayList<String>();
		chatLines.add("16/03/2018, 08:46 - Firstname Surname: This is my message");
		chatLines.add("16/03/2018, 21:47 - Firstname Surname: This is my message2");

		List<String> propLines = new ArrayList<String>();
		propLines.add(String.format("locale=%s", locale));

		// String dir = folder.newFolder("testWriteTex").toString();
		String dir = "/tmp/testWriteText";
		Files.createDirectories(Paths.get(dir));
		Path inputDir = Paths.get(dir, "input");
		Path outputDir = Paths.get(dir, "output");
		Path emojiDir = Paths.get(dir, "emojis");

		Path chatDir = inputDir.resolve("chat");
		Path configDir = inputDir.resolve("config");

		Files.createDirectories(inputDir);
		Files.createDirectories(outputDir);
		Files.createDirectories(emojiDir);
		Files.createDirectories(chatDir);
		Files.createDirectories(configDir);

		Files.write(chatDir.resolve("WhatsApp Chat with Firstname Surname.txt"), chatLines);
		if (locale != null) {
			Files.write(configDir.resolve("bookcreator.properties"), propLines);
		}

		BookCreator bk = new BookCreator(inputDir, outputDir, emojiDir);
		bk.setHeader("header");
		bk.setFooter("footer");
		bk.writeTex();

		Path texFile = outputDir.resolve("WhatsApp Chat with Firstname Surname.tex");
		assertTrue(Files.exists(texFile));
		return texFile;
	}
}
