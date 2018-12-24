package program;

import helper.DateUtils;
import helper.EmojiParser;
import helper.FileHandler;
import imagematcher.*;
import helper.Latex;
import helper.Misc;

import messageparser.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.text.TextStringBuilder;

public class BookCreator {

	// The top level input directory. It typically contains the subdirectories
	// chat and config
	private Path inputDir;

	// This is the directory where the tex file and other output files are
	// written
	private Path outputDir;

	// This is the directory where the used written emojis are written to.
	// Default is OutputDir/emojis
	private Path emojiOutputDir;

	// It is the directory where the chat txt file and the images are stored.
	// These files can be obtained by exporting the chat in the Whatsapp app
	// By default the directory is set to InputDir/Chat
	private Path chatDir;

	// In this directory all configuration files are, e.g. the
	// chatname.match.xml file
	private Path configDir;

	// It contains all images for the chat.
	// By default it is set to ChatDir
	private Path imageDir;

	// It should contain all whatsapp images.
	// This directory is used if there "<Media omitted>" lines in the chat file.
	// and if no chatname.match.xml file is available.
	// It is set to null by default.
	private Path imagePoolDir;

	private Path emojiInputDir;
	
	private Path texOutputPath;

	private EmojiParser emojis;

	private String header;
	private String footer;

	private final String EMOJIPREFIX = "emoji_u";
	private final String DEFAULT_LOCALE = "en";

	private List<CopyItem> copyList;
	private DateUtils dateUtils;
	private boolean writeMediaOmittedHints;

	public BookCreator(Path inputDir, Path outputDir, Path emojiInputDir) throws IOException {
		List<String> emojiList = readEmojiList(emojiInputDir);
		this.emojiInputDir = emojiInputDir;

		this.emojis = new EmojiParser(emojiList, x -> getEmojiPath(x));

		this.header = getRessourceAsString("header.tex.tmpl");
		this.footer = getRessourceAsString("footer.tex.tmpl");

		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.chatDir = this.inputDir.resolve("chat");
		this.configDir = this.inputDir.resolve("config");
		this.imageDir = this.chatDir;
		this.imagePoolDir = null;
		this.emojiOutputDir = this.outputDir.resolve("emojis");

		emojiOutputDir.toFile().mkdir();
	}

