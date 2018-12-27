package program;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import helper.Misc;

// Parses the old Soft Bank unicode characters to new unicode characters
// Table from https://github.com/iamcal/emoji-data
public class Program {

	private static Logger logger = LogManager.getLogger(Program.class);

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

	public static void main(String[] args) throws org.apache.commons.cli.ParseException, IOException, ParseException {
		// -i "/tmp/mychat" -e "/tmp/emojis" -imagepooldir "/tmp/imagepool"

		Config config = new Config();
		Options options = getOptions();
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, args);
			config.setInputDir(line.getOptionValue("inputdir"));
			config.setEmojiDir(line.getOptionValue("emojidir"));
			config.setImagePoolDir(line.getOptionValue("imagepooldir"));
			config.setDebugDir(line.getOptionValue("debugdir"));

			String outputDirStr = line.getOptionValue("outputdir");
			config.setOutputDir(Misc.isNullOrWhiteSpace(outputDirStr) ? config.getInputDir() : Paths.get(outputDirStr));

		} catch (MissingOptionException moe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("thebook", options);
			return;
		}

		BookCreator creator = new BookCreator(config.getInputDir(), config.getOutputDir(), config.getEmojiDir());
		creator.setImagePoolDir(config.getImagePoolDir());
		creator.writeTex();

		logger.info("Done");
	}

	// TODO Some Softbank icons don't have a mapping
	// TODO use streams
	// TODO file header everywhere
}
