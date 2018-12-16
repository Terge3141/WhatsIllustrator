package program;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.ParseException;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.xml.sax.SAXException;

import helper.Misc;
import helper.Container;

// Parses the old Soft Bank unicode characters to new unicode characters
// Table from https://github.com/iamcal/emoji-data
public class Program {

	public static Options getOptions() {
		Options options = new Options();
		options.addRequiredOption("i", "inputdir", true,
				"Input directory, should contain a subdirectory 'chat' where conversion and images is stored");
		options.addRequiredOption("e", "emojidir", true, "Directory where the emoji png images are stored");
		options.addOption("o", "outputdir", true, "Output directory, default is input directory");
		options.addOption("imagepooldir", true,
				"Directory of the image pool. Only used when 'media omitted' messages are found");
		options.addOption("debugdir", true, "Directory where debug information is stored");

		return options;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws TransformerFactoryConfigurationError
	 * @throws                                      org.apache.commons.cli.ParseException
	 */
	public static void main(String[] args)
			throws IOException, ParserConfigurationException, SAXException, ParseException,
			TransformerFactoryConfigurationError, TransformerException, org.apache.commons.cli.ParseException {
		// -i "/tmp/mychat" -e "/tmp/emojis" -imagepooldir "/tmp/imagepool"

		Config config = new Config();
		Options options = getOptions();
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, args);
			config.setInputDir(line.getOptionValue("inputdir"));
			config.setEmojiDir(line.getOptionValue("emojidir"));
			config.setOutputDir(line.getOptionValue("outputdir"));
			config.setImagePoolDir(line.getOptionValue("imagepooldir"));
			config.setDebugDir(line.getOptionValue("debugdir"));

		} catch (MissingOptionException moe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("thebook", options);
			return;
		}

		config.setOutputDir(
				Misc.isNullOrWhiteSpace(config.getOutputDir()) ? config.getInputDir() : config.getOutputDir());

		if (!Misc.isNullOrWhiteSpace(config.getDebugDir())) {
			Container.Debug = new PrintWriter(Paths.get(config.getDebugDir(), "output.log").toString());
		}

		BookCreator creator = new BookCreator(config.getInputDir(), config.getOutputDir(), config.getEmojiDir());
		creator.setImagePoolDir(config.getImagePoolDir());
		creator.writeTex();

		System.out.println("Done");

		if (Container.Debug != null) {
			Container.Debug.close();
			Container.Debug = null;
		}
	}

	// TODO Some Softbank icons don't have a mapping
	// TODO check were unit tests should be placed
	// TODO use streams
}
