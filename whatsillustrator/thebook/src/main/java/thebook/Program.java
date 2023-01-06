package thebook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
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
	
	public static void run(String xml) throws ConfigurationException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserException, WriterException, IOException, ParseException, ParserConfigurationException, XPathExpressionException, SAXException {
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(stream);
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		
		Node globalNode = selectNode(document, "//configuration/global");
		Global global = Global.fromXmlString(globalNode.toString());
		System.out.println(global);
		
		Node parserNodeName = selectNode(document, "//configuration/parser/name");
		System.out.println(parserNodeName.toString());
		
		Class<?> myClass = Class.forName(parserNodeName.toString());
		Constructor<?> cons = myClass.getConstructor();
		IParser parser = (IParser)cons.newInstance();
		parser.init(selectNode(document, "//configuration/parser/parserconfiguration").toString(), global);
		global.setNameSuggestion(parser.getNameSuggestion());
		
		List<IWriterPlugin> plugins = new ArrayList<IWriterPlugin>();
		NodeList writerNodes = (NodeList)xpath.compile("//configuration/writers/writer").evaluate(document, XPathConstants.NODESET);
		for(int i=0; i<writerNodes.getLength(); i++) {
			Node writerNode = writerNodes.item(i);
			String name = selectNode(writerNode, "name").getNodeValue();
			String xmlConfig = selectNode(writerNode, "writerconfiguration").getNodeValue();
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