	public void writeTex() throws IOException, ParseException {
		this.copyList = new ArrayList<BookCreator.CopyItem>();

		List<String> txtFiles = FileHandler.listDir(chatDir, ".*.txt");
		if (txtFiles.size() != 1) {
			throw new IllegalArgumentException(
					String.format("Invalid number of .txt-files found: %d", txtFiles.size()));
		}

		Path txtInputPath = Paths.get(txtFiles.get(0));
		System.out.format("Using %s as input\n", txtInputPath);

		String namePrefix = txtInputPath.toFile().getName();
		namePrefix = namePrefix.substring(0, namePrefix.length() - 4);
		this.texOutputPath = this.outputDir.resolve(namePrefix + ".tex");

		Path matchInputPath = this.configDir.resolve(namePrefix + ".match.xml");
		Path matchOutputPath = this.outputDir.resolve(namePrefix + ".match.xml");
		ImageMatcher im = null;
		if (matchInputPath.toFile().isFile()) {
			System.out.format("Loading matches '%s'\n", matchInputPath);
			im = ImageMatcher.fromXmlFile(matchInputPath);
			im.setSearchMode(false);
		} else {
			im = new ImageMatcher();
			if (imagePoolDir == null) {
				im.setSearchMode(false);
			} else {
				System.out.format("Loading pool images from '%s'\n", imagePoolDir);
				im.loadFiles(imagePoolDir);
				im.setSearchMode(true);
				System.out.format("%d images found\n", im.getFileList().size());
			}
		}

		Path lookupInputPath = this.configDir.resolve("namelookup.xml");
		NameLookup nl;
		if (lookupInputPath.toFile().isFile()) {
			System.out.format("Loading name lookup '%s'\n", lookupInputPath);
			nl = NameLookup.fromXmlFile(lookupInputPath);
		} else {
			nl = new NameLookup();
		}

		WhatsappParser parser = WhatsappParser.of(txtInputPath, im, nl);

		Path propertiesInputPath = this.configDir.resolve("bookcreator.properties");
		// file should contain
		// locale=language
		String locale = this.DEFAULT_LOCALE;
		this.writeMediaOmittedHints = false;
		if (propertiesInputPath.toFile().isFile()) {
			System.out.format("Using properties file '%s'\n", propertiesInputPath);
			Properties properties = new Properties();
			properties.load(new FileInputStream(propertiesInputPath.toFile()));

			locale = properties.getProperty("locale", locale);
			this.writeMediaOmittedHints = Boolean.parseBoolean(
					properties.getProperty("mediaomittedhints", Boolean.toString(this.writeMediaOmittedHints)));
		}
		this.dateUtils = new DateUtils(locale);

		System.out.println("Start parsing messages");
		TextStringBuilder tsb = new TextStringBuilder();
		tsb.appendln(header);

		IMessage msg;
		LocalDateTime last = LocalDateTime.MIN;
		while ((msg = parser.nextMessage()) != null) {
			if (DateUtils.dateDiffer(last, msg.getTimepoint())) {
				tsb.appendln("\\begin{center}%s\\end{center}",
						Latex.encodeLatex(this.dateUtils.formatDateString(msg.getTimepoint())));
			}

			last = msg.getTimepoint();

			if (msg instanceof TextMessage) {
				appendTextMessage((TextMessage) msg, tsb);
			} else if (msg instanceof ImageMessage) {
				appendImageMessage((ImageMessage) msg, tsb);
			} else if (msg instanceof MediaOmittedMessage) {
				appendMediaOmittedMessage((MediaOmittedMessage) msg, tsb);
			} else if (msg instanceof MediaMessage) {
				appendMediaMessage((MediaMessage) msg, tsb);
			}
		}

		tsb.appendln(this.footer);

		System.out.format("Writing tex file to '%s'\n", texOutputPath);
		Misc.writeAllText(texOutputPath, tsb.toString());

		System.out.format("Writing match file to '%s'\n", matchOutputPath);
		im.toXmlFile(matchOutputPath);

		System.out.format("Copy emojis to '%s'\n", emojiOutputDir);
		copyList();
	}

	private String formatSenderAndTime(IMessage msg) {
		String sender = String.format("\\textbf{%s}", Latex.encodeLatex(msg.getSender()));
		return String.format("%s (%s):", sender, this.dateUtils.formatTimeString(msg.getTimepoint()));
	}

	private String getRessourceAsString(String name) throws IOException {
		InputStream inputStream = this.getClass().getResourceAsStream(name);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();
		byte[] byteArray = buffer.toByteArray();

		return new String(byteArray, StandardCharsets.UTF_8);
	}

	private List<String> readEmojiList(Path dir) {
		List<String> list = new ArrayList<>();

		for (File x : dir.toFile().listFiles()) {
			String fileName = x.getName();
			String nr = fileName.replace(EMOJIPREFIX, "").replace(".png", "");
			list.add(nr);

			String[] excludes = { "0023", "002a", "0030", "0031", "0032", "0033", "0034", "0035", "0036", "0037",
					"0038", "0039" };

			for (String str : excludes) {
				list.remove(str);
			}
		}

		System.out.format("Loaded %d entries from %s\n", list.size(), dir);

		return list;
	}

