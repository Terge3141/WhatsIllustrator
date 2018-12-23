package imagematcher.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import imagematcher.ImageMatcher;

public class Excluder {
	public static Options getOptions() {
		Options options = new Options();
		options.addRequiredOption("m", "matchfile", true, "Path of the match.xml input file");
		options.addRequiredOption("e", "excludefile", true,
				"Input path to the file with exclude lines. Each line consists of timepoint;Sent/relpath;cnt");
		options.addRequiredOption("o", "outmatchfile", true, "Path of the match.xml output file");

		return options;
	}

	public static void main(String[] args) throws IOException, ParseException, ParserConfigurationException,
			SAXException, java.text.ParseException, TransformerFactoryConfigurationError, TransformerException {
		Options options = getOptions();
		CommandLineParser parser = new DefaultParser();
		String inputMatchPath;
		String outputMatchPath;
		String excludePath;
		try {
			CommandLine commandLine = parser.parse(options, args);
			inputMatchPath = commandLine.getOptionValue("m");
			outputMatchPath = commandLine.getOptionValue("o");
			excludePath = commandLine.getOptionValue("e");
		} catch (MissingOptionException moe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("thebook", options);
			return;
		}

		System.out.format("Reading xml from '%s'\n", inputMatchPath);
		ImageMatcher im = ImageMatcher.fromXmlFile(Paths.get(inputMatchPath));

		System.out.format("Reading exlude from '%s'\n", excludePath);
		List<String> lines = Files.readAllLines(Paths.get(excludePath));
		for (String line : lines) {
			im.excludeExcept(line);
		}

		System.out.format("Writing output file to '%s'\n", outputMatchPath);
		im.toXmlFile(Paths.get(outputMatchPath));
	}

}
