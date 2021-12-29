package configurator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import helper.DateUtils;

public class Global {
	private final String DEFAULT_LOCALE = "en";
	private final String EMOJIPREFIX = "emoji_u";
	
	private static Logger logger = LogManager.getLogger(Global.class);
	
	private Path inputDir;
	private Path outputDir;
	private Path emojiDir;
	private Path imagePoolDir;
	private Path debugDir;
	private Path namePrefix;
	
	private Path chatDir;
	private Path configDir;
	private Path imageDir;
	
	private DateUtils dateUtils;
	private List<String> emojiList;
	
	public static Global fromXmlString(String xml) throws ConfigurationException, DocumentException {
		SAXReader reader = new SAXReader();
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));
		Document document = reader.read(stream);
		
		Global global = new Global();
		global.inputDir = readPath(document, "//global/inputdir");
		global.outputDir = readPath(document, "//global/outputdir", global.inputDir.resolve("output").toString());
		global.emojiDir = readPath(document, "//global/emojidir");
		global.imagePoolDir = readPath(document, "//global/imagepooldir");
		global.debugDir = readPath(document, "//global/debugdir");
		global.namePrefix = readPath(document, "//global/nameprefix");
		
		global.chatDir = global.inputDir.resolve("chat");
		global.configDir = global.inputDir.resolve("config");
		global.imageDir = global.chatDir;
		
		global.dateUtils = new DateUtils(global.DEFAULT_LOCALE);
		
		global.emojiList = global.readEmojiList(global.emojiDir);
				
		return global;
	}
	
	private static Path readPath(Node node, String xPathExpression, String alternative) throws ConfigurationException {
		Node pathNode = node.selectSingleNode(xPathExpression);
		String path = (pathNode==null) ? alternative : pathNode.getStringValue();
		
		return Paths.get(path);
	}
	
	private static Path readPath(Node node, String xPathExpression) throws ConfigurationException {
		return readPath(node, xPathExpression, null);
	}
	
	@Override
	public String toString() {
		String str = "";
		str = str + "inputdir: " + inputDir;
		str = str + ", outputdir: " + outputDir;
		str = str + ", emojidir: " + emojiDir;
		str = str + ", imagepooldir: " + imagePoolDir;
		str = str + ", debugdir: " + debugDir;
		str = str + ", nameprefix: " + namePrefix;
		return str;
	}
	
	public Path getInputDir() {
		return inputDir;
	}

	public Path getOutputDir() {
		return outputDir;
	}

	public Path getEmojiDir() {
		return emojiDir;
	}

	public Path getImagePoolDir() {
		return imagePoolDir;
	}

	public Path getDebugDir() {
		return debugDir;
	}
	
	public Path getNamePrefix() {
		return namePrefix;
	}

	public Path getChatDir() {
		return chatDir;
	}

	public Path getConfigDir() {
		return configDir;
	}

	public Path getImageDir() {
		return imageDir;
	}
	
	// TODO update for WhatsApp
	public boolean isWriteMediaOmittedHints() {
		return false;
	}

	public DateUtils getDateUtils() {
		return dateUtils;
	}
	
	public List<String> getEmojiList() {
		return emojiList;
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
}
