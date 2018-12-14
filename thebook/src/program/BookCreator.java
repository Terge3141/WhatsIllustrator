package program;

import helper.DateUtils;
import helper.EmojiParser;
import helper.FileHandler;
import helper.IEmojiFormatFunction;
import imagematcher.*;
import helper.Latex;
import helper.Misc;

import messageparser.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;

public class BookCreator implements IEmojiFormatFunction {

	// The top level input directory. It typically contains the subdirectories
	// chat and config
	public String InputDir;

	// This is the directory where the tex file and other output files are
	// written
	public String OutputDir;

	// This is the directory where the used written emojis are written to.
	// Default is OutputDir/emojis
	public String EmojiOutputDir;

	// It is the directory where the chat txt file and the images are stored.
	// These files can be obtained by exporting the chat in the Whatsapp app
	// By default the directory is set to InputDir/Chat
	public String ChatDir;

	// In this directory all configuration files are, e.g. the
	// chatname.match.xml file
	public String ConfigDir;

	// It contains all images for the chat.
	// By default it is set to ChatDir
	public String ImageDir;

	// It should contain all whatsapp images.
	// This directory is used if there "<Media omitted>" lines in the chat file.
	// and if no chatname.match.xml file is available.
	// It is set to null by default.
	public String ImagePoolDir;

	private String emojiInputDir;

	private EmojiParser emojis;

	private String header;
	private String footer;

	private final String EMOJIPREFIX = "emoji_u";

	private List<CopyItem> copyList;

	public BookCreator(String inputDir, String outputDir, String emojiInputDir) throws IOException {
		List<String> emojiList = readEmojiList(emojiInputDir);
		this.emojiInputDir = emojiInputDir;

		this.emojis = new EmojiParser(emojiList, this);

		header = Misc.readAllText("header.tex.tmpl");
		footer = Misc.readAllText("footer.tex.tmpl");

		InputDir = inputDir;
		OutputDir = outputDir;
		ChatDir = Paths.get(InputDir, "chat").toString();
		ConfigDir = Paths.get(InputDir, "config").toString();
		ImageDir = Paths.get(ChatDir).toString();
		ImagePoolDir = null;
		EmojiOutputDir = Paths.get(OutputDir, "emojis").toString();

		File dir = new File(EmojiOutputDir);
		dir.mkdir();
	}

	public void writeTex() throws IOException, ParserConfigurationException, SAXException, ParseException,
			TransformerFactoryConfigurationError, TransformerException {
		this.copyList = new ArrayList<BookCreator.CopyItem>();

		List<String> txtFiles = FileHandler.listDir(ChatDir, ".*.txt");
		if (txtFiles.size() != 1) {
			throw new IllegalArgumentException(
					String.format("Invalid number of .txt-files found: %d", txtFiles.size()));
		}

		String txtInputPath = txtFiles.get(0);
		System.out.format("Using %s as input\n", txtInputPath);

		String namePrefix = FileHandler.getFileName(txtInputPath);
		namePrefix = namePrefix.substring(0, namePrefix.length() - 4);
		String texOutputPath = Paths.get(OutputDir, namePrefix + ".tex").toString();

		String matchInputPath = Paths.get(ConfigDir, namePrefix + ".match.xml").toString();
		String matchOutputPath = Paths.get(OutputDir, namePrefix + ".match.xml").toString();
		ImageMatcher im = null;
		if (FileHandler.fileExists(matchInputPath)) {
			System.out.format("Loading matches '%s'\n", matchInputPath);
			im = ImageMatcher.fromXmlFile(matchInputPath);
			im.setSearchMode(false);
		} else {
			im = new ImageMatcher();
			if (ImagePoolDir == null) {
				im.setSearchMode(false);
			} else {
				System.out.format("Loading pool images from '%s'\n", ImagePoolDir);
				im.loadFiles(ImagePoolDir);
				im.setSearchMode(true);
			}
		}

		WhatsappParser parser = new WhatsappParser(txtInputPath, im);

		StringBuilder sb = new StringBuilder();
		sb.append(header + "\n");

		IMessage msg;
		LocalDateTime last = LocalDateTime.MIN;
		while ((msg = parser.NextMessage()) != null) {
			if (DateUtils.dateDiffer(last, msg.getTimepoint())) {
				// TODO via sb.format??
				sb.append("\\begin{center}" + DateUtils.formatDateString(msg.getTimepoint()) + "\\end{center}\n");
			}

			last = msg.getTimepoint();

			if (msg instanceof TextMessage) {
				appendTextMessage((TextMessage) msg, sb);
			} else if (msg instanceof ImageMessage) {
				appendImageMessage((ImageMessage) msg, sb);
			} else if (msg instanceof MediaOmittedMessage) {
				appendMediaOmittedMessage((MediaOmittedMessage) msg, sb);
			} else if (msg instanceof MediaMessage) {
				appendMediaMessage((MediaMessage) msg, sb);
			}
		}

		sb.append(this.footer + "\n");

		System.out.format("Writing tex file to '%s'\n", texOutputPath);
		Misc.writeAllText(texOutputPath, sb.toString());

		System.out.format("Writing match file to '%s'\n", matchOutputPath);
		im.toXmlFile(matchOutputPath);

		System.out.format("Copy emojis to '%s'\n", EmojiOutputDir);
		copyList();
	}

