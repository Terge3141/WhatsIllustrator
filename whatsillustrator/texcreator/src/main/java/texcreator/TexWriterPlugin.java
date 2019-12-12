package texcreator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.text.TextStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import creator.plugins.IWriterPlugin;
import creator.plugins.WriterConfig;
import creator.plugins.WriterException;
import helper.EmojiParser;
import helper.Latex;
import helper.Misc;
import messageparser.IMessage;
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;

public class TexWriterPlugin implements IWriterPlugin {
	private static Logger logger = LogManager.getLogger(TexWriterPlugin.class);

	private WriterConfig config;
	private TextStringBuilder tsb;

	private String header;
	private String footer;

	private EmojiParser emojis;

	// This is the directory where the used written emojis are written to.
	// Default is OutputDir/emojis
	private Path emojiOutputDir;
	private List<CopyItem> copyList;

	private Path outputDir;
	private Path texOutputPath;

	@Override
	public void preAppend(WriterConfig config) throws WriterException {
		this.config = config;
		this.tsb = new TextStringBuilder();

		this.outputDir = this.config.getOutputDir().resolve("tex");
		this.outputDir.toFile().mkdir();

		this.emojis = new EmojiParser(config.getEmojiList());
		this.emojiOutputDir = this.outputDir.resolve("emojis");
		this.emojiOutputDir.toFile().mkdir();
		this.copyList = new ArrayList<CopyItem>();
		this.texOutputPath = this.outputDir.resolve(config.getNamePrefix() + ".tex");

		try {
			if (this.header == null) {
				this.header = getRessourceAsString("header.tex.tmpl");
			}

			if (this.footer == null) {
				this.footer = getRessourceAsString("footer.tex.tmpl");
			}
		} catch (IOException ioe) {
			throw new WriterException(ioe);
		}

		tsb.appendln(this.header);
	}

	@Override
	public void postAppend() throws WriterException {
		tsb.appendln(this.footer);

		logger.info("Writing tex file to '{}'", texOutputPath);
		try {
			Misc.writeAllText(texOutputPath, tsb.toString());
		} catch (IOException ioe) {
			throw new WriterException(ioe);
		}

		logger.info("Copy emojis to '{}'", emojiOutputDir);
		try {
			copyList();
		} catch (IOException ioe) {
			throw new WriterException(ioe);
		}
	}

	@Override
	public void appendDateHeader(LocalDateTime timepoint) throws WriterException {
		this.tsb.appendln("\\begin{center}%s\\end{center}",
				Latex.encodeLatex(this.config.getDateUtils().formatDateString(timepoint)));
	}

	@Override
	public void appendTextMessage(TextMessage msg) throws WriterException {
		String senderAndTime = formatSenderAndTime(msg);
		String content = encode(msg.getContent());
		tsb.appendln("%s %s\\\\", senderAndTime, content);
	}

	@Override
	public void appendImageMessage(ImageMessage msg) throws WriterException {
		Path absoluteImgPath = this.config.getImageDir().resolve(msg.getFilename());
		Path relativeImgPath = this.outputDir.relativize(absoluteImgPath);

		tsb.appendln("%s\\\\", formatSenderAndTime(msg));
		tsb.append("\\begin{center}");
		tsb.append(createLatexImage(relativeImgPath, msg.getSubscription()));
		tsb.appendln("\\end{center}");
	}

	@Override
	public void appendMediaOmittedMessage(MediaOmittedMessage msg) throws WriterException {
		tsb.appendln("%s\\\\", formatSenderAndTime(msg));
		Iterator<String> it = msg.getRelpaths().iterator();
		while (it.hasNext()) {
			tsb.append("\\begin{center}");
			String relPath = it.next();
			String str = "";
			if (this.config.isWriteMediaOmittedHints()) {
				str = String.format("%s;%s;%d", msg.getTimepoint(), relPath, msg.getCnt());
			}

			tsb.append(createLatexImage(this.config.getImagePoolDir().resolve(relPath), str));
			tsb.appendln("\\end{center}");
		}
	}

	@Override
	public void appendMediaMessage(MediaMessage msg) throws WriterException {
		String str = String.format("%s \\textit{%s}", formatSenderAndTime(msg), Latex.encodeLatex(msg.getFilename()));
		if (!Misc.isNullOrWhiteSpace(msg.getSubscription())) {
			str = str + " - " + encode(msg.getSubscription());
		}

		tsb.appendln(str);
		tsb.appendln("\\\\");
	}

	private String getEmojiPath(String str) {
		Path src = this.config.getEmojiInputDir().resolve(String.format("%s%s.png", this.emojis.getEmojiPrefix(), str));
		Path dst = this.emojiOutputDir.resolve(String.format("%s.png", str));

		copyList.add(new CopyItem(src, dst));

		return String.format("\\includegraphics[scale=0.075]{emojis/%s.png}", str);
	}

	private void copyList() throws IOException {
		for (CopyItem x : this.copyList) {
			Files.copy(x.getSrcPath(), x.getDstPath(), StandardCopyOption.REPLACE_EXISTING);
		}
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

	private String encode(String str) {
		str = Latex.encodeLatex(str);
		str = Latex.replaceURL(str);
		str = this.emojis.replaceEmojis(str, x -> getEmojiPath(x));
		return str;
	}

	private String formatSenderAndTime(IMessage msg) {
		String sender = String.format("\\textbf{%s}", Latex.encodeLatex(msg.getSender()));
		return String.format("%s (%s):", sender, this.config.getDateUtils().formatTimeString(msg.getTimepoint()));
	}

	private String createLatexImage(String path, String subscription) {
		TextStringBuilder tsb = new TextStringBuilder();
		tsb.appendln("\\includegraphics[height=0.1\\textheight]{%s}\\\\", path);
		
		subscription = subscription.replace("\n", " ").replace("\r", "");
		tsb.appendln("\\small{\\textit{%s}}", encode(subscription));
		return tsb.toString();
	}

	private String createLatexImage(Path path, String subscription) {
		return createLatexImage(path.toString(), subscription);
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
		return this.texOutputPath;
	}

	private class CopyItem {

		private Path src;
		private Path dst;

		public CopyItem(Path src, Path dst) {
			this.src = src;
			this.dst = dst;
		}

		public Path getSrcPath() {
			return src;
		}

		public Path getDstPath() {
			return dst;
		}
	}

}
