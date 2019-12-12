package creator.plugins;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import helper.DateUtils;

public class WriterConfig {

	private final String EMOJIPREFIX = "emoji_u";

	private static Logger logger = LogManager.getLogger(WriterConfig.class);

	// The top level input directory. It typically contains the subdirectories
	// chat and config
	private Path inputDir;

	// This is the directory where the tex file and other output files are
	// written
	private Path outputDir;

	// This is the directory where the used written emojis are written to.
	// Default is OutputDir/emojis
	// private Path emojiOutputDir;

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

	// This is the input directory where all emojis are stored
	private Path emojiInputDir;
	
	private DateUtils dateUtils;

	private String namePrefix;

	private List<String> emojiList;
	
	private boolean writeMediaOmittedHints;

	public WriterConfig(Path inputDir, Path outputDir, Path emojiInputDir) {
		this.emojiInputDir = emojiInputDir;
		this.emojiList = readEmojiList(emojiInputDir);

		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.chatDir = this.inputDir.resolve("chat");
		this.configDir = this.inputDir.resolve("config");
		this.imageDir = this.chatDir;
		this.imagePoolDir = null;
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

		logger.info("Loaded {} entries from {}", list.size(), dir);

		return list;
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

	public Path getEmojiInputDir() {
		return emojiInputDir;
	}

	public void setEmojiInputDir(Path emojiInputDir) {
		this.emojiInputDir = emojiInputDir;
	}

	public DateUtils getDateUtils() {
		return dateUtils;
	}

	public void setDateUtils(DateUtils dateUtils) {
		this.dateUtils = dateUtils;
	}

	public String getNamePrefix() {
		return namePrefix;
	}

	/**
	 * Sets the name prefix, normally something like 'WhatsApp Chat with Firstname
	 * Lastname'
	 * 
	 * @param prefix The actual prefix
	 */
	public void setNamePrefix(String prefix) {
		this.namePrefix = prefix;
	}

	public List<String> getEmojiList() {
		return emojiList;
	}

	public void setEmojiList(List<String> emojiList) {
		this.emojiList = emojiList;
	}

	public boolean isWriteMediaOmittedHints() {
		return writeMediaOmittedHints;
	}

	public void setWriteMediaOmittedHints(boolean writeMediaOmittedHints) {
		this.writeMediaOmittedHints = writeMediaOmittedHints;
	}
}
