package messageparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.text.TextStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import configurator.Global;
import helper.FileHandler;
import imagematcher.*;

public class WhatsappParser implements IParser {
	
	private static Logger logger = LogManager.getLogger(WhatsappParser.class);
	
	private Global globalConfig;
	private Path messageDir;
	private Path configDir;
	private Path chatDir;

	private List<String> lines;
	private ImageMatcher imageMatcher;
	private NameLookup nameLookup;
	private int index;
	private LastCnt lastCnt;

	// TODO extend pattern with Nickname and Message
	private static final String DATEPATTERN = "[0-3][0-9]/[0-1][0-9]/[0-9]{4},\\ [0-2][0-9]:[0-5][0-9]";
	private static final String PATTERN = DATEPATTERN + "\\ -\\ ";
	private static final String FILE_ATTACHED = "(file attached)";
	private static final String MEDIA_OMITTED = "<Media omitted>";

	public WhatsappParser() {

	}
	
	@Override
	public void init(String xmlConfig, Global globalConfig) throws Exception {
		SAXReader reader = new SAXReader();
		InputStream stream = new ByteArrayInputStream(xmlConfig.getBytes(StandardCharsets.UTF_16));
		Document document = reader.read(stream);
		
		this.globalConfig = globalConfig;
		this.messageDir = Paths.get(document.selectSingleNode("//messagedir").getStringValue());
		this.configDir = this.messageDir.resolve("config");
		this.chatDir = this.messageDir.resolve("chat");
		
		List<String> txtFiles = FileHandler.listDir(chatDir, ".*.txt");
		if (txtFiles.size() != 1) {
			throw new IllegalArgumentException(
					String.format("Invalid number of .txt-files found: %d", txtFiles.size()));
		}		
		Path txtInputPath = Paths.get(txtFiles.get(0));
		logger.info("Using {} as input", txtInputPath);
		
		this.lines = Files.readAllLines(txtInputPath);
		
		String namePrefix = txtInputPath.toFile().getName();
		namePrefix = namePrefix.substring(0, namePrefix.length() - 4);
		this.imageMatcher = getImageMatcher(namePrefix);
		this.nameLookup = getNameLookup();
	}

	public IMessage nextMessage() {
		if (this.index == this.lines.size()) {
			return null;
		}

		String line = this.lines.get(this.index);
		if (!isHeader(line)) {
			throw new IllegalArgumentException(String.format("Invalid header line: '%s'", line));
		}

		this.index++;

		Pattern p = Pattern.compile("^" + DATEPATTERN);
		Matcher m = p.matcher(line);
		if (!m.find()) {
			throw new IllegalArgumentException(String.format("Invalid date format in line: '%s'", line));
		}
		String dateStr = line.substring(m.start(), m.end());

		LocalDateTime date = null;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
			date = LocalDateTime.parse(dateStr, formatter);
		} catch (DateTimeParseException dtpe) {
			throw new IllegalArgumentException(String.format("Invalid date format in line: '%s'", line));
		}

		int senderEnd = line.indexOf(":", dateStr.length());

		// special message, e.g. encryption information
		if (senderEnd == -1) {
			logger.warn("No sender found, skipping line '{}'", line);
			return nextMessage();
		}

		String sender = line.substring(dateStr.length() + 3, senderEnd);
		// special case: phone numbers have \u202A as start and \u202C as end
		sender = sender.replaceAll("\u202A", "");
		sender = sender.replaceAll("\u202C", "");
		sender = this.nameLookup.tryLookup(sender);

		String contentStr = line.substring(senderEnd + 2);

		if (contentStr.endsWith(FILE_ATTACHED)) {
			String fileName = contentStr.substring(0, contentStr.length() - FILE_ATTACHED.length() - 1);
			String extension = fileName.substring(fileName.length() - 3);

			switch (extension) {
			case "jpg":
				String subscription = parseNextLines().trim();
				return new ImageMessage(date, sender, fullPath(fileName), subscription);
			default:
				subscription = parseNextLines().trim();
				return new MediaMessage(date, sender, fileName, subscription);
			}
		} else if (contentStr.equals(MEDIA_OMITTED)) {
			MatchEntry entry = this.imageMatcher.pick(date, getCnt(date));
			if (entry.isImageType() && entry.getFileMatches().size() > 0) {
				List<String> relpaths = entry.getFileMatches().stream().map(x -> x.getRelPath()).distinct()
						.collect(Collectors.toList());
				return new MediaOmittedMessage(entry.getTimePoint(), sender, relpaths, entry.getCnt());
			} else {
				return nextMessage();
			}
		} else {
			contentStr = contentStr + parseNextLines();
			contentStr = contentStr.trim();
			return new TextMessage(date, sender, contentStr);
		}
	}

	private boolean isHeader(String str) {
		return str.matches("^" + PATTERN + ".*");
	}

	private String parseNextLines() {
		TextStringBuilder tsb = new TextStringBuilder();
		while (this.index < this.lines.size() && !isHeader(this.lines.get(this.index))) {
			tsb.appendNewLine();
			tsb.append(this.lines.get(this.index));
			this.index++;
		}

		return tsb.toString();
	}

	private int getCnt(LocalDateTime tp) {
		if (this.lastCnt.cnt == -1) {
			this.lastCnt = new LastCnt(tp, 0);
		} else {
			if (this.lastCnt.date.equals(tp)) {
				this.lastCnt = new LastCnt(this.lastCnt.date, this.lastCnt.cnt + 1);
			} else {
				this.lastCnt = new LastCnt(tp, 0);
			}
		}

		return this.lastCnt.cnt;
	}
	
	private Path fullPath(String relativePath) {
		return this.chatDir.resolve(relativePath);
	}
	
	private ImageMatcher getImageMatcher(String namePrefix) throws IOException, ParseException {
		Path matchInputPath = this.configDir.resolve(namePrefix + ".match.xml");
		Path matchOutputPath = this.messageDir.resolve("output").resolve(namePrefix + ".match.xml");
		ImageMatcher im = null;

		if (matchInputPath.toFile().isFile()) {
			logger.info("Loading matches '{}'", matchInputPath);
			im = ImageMatcher.fromXmlFile(matchInputPath);
			im.setSearchMode(false);
		} else {
			Path imagePoolDir = globalConfig.getImagePoolDir();
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
		Path lookupInputPath = this.globalConfig.getConfigDir().resolve("namelookup.xml");
		NameLookup nl;
		if (lookupInputPath.toFile().isFile()) {
			logger.info("Loading name lookup '{}'", lookupInputPath);
			nl = NameLookup.fromXmlFile(lookupInputPath);
		} else {
			nl = new NameLookup();
		}

		return nl;
	}

	public class LastCnt {
		private LocalDateTime date;
		private int cnt;

		public LastCnt() {
			this.date = LocalDateTime.MIN;
			this.cnt = 0;
		}

		public LastCnt(LocalDateTime date, int cnt) {
			this.date = date;
			this.cnt = cnt;
		}
	}
}
