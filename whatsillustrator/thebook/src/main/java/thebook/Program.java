package thebook;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.Node;

import configurator.Global;
import creator.BookCreator;
import creator.plugins.IWriterPlugin;
import fopcreator.FOPWriterPlugin;
import odfcreator.OdfWriterPlugin;
import texcreator.TexWriterPlugin;
import helper.Misc;
import messageparser.IParser;

public class Program {

	private static Logger logger;

	public static Options getOptions() {
		Options options = new Options();
		options.addRequiredOption("c", "config", true, "File to xml config file");

		return options;
	}
	
	private static <T> T createObject(String className) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> myClass = Class.forName(className);
		Constructor<?> cons = myClass.getConstructor();
		return (T)cons.newInstance();
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

		SAXReader reader = new SAXReader();
		String xml = Misc.readAllText(Paths.get(xmlConfigFile));
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));
		Document document = reader.read(stream);
		
		Node globalNode = document.selectSingleNode("//configuration/global");
		Global global = Global.fromXmlString(globalNode.asXML());
		System.out.println(global);
		
		Node parserNodeName = document.selectSingleNode("//configuration/parser/name");
		System.out.println(parserNodeName.getStringValue());
		
		Class<?> myClass = Class.forName(parserNodeName.getStringValue());
		Constructor<?> cons = myClass.getConstructor();
		IParser parser = (IParser)cons.newInstance();
		parser.init(document.selectSingleNode("//configuration/parser/parserconfiguration").asXML(), global);
		
		List<IWriterPlugin> plugins = new ArrayList<IWriterPlugin>();
		List<Node> writerNodes = document.selectNodes("//configuration/writers/writer");
		for(Node writerNode : writerNodes) {
			String name = writerNode.selectSingleNode("name").getStringValue();
			String xmlConfig = writerNode.selectSingleNode("writerconfiguration").asXML();
			IWriterPlugin plugin = createObject(name);
			plugin.preAppend(xmlConfig, global);
			plugins.add(plugin);
		}
		
		long start = System.currentTimeMillis();
		
		BookCreator creator = new BookCreator(global, parser, plugins);
		creator.write();

		long stop = System.currentTimeMillis();
		double seconds = 0.001 * (stop - start);

		logger.info("Done {}", seconds);
	}
}
