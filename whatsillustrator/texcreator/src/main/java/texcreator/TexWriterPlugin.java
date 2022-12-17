package texcreator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.text.TextStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jcodec.api.JCodecException;

import configurator.Global;
import creator.plugins.IWriterPlugin;
import creator.plugins.WriterException;
import emojicontainer.EmojiContainer;
import helper.Latex;
import helper.Misc;
import messageparser.IMessage;
import messageparser.ImageMessage;
import messageparser.LinkMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;
import messageparser.VideoMessage;
import videothumbnails.ThumbnailCreator;

public class TexWriterPlugin implements IWriterPlugin {
	private static Logger logger = LogManager.getLogger(TexWriterPlugin.class);

	private Global globalConfig;
	private TextStringBuilder tsb;

	private String header;
	private String footer;

	private EmojiContainer emojis;

	// This is the directory where the used written emojis are written to.
	// Default is OutputDir/emojis
	private Path emojiOutputDir;
	
	private List<CopyItem> copyList;
	
	private Path imageOutputDir;
	private int videoThumbnailCnt = 6;

	private Path outputDir;
	private Path texOutputPath;


	@Override
	public void preAppend(String xmlConfig, Global globalConfig) throws WriterException {
		this.globalConfig = globalConfig;
		this.tsb = new TextStringBuilder();

		this.outputDir = this.globalConfig.getOutputDir().resolve(globalConfig.getNameSuggestion()).resolve("tex");
		this.outputDir.toFile().mkdirs();

		try {
			this.emojis = new EmojiContainer();
		} catch (IOException e) {
			throw new WriterException(e);
		}
		this.emojiOutputDir = this.outputDir.resolve("emojis");
		this.emojiOutputDir.toFile().mkdirs();
		
		this.imageOutputDir = this.outputDir.resolve("images");
		this.imageOutputDir.toFile().mkdirs();
		
		this.copyList = new ArrayList<CopyItem>();
		this.texOutputPath = this.outputDir.resolve(globalConfig.getNameSuggestion() + ".tex");

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
				Latex.encodeLatex(this.globalConfig.getDateUtils().formatDateString(timepoint)));
	}

	@Override
	public void appendTextMessage(TextMessage msg) throws WriterException {
		String senderAndTime = formatSenderAndTime(msg);
		String content = encode(msg.getContent());
		tsb.appendln("%s %s\\\\", senderAndTime, content);
	}

	@Override
	public void appendImageMessage(ImageMessage msg) throws WriterException {
		Path absoluteImgPath = msg.getFilepath();

		if(Files.exists(absoluteImgPath)) {
			tsb.appendln("%s\\\\", formatSenderAndTime(msg));
			tsb.appendln("\\begin{center}");
			tsb.append(createLatexImage(absoluteImgPath, msg.getSubscription()));
			tsb.appendln("\\end{center}");
		} else {
			logger.warn("File '{}' does not exist, skipping message", absoluteImgPath);
		}
	}
	
	@Override
	public void appendVideoMessage(VideoMessage msg) throws WriterException {
		Path videoPath = msg.getFilepath();
		if(videoPath.toFile().exists()) {
			ThumbnailCreator tc = ThumbnailCreator.of(videoPath, videoThumbnailCnt, imageOutputDir);
			List<Path> tnPaths;
			try {
				tnPaths = tc.createThumbnails();
			} catch (IOException | JCodecException e) {
				throw new WriterException(e);
			}
			
			tsb.appendln("%s\\\\", formatSenderAndTime(msg));
			tsb.appendln("\\begin{center}");
			tsb.append(createLatexImageStack(tnPaths, tc.isLandScape(), msg.getSubscription()));
			tsb.appendln("\\end{center}");
		} else {
			logger.warn("File '{}' does not exist, skipping message", videoPath);
		}
	}

	@Override
	public void appendMediaOmittedMessage(MediaOmittedMessage msg) throws WriterException {
		tsb.appendln("%s\\\\", formatSenderAndTime(msg));
		Iterator<Path> it = msg.getAbspaths().iterator();
		while (it.hasNext()) {
			tsb.append("\\begin{center}");
			Path absPath = it.next();
			String str = "";
			if (this.globalConfig.isWriteMediaOmittedHints()) {
				str = String.format("%s;%s;%d", msg.getTimepoint(), absPath, msg.getCnt());
			}

			tsb.append(createLatexImage(absPath, str));
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
	
	@Override
	public void appendLinkMessage(LinkMessage msg) throws WriterException {
		String str = String.format("\\textit{%s}", Latex.encodeLatex(msg.getUrl()));
		
		tsb.appendln(str);
		tsb.appendln("\\\\");
	}

	private String getEmojiPath(String str) {
		Path dst = this.emojiOutputDir.resolve(String.format("%s.png", str));
		Path rel = this.outputDir.relativize(dst);
		
		this.copyList.add(new CopyItem(str, dst));

		return String.format("\\includegraphics[scale=0.075]{%s}", rel);
	}

	private void copyList() throws IOException {
		for (CopyItem x : this.copyList) {
			this.emojis.copyEmoji(x.id, x.dst);
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
		return String.format("%s (%s):", sender, this.globalConfig.getDateUtils().formatTimeString(msg.getTimepoint()));
	}

	private String createLatexImage(String path, String subscription) throws WriterException {
		Path src = Paths.get(path);
		Path fileName = src.getFileName();
		Path dst = this.imageOutputDir.resolve(fileName);
		Path relDst = this.outputDir.relativize(dst);
		try {
			Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
		}
		catch(IOException ioe) {
			throw new WriterException("Cannot copy file from '" + src.toString() + "' to '" + dst.toString() + "'",
					ioe);
		}
		
		TextStringBuilder tsb = new TextStringBuilder();
		tsb.appendln("\\includegraphics[height=0.1\\textheight]{%s}\\\\", relDst);
		
		if(subscription==null) {
			subscription="";
		}
		subscription = subscription.replace("\n", " ").replace("\r", "");
		tsb.appendln("\\small{\\textit{%s}}", encode(subscription));
		return tsb.toString();
	}
	
	public String createLatexImageStack(List<Path> paths, boolean landscapeImages, String subscription) throws WriterException {
		TextStringBuilder tsb = new TextStringBuilder();
		String sizeInfo = String.format("width=%f\\columnwidth", landscapeImages ? 0.3 : 0.15);
		
		for(Path src : paths) {
			Path fileName = src.getFileName();
			Path dst = this.imageOutputDir.resolve(fileName);
			Path relDst = this.outputDir.relativize(dst);
			try {
				Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
			}
			catch(IOException ioe) {
				throw new WriterException("Cannot copy file from '" + src.toString() + "' to '" + dst.toString() + "'",
						ioe);
			}
			
			tsb.appendln("\\includegraphics[%s]{%s}", sizeInfo, relDst); 
		}
		
		tsb.appendln("\\\\");
		
		if(subscription==null) {
			subscription = "";
		}
		subscription = subscription.replace("\n", " ").replace("\r", "");
		tsb.appendln("\\small{\\textit{%s}}", encode(subscription));
		return tsb.toString();
	}
	
	private String createLatexImage(Path path, String subscription) throws WriterException {
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

		private String id;
		private Path dst;

		public CopyItem(String id, Path dst) {
			this.id = id;
			this.dst = dst;
		}

		public String getId() {
			return id;
		}

		public Path getDstPath() {
			return dst;
		}
	}
}
