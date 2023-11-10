package configurator;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import helper.DateUtils;
import helper.Xml;

public class Global {
	private static final String DEFAULT_LOCALE = "en";
	
	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger(Global.class);
	
	private Path outputDir;
	private Path debugDir;
	
	private String nameSuggestion;
	
	private DateUtils dateUtils;
	
	public Global() {
	}
	
	public Global(Path outputDir, Path debugDir, String locale) {
		this.outputDir = outputDir;
		this.debugDir = debugDir;
		this.dateUtils = new DateUtils(locale);
	}
	public Global(Path outputDir, Path debugDir) {
		this(outputDir, debugDir, DEFAULT_LOCALE);
	}
	
	public static Global fromXmlString(String xml) throws ConfigurationException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		
		Path outputDir = readPath(document, "//global/outputdir");
		Path debugDir = readPath(document, "//global/debugdir");
		String locale = Xml.getTextFromNode(document, "//global/locale"); 
		
		return new Global(outputDir, debugDir, locale);
	}
	
	private static Path readPath(Document doc, String xPathExpression, String alternative) throws ConfigurationException, XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String path = (String)xpath.compile(xPathExpression).evaluate(doc, XPathConstants.STRING);
		if(path==null) {
			path = alternative;
		}
		
		return Paths.get(path);
	}
	
	private static Path readPath(Document doc, String xPathExpression) throws ConfigurationException, XPathExpressionException {
		return readPath(doc, xPathExpression, null);
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