	public String format(String str) {
		return getEmojiPath(str);
	}

	public static String formatSenderAndTime(IMessage msg) {
		String sender = String.format("\\textbf{%s}", Latex.encodeLatex(msg.getSender()));
		return String.format("%s (%s):", sender, DateUtils.formatTimeString(msg.getTimepoint()));
	}

	private List<String> readEmojiList(String dir) {
		List<String> list = new ArrayList<>();

		File lister = new File(dir);
		for (File x : lister.listFiles()) {
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
			System.out.format("%s --> %s\n", x.getSrcPath(), x.getDstPath());
			Files.copy(x.getSrcPath(), x.getDstPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private String Encode(String str) {
		str = Latex.encodeLatex(str);
		str = Latex.replaceURL(str);
		str = this.emojis.replaceEmojis(str);
		return str;
	}

	private String createLatexImage(String path, String subscription) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\\includegraphics[height=0.1\\textheight]{%s}\\\\\n", path));
		sb.append(String.format("\\small{\\textit{%s}}\n", Encode(subscription)));
		return sb.toString();
	}

	private String createLatexImage(Path path, String subscription) {
		return createLatexImage(path.toString(), subscription);
	}

	private String getEmojiPath(String str) {
		String src = String.format("%s/%s%s.png", emojiInputDir, EMOJIPREFIX, str);
		String dst = String.format("%s/%s.png", EmojiOutputDir, str);

		copyList.add(new CopyItem(src, dst));

		return String.format("\\includegraphics[scale=0.075]{emojis/%s.png}", str);
	}

	private void appendTextMessage(TextMessage msg, StringBuilder sb) {
		String senderAndTime = formatSenderAndTime(msg);
		String content = Encode(msg.content);
		sb.append(String.format("%s %s\n", senderAndTime, content));
		sb.append("\\\\\n");
	}

	private void appendImageMessage(ImageMessage msg, StringBuilder sb) {
		sb.append(String.format("%s\\\\\n", formatSenderAndTime(msg)));
		sb.append("\\begin{center}");
		sb.append(createLatexImage(Paths.get(ImageDir, msg.getFilename()), msg.getSubscription()));
		sb.append("\\end{center}\n");
	}

	private void appendMediaOmittedMessage(MediaOmittedMessage msg, StringBuilder sb) {
		sb.append(String.format("%s\\\\\n", formatSenderAndTime(msg)));
		sb.append("\\begin{center}");

		Iterator<String> it = msg.getRelpaths().iterator();
		while (it.hasNext()) {
			String str = it.next();
			sb.append(createLatexImage(Paths.get(ImagePoolDir, str), Encode(str)));
		}

		sb.append("\\end{center}\n");
	}

	private void appendMediaMessage(MediaMessage msg, StringBuilder sb) {
		String str = String.format("%s \\textit{%s}", formatSenderAndTime(msg), Latex.encodeLatex(msg.getFilename()));
		if (!Misc.isNullOrWhiteSpace(msg.getSubscription())) {
			str = str + " - " + Encode(msg.getSubscription());
		}

		sb.append(str + "\n");
		sb.append("\\\\\n");
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

		public String getSrcString() {
			return this.src;
		}

		public String getDstString() {
			return this.dst;
		}
	}
}
