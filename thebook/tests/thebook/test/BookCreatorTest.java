package thebook.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import imagematcher.FileEntry;
import imagematcher.ImageMatcher;
import imagematcher.MatchEntry;
import program.BookCreator;

public class BookCreatorTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testWriteTextTex_DE() throws IOException, ParserConfigurationException, SAXException, ParseException,
			TransformerFactoryConfigurationError, TransformerException {
		Path texFile = createTextTexFile("de");

		List<String> texLines = Files.readAllLines(texFile);
		assertEquals(5, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}Freitag, der 16. M\\\"arz 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46): This is my message\\\\", texLines.get(2));
		assertEquals("\\textbf{Firstname Surname} (21:47): This is my message2\\\\", texLines.get(3));
		assertEquals("footer", texLines.get(4));
	}

	@Test
	public void testWriteTextTex_EN() throws IOException, ParserConfigurationException, SAXException, ParseException,
			TransformerFactoryConfigurationError, TransformerException {
		Path texFile = createTextTexFile("en");

		List<String> texLines = Files.readAllLines(texFile);
		assertEquals(5, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}16 March 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46 AM): This is my message\\\\", texLines.get(2));
		assertEquals("\\textbf{Firstname Surname} (09:47 PM): This is my message2\\\\", texLines.get(3));
		assertEquals("footer", texLines.get(4));
	}

	@Test
	public void testWriteTextTex_Default() throws IOException, ParserConfigurationException, SAXException,
			ParseException, TransformerFactoryConfigurationError, TransformerException {
		Path texFile = createTextTexFile(null);

		List<String> texLines = Files.readAllLines(texFile);
		assertEquals(5, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}16 March 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46 AM): This is my message\\\\", texLines.get(2));
		assertEquals("\\textbf{Firstname Surname} (09:47 PM): This is my message2\\\\", texLines.get(3));
		assertEquals("footer", texLines.get(4));
	}

	@Test
	public void testWriteMediaOmittedTex_NoHints() throws IOException, ParseException {
		BookCreator bc = createMediaOmittedTexFile(false);

		Path imgPath = bc.getImagePoolDir().resolve("relpath/IMG-20180316-WA0001.jpg");
		List<String> texLines = Files.readAllLines(bc.getTexOutputPath());

		assertEquals(7, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}16 March 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46 AM):\\\\", texLines.get(2));
		assertEquals(String.format("\\begin{center}\\includegraphics[height=0.1\\textheight]{%s}\\\\", imgPath),
				texLines.get(3));
		assertEquals("\\small{\\textit{}}", texLines.get(4));
		assertEquals("\\end{center}", texLines.get(5));
		assertEquals("footer", texLines.get(6));
	}

	@Test
	public void testWriteMediaOmittedTex_WithHints() throws IOException, ParseException {
		BookCreator bc = createMediaOmittedTexFile(true);

		Path imgPath = bc.getImagePoolDir().resolve("relpath/IMG-20180316-WA0001.jpg");
		List<String> texLines = Files.readAllLines(bc.getTexOutputPath());

		assertEquals(7, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}16 March 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46 AM):\\\\", texLines.get(2));
		assertEquals(String.format("\\begin{center}\\includegraphics[height=0.1\\textheight]{%s}\\\\", imgPath),
				texLines.get(3));
		assertEquals("\\small{\\textit{2018-03-16T08:46;relpath/IMG-20180316-WA0001.jpg;0}}", texLines.get(4));
		assertEquals("\\end{center}", texLines.get(5));
		assertEquals("footer", texLines.get(6));
	}

	private BookCreator createMediaOmittedTexFile(boolean hints) throws IOException, ParseException {
		List<String> chatLines = new ArrayList<String>();
		chatLines.add("16/03/2018, 08:46 - Firstname Surname: <Media omitted>");

		List<String> propLines = new ArrayList<String>();
		propLines.add("locale=en");
		propLines.add("mediaomittedhints=" + Boolean.toString(hints));

		String dir = folder.newFolder("testWriteMediaOmittedTex").toString();

		Path inputDir = Paths.get(dir, "input");
		Path outputDir = Paths.get(dir, "output");
		Path emojiDir = Paths.get(dir, "emojis");
		Path imagePoolDir = Paths.get(dir, "imagepool");

		Path chatDir = inputDir.resolve("chat");
		Path configDir = inputDir.resolve("config");

		ImageMatcher imageMatcher = new ImageMatcher();

		List<FileEntry> fileEntries = new ArrayList<>();
		fileEntries.add(new FileEntry("/path/relpath/IMG-20180316-WA0001.jpg", "/path"));
		MatchEntry matchEntry = new MatchEntry(LocalDateTime.of(2018, 03, 16, 8, 46), fileEntries, 0);
		imageMatcher.setMatchList(Arrays.asList(matchEntry));

		Files.createDirectories(inputDir);
		Files.createDirectories(outputDir);
		Files.createDirectories(emojiDir);
		Files.createDirectories(chatDir);
		Files.createDirectories(configDir);

		Files.write(chatDir.resolve("WhatsApp Chat with Firstname Surname.txt"), chatLines);

		imageMatcher.toXmlFile(configDir.resolve("WhatsApp Chat with Firstname Surname.match.xml"));
		Files.write(configDir.resolve("bookcreator.properties"), propLines);

		BookCreator bk = new BookCreator(inputDir, outputDir, emojiDir);
		bk.setImagePoolDir(imagePoolDir);
		bk.setHeader("header");
		bk.setFooter("footer");
		bk.writeTex();

		Path texFile = outputDir.resolve("WhatsApp Chat with Firstname Surname.tex");
		assertTrue(Files.exists(texFile));

		return bk;
	}

	private Path createTextTexFile(String locale) throws IOException, ParseException {
		List<String> chatLines = new ArrayList<String>();
		chatLines.add("16/03/2018, 08:46 - Firstname Surname: This is my message");
		chatLines.add("16/03/2018, 21:47 - Firstname Surname: This is my message2");

		List<String> propLines = new ArrayList<String>();
		propLines.add(String.format("locale=%s", locale));

		String dir = folder.newFolder("testWriteTex").toString();
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
