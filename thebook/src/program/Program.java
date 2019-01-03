package program;

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

import creator.BookCreator;
import creator.plugins.IWriterPlugin;
import creator.plugins.odf.OdfWriterPlugin;
import creator.plugins.tex.TexWriterPlugin;
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

	public static void main(String[] args) throws Exception {
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

		long start = System.currentTimeMillis();
		List<IWriterPlugin> plugins = new ArrayList<IWriterPlugin>();
		plugins.add(new TexWriterPlugin());
		plugins.add(new OdfWriterPlugin());

		BookCreator creator = new BookCreator(config.getInputDir(), config.getOutputDir(), config.getEmojiDir(),
				plugins);
		creator.getWriterConfig().setImagePoolDir(config.getImagePoolDir());
		creator.write();

		long stop = System.currentTimeMillis();
		double seconds = 0.001 * (stop - start);

		logger.info("Done {}", seconds);
	}

	// TODO Some Softbank icons don't have a mapping
	// TODO use streams
	// TODO file header everywhere
}
