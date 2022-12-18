package configurator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import helper.DateUtils;

public class Global {
	private final String DEFAULT_LOCALE = "en";
	
	private static Logger logger = LogManager.getLogger(Global.class);
	
	private Path outputDir;
	private Path debugDir;
	
	private String nameSuggestion;
	
	private DateUtils dateUtils;
	//private List<String> emojiList;
	
	public static Global fromXmlString(String xml) throws ConfigurationException, DocumentException {
		SAXReader reader = new SAXReader();
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));
		Document document = reader.read(stream);
		
		Global global = new Global();
		global.outputDir = readPath(document, "//global/outputdir");
		global.debugDir = readPath(document, "//global/debugdir");
		
		global.dateUtils = new DateUtils(global.DEFAULT_LOCALE);
				
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
		str = str + ", outputdir: " + outputDir;
		str = str + ", debugdir: " + debugDir;
		str = str + ", nameSuggestion: " + nameSuggestion;
		return str;
	}

	public Path getOutputDir() {
		return outputDir;
	}

	public Path getDebugDir() {
		return debugDir;
	}
	
	public void setNameSuggestion(String nameSuggestion) {
		this.nameSuggestion = nameSuggestion;
	}
	
	public String getNameSuggestion() {
		return nameSuggestion;
	}
	
	// TODO update for WhatsApp
	public boolean isWriteMediaOmittedHints() {
		return false;
	}

	public DateUtils getDateUtils() {
		return dateUtils;
	}
}