	private void copyList() throws IOException {
		for (CopyItem x : this.copyList) {
			Files.copy(x.getSrcPath(), x.getDstPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private String encode(String str) {
		str = Latex.encodeLatex(str);
		str = Latex.replaceURL(str);
		str = this.emojis.replaceEmojis(str);
		return str;
	}

	private String createLatexImage(String path, String subscription) {
		TextStringBuilder tsb = new TextStringBuilder();
		tsb.appendln("\\includegraphics[height=0.1\\textheight]{%s}\\\\", path);
		tsb.appendln("\\small{\\textit{%s}}", encode(subscription));
		return tsb.toString();
	}

	private String createLatexImage(Path path, String subscription) {
		return createLatexImage(path.toString(), subscription);
	}

	private String getEmojiPath(String str) {
		String src = String.format("%s/%s%s.png", emojiInputDir, EMOJIPREFIX, str);
		String dst = String.format("%s/%s.png", emojiOutputDir, str);

		copyList.add(new CopyItem(src, dst));

		return String.format("\\includegraphics[scale=0.075]{emojis/%s.png}", str);
	}

	private void appendTextMessage(TextMessage msg, TextStringBuilder tsb) {
		String senderAndTime = formatSenderAndTime(msg);
		String content = encode(msg.content);
		tsb.appendln("%s %s\\\\", senderAndTime, content);
	}

	private void appendImageMessage(ImageMessage msg, TextStringBuilder tsb) {
		tsb.appendln("%s\\\\", formatSenderAndTime(msg));
		tsb.append("\\begin{center}");
		tsb.append(createLatexImage(this.imageDir.resolve(msg.getFilename()), msg.getSubscription()));
		tsb.appendln("\\end{center}");
	}

	private void appendMediaOmittedMessage(MediaOmittedMessage msg, TextStringBuilder tsb) {
		tsb.appendln("%s\\\\", formatSenderAndTime(msg));
		Iterator<String> it = msg.getRelpaths().iterator();
		while (it.hasNext()) {
			tsb.append("\\begin{center}");
			String relPath = it.next();
			String str = "";
			if (this.writeMediaOmittedHints) {
				str = String.format("%s;%s;%d", msg.getTimepoint(), relPath, msg.getCnt());
			}

			tsb.append(createLatexImage(this.imagePoolDir.resolve(relPath), str));
			tsb.appendln("\\end{center}");
		}
	}

	private void appendMediaMessage(MediaMessage msg, TextStringBuilder tsb) {
		String str = String.format("%s \\textit{%s}", formatSenderAndTime(msg), Latex.encodeLatex(msg.getFilename()));
		if (!Misc.isNullOrWhiteSpace(msg.getSubscription())) {
			str = str + " - " + encode(msg.getSubscription());
		}

		tsb.appendln(str);
		tsb.appendln("\\\\");
	}

	public Path getInputDir() {
		return inputDir;
	}

	public void setInputDir(Path inputDir) {
		this.inputDir = inputDir;
	}

	public Path getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(Path outputDir) {
		this.outputDir = outputDir;
	}

	public Path getEmojiOutputDir() {
		return emojiOutputDir;
	}

	public void setEmojiOutputDir(Path emojiOutputDir) {
		this.emojiOutputDir = emojiOutputDir;
	}

	public Path getChatDir() {
		return chatDir;
	}

	public void setChatDir(Path chatDir) {
		this.chatDir = chatDir;
	}

	public Path getConfigDir() {
		return configDir;
	}

	public void setConfigDir(Path configDir) {
		this.configDir = configDir;
	}

	public Path getImageDir() {
		return imageDir;
	}

	public void setImageDir(Path imageDir) {
		this.imageDir = imageDir;
	}

	public Path getImagePoolDir() {
		return imagePoolDir;
	}

	public void setImagePoolDir(Path imagePoolDir) {
		this.imagePoolDir = imagePoolDir;
	}

	private class CopyItem {

		private String src;
		private String dst;

		public CopyItem(String src, String dst) {
			this.src = src;
			this.dst = dst;
		}

		public Path getSrcPath() {
			return Paths.get(src);
		}

		public Path getDstPath() {
			return Paths.get(dst);
		}
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public Path getTexOutputPath() {
		return texOutputPath;
	}
}
