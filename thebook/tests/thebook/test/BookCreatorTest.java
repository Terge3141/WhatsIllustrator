package thebook.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import creator.IWriterPlugin;
import creator.TexWriterPlugin;
import creator.WriterException;
import imagematcher.FileEntry;
import imagematcher.ImageMatcher;
import imagematcher.MatchEntry;
import program.BookCreator;

public class BookCreatorTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@BeforeClass
	public static void setLogger() throws MalformedURLException, URISyntaxException {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		AppenderComponentBuilder console = builder.newAppender("stdout", "Console");

		LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
		standard.addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n");
		console.add(standard);
		
		builder.add(console);

		RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);
		rootLogger.add(builder.newAppenderRef("stdout"));

		builder.add(rootLogger);
		
		Configurator.initialize(builder.build());
	}

	@Test
	public void testWriteTextTex_DE() throws WriterException, ParseException, IOException {
		CreatorAndPlugin cap = createTextTexFile("de");

		List<String> texLines = Files.readAllLines(cap.twp.getTexOutputPath());
		assertEquals(5, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}Freitag, der 16. M\\\"arz 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46): This is my message\\\\", texLines.get(2));
		assertEquals("\\textbf{Firstname Surname} (21:47): This is my message2\\\\", texLines.get(3));
		assertEquals("footer", texLines.get(4));
	}

	@Test
	public void testWriteTextTex_EN() throws WriterException, ParseException, IOException {
		CreatorAndPlugin cap = createTextTexFile("en");

		List<String> texLines = Files.readAllLines(cap.twp.getTexOutputPath());
		assertEquals(5, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}16 March 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46 AM): This is my message\\\\", texLines.get(2));
		assertEquals("\\textbf{Firstname Surname} (09:47 PM): This is my message2\\\\", texLines.get(3));
		assertEquals("footer", texLines.get(4));
	}

	@Test
	public void testWriteTextTex_Default() throws WriterException, ParseException, IOException {
		CreatorAndPlugin cap = createTextTexFile(null);

		List<String> texLines = Files.readAllLines(cap.twp.getTexOutputPath());
		assertEquals(5, texLines.size());
		assertEquals("header", texLines.get(0));
		assertEquals("\\begin{center}16 March 2018\\end{center}", texLines.get(1));
		assertEquals("\\textbf{Firstname Surname} (08:46 AM): This is my message\\\\", texLines.get(2));
		assertEquals("\\textbf{Firstname Surname} (09:47 PM): This is my message2\\\\", texLines.get(3));
		assertEquals("footer", texLines.get(4));
	}

	@Test
	public void testWriteMediaOmittedTex_NoHints() throws WriterException, ParseException, IOException {
		CreatorAndPlugin cap = createMediaOmittedTexFile(false);

		Path imgPath = cap.bc.getWriterConfig().getImagePoolDir().resolve("relpath/IMG-20180316-WA0001.jpg");
		List<String> texLines = Files.readAllLines(cap.twp.getTexOutputPath());

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
	public void testWriteMediaOmittedTex_WithHints() throws WriterException, ParseException, IOException {
		CreatorAndPlugin cap = createMediaOmittedTexFile(true);

		Path imgPath = cap.bc.getWriterConfig().getImagePoolDir().resolve("relpath/IMG-20180316-WA0001.jpg");
		List<String> texLines = Files.readAllLines(cap.twp.getTexOutputPath());

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

	private CreatorAndPlugin createTexFile(List<String> chatLines, List<String> propLines)
			throws ParseException, IOException, WriterException {
		String dir = this.folder.newFolder("test").toString();

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

		TexWriterPlugin plugin = new TexWriterPlugin();
		plugin.setHeader("header");
		plugin.setFooter("footer");

		List<IWriterPlugin> plugins = Arrays.asList(plugin);
		BookCreator bk = new BookCreator(inputDir, outputDir, emojiDir, plugins);
		bk.getWriterConfig().setImagePoolDir(imagePoolDir);

		bk.write();

		Path texFile = outputDir.resolve("WhatsApp Chat with Firstname Surname.tex");
		assertTrue(Files.exists(texFile));

		return new CreatorAndPlugin(bk, plugin);
	}

	private CreatorAndPlugin createTextTexFile(String locale) throws WriterException, ParseException, IOException {
		List<String> chatLines = new ArrayList<String>();
		chatLines.add("16/03/2018, 08:46 - Firstname Surname: This is my message");
		chatLines.add("16/03/2018, 21:47 - Firstname Surname: This is my message2");

		List<String> propLines = new ArrayList<String>();
		if (locale != null) {
			propLines.add(String.format("locale=%s", locale));
		}

		return createTexFile(chatLines, propLines);
	}

	private CreatorAndPlugin createMediaOmittedTexFile(boolean hints)
			throws WriterException, ParseException, IOException {
		List<String> chatLines = new ArrayList<String>();
		chatLines.add("16/03/2018, 08:46 - Firstname Surname: <Media omitted>");

		List<String> propLines = new ArrayList<String>();
		propLines.add("locale=en");
		propLines.add("mediaomittedhints=" + Boolean.toString(hints));

		return createTexFile(chatLines, propLines);
	}

	private class CreatorAndPlugin {
		public BookCreator bc;
		public TexWriterPlugin twp;

		public CreatorAndPlugin(BookCreator bc, TexWriterPlugin twp) {
			this.bc = bc;
			this.twp = twp;
		}
	}
}
