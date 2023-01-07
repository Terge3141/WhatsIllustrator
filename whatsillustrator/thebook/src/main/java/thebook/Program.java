package thebook;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import configurator.ConfigurationException;
import configurator.Global;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import creator.BookCreator;
import creator.plugins.IWriterPlugin;
import creator.plugins.WriterException;
import helper.Misc;
import messageparser.IParser;
import messageparser.ParserException;

public class Program {

	private static Logger logger;

	public static Options getOptions() {
		Options options = new Options();
		options.addRequiredOption("c", "config", true, "File to xml config file");

		return options;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T createObject(String className) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> myClass = Class.forName(className);
		Constructor<?> cons = myClass.getConstructor();
		return (T)cons.newInstance();
	}
	
	private static Node selectNode(Object source, String xpathExpression) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		Node node = (Node)xpath.compile(xpathExpression).evaluate(source, XPathConstants.NODE);
		return node;
	}
	
	private static String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter sw = new StringWriter();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.transform(new DOMSource(node), new StreamResult(sw));
		return sw.toString();
	}
	
	public static void run(String xml) throws ConfigurationException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserException, WriterException, IOException, ParseException, ParserConfigurationException, XPathExpressionException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		
		Node globalNode = selectNode(document, "/configuration/global");
		Global global = Global.fromXmlString(nodeToString(globalNode));
		
		Node parserNodeName = selectNode(document, "/configuration/parser/name");
		String className = parserNodeName.getTextContent();
		
		Class<?> myClass = Class.forName(className);
		Constructor<?> cons = myClass.getConstructor();
		IParser parser = (IParser)cons.newInstance();
		Node parserNodeConfig = selectNode(document, "/configuration/parser/parserconfiguration");
		parser.init(nodeToString(parserNodeConfig), global);
		global.setNameSuggestion(parser.getNameSuggestion());
		
		List<IWriterPlugin> plugins = new ArrayList<IWriterPlugin>();
		NodeList writerNodes = (NodeList)xpath.compile("/configuration/writers/writer").evaluate(document, XPathConstants.NODESET);
		for(int i=0; i<writerNodes.getLength(); i++) {
			Node writerNode = writerNodes.item(i);
			String name = selectNode(writerNode, "name").getTextContent();
			String xmlConfig = selectNode(writerNode, "writerconfiguration").getTextContent();
			IWriterPlugin plugin = createObject(name);
			plugin.preAppend(xmlConfig, global);
			plugins.add(plugin);
		}
		
		BookCreator creator = new BookCreator(global, parser, plugins);
		creator.write();
	}

	public static void main(String[] args) throws Exception {
		logger = LogManager.getLogger(Program.class);

		Options options = getOptions();
		CommandLineParser commandLineParser = new DefaultParser();
		String xmlConfigFile = null;
		try {
			CommandLine line = commandLineParser.parse(options, args);
			xmlConfigFile = line.getOptionValue("config");
		} catch (MissingOptionException moe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("thebook", options);
			return;
		}
		
		String xml = Misc.readAllText(Paths.get(xmlConfigFile));

		
		long start = System.currentTimeMillis();
		
		run(xml);

		long stop = System.currentTimeMillis();
		double seconds = 0.001 * (stop - start);

		logger.info("Done {}", seconds);
	}
}
