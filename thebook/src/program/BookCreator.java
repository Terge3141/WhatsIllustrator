package program;

import helper.DateUtils;
import helper.FileHandler;
import imagematcher.*;

import messageparser.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import creator.IWriterPlugin;
import creator.WriterConfig;
import creator.WriterException;

public class BookCreator {

	private final String DEFAULT_LOCALE = "en";

	private static Logger logger = LogManager.getLogger(BookCreator.class);

	private WriterConfig config;
	private List<IWriterPlugin> plugins;

	public BookCreator(Path inputDir, Path outputDir, Path emojiInputDir, List<IWriterPlugin> writerPlugins) {
		this.config = new WriterConfig(inputDir, outputDir, emojiInputDir);
		this.plugins = writerPlugins;
	}

	public void write() throws IOException, ParseException, WriterException  {
		List<String> txtFiles = FileHandler.listDir(this.config.getChatDir(), ".*.txt");
		if (txtFiles.size() != 1) {
			throw new IllegalArgumentException(
					String.format("Invalid number of .txt-files found: %d", txtFiles.size()));
		}

		Path txtInputPath = Paths.get(txtFiles.get(0));
		logger.info("Using {} as input", txtInputPath);

		String namePrefix = txtInputPath.toFile().getName();
		namePrefix = namePrefix.substring(0, namePrefix.length() - 4);

		readProperties();
		ImageMatcher im = getImageMatcher(namePrefix);
		NameLookup nl = getNameLookup();

		WhatsappParser parser = WhatsappParser.of(txtInputPath, im, nl);

		// pre append
		for (IWriterPlugin plugin : this.plugins) {
			plugin.preAppend(this.config);
		}

		// write messages
		logger.info("Start parsing messages");
		IMessage msg;
		LocalDateTime last = LocalDateTime.MIN;
		while ((msg = parser.nextMessage()) != null) {
			if (DateUtils.dateDiffer(last, msg.getTimepoint())) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendDateHeader(msg.getTimepoint());
				}
			}

			last = msg.getTimepoint();

			if (msg instanceof TextMessage) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendTextMessage((TextMessage) msg);
				}
			} else if (msg instanceof ImageMessage) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendImageMessage((ImageMessage) msg);
				}
			} else if (msg instanceof MediaOmittedMessage) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendMediaOmittedMessage((MediaOmittedMessage) msg);
				}
			} else if (msg instanceof MediaMessage) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendMediaMessage((MediaMessage) msg);
				}
			}
		}
		
		// post append
		for (IWriterPlugin plugin : this.plugins) {
			plugin.postAppend();
		}

		im.toXmlFile();
	}
	
	public WriterConfig getWriterConfig() {
		return this.config;
	}

	private ImageMatcher getImageMatcher(String namePrefix) throws IOException, ParseException {
		Path matchInputPath = this.config.getConfigDir().resolve(namePrefix + ".match.xml");
		Path matchOutputPath = this.config.getOutputDir().resolve(namePrefix + ".match.xml");
		ImageMatcher im = null;

		if (matchInputPath.toFile().isFile()) {
			logger.info("Loading matches '{}'", matchInputPath);
			im = ImageMatcher.fromXmlFile(matchInputPath);
			im.setSearchMode(false);
		} else {
			Path imagePoolDir = config.getImagePoolDir();
			im = new ImageMatcher();
			if (imagePoolDir == null) {
				im.setSearchMode(false);
			} else {
				logger.info("Loading pool images from '{}'", imagePoolDir);
				im.loadFiles(imagePoolDir);
				im.setSearchMode(true);
				logger.info("{} images found", im.getFileList().size());
			}
		}

		im.setMatchOutputPath(matchOutputPath);

		return im;
	}

	private NameLookup getNameLookup() throws IOException {
		Path lookupInputPath = this.config.getConfigDir().resolve("namelookup.xml");
		NameLookup nl;
		if (lookupInputPath.toFile().isFile()) {
			logger.info("Loading name lookup '{}'", lookupInputPath);
			nl = NameLookup.fromXmlFile(lookupInputPath);
		} else {
			nl = new NameLookup();
		}

		return nl;
	}

	private void readProperties() throws FileNotFoundException, IOException {
		Path propertiesInputPath = this.config.getConfigDir().resolve("bookcreator.properties");
		
		// file should contain
		// locale=language
		String locale = this.DEFAULT_LOCALE;
		boolean writeMediaOmittedHints = false;
		if (propertiesInputPath.toFile().isFile()) {
			logger.info("Using properties file '{}'", propertiesInputPath);
			Properties properties = new Properties();
			properties.load(new FileInputStream(propertiesInputPath.toFile()));

			locale = properties.getProperty("locale", locale);
			writeMediaOmittedHints = Boolean.parseBoolean(
					properties.getProperty("mediaomittedhints", Boolean.toString(writeMediaOmittedHints)));
		}
		this.config.setDateUtils(new DateUtils(locale));
		this.config.setWriteMediaOmittedHints(writeMediaOmittedHints);
	}
}
