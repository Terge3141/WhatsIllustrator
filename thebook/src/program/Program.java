package program;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.stream.Stream;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;

import helper.Misc;
import helper.Container;

// Parses the old Soft Bank unicode characters to new unicode characters
// Table from https://github.com/iamcal/emoji-data
public class Program {

	/**
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws TransformerFactoryConfigurationError
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException,
			ParseException, TransformerFactoryConfigurationError, TransformerException {
		// TODO read command line arguments
		String inputDir = "/tmp/mychat";
		String emojiDir = "/tmp/emojis";
		String imagePoolDir = "/tmp/imagepool";

		Config config = new Config();
		config.InputDir = inputDir;
		config.EmojiDir = emojiDir;
		config.ImagePoolDir = imagePoolDir;

		config.OutputDir = Misc.isNullOrWhiteSpace(config.OutputDir) ? config.InputDir : config.OutputDir;

		if (!Misc.isNullOrWhiteSpace(config.DebugDir)) {
			Container.Debug = new PrintWriter(Paths.get(config.DebugDir, "output.log").toString());
		}

		BookCreator creator = new BookCreator(config.InputDir, config.OutputDir, config.EmojiDir);
		creator.ImagePoolDir = config.ImagePoolDir;
		creator.writeTex();

		System.out.println("Done");

		if (Container.Debug != null) {
			Container.Debug.close();
			Container.Debug = null;
		}
	}

	// TODO use of get/set
	// TODO org.apache.commons.lang3.text.StrBuilder
	// TODO Some Softbank icons don't have a mapping
	// TODO use streams

	public static void main3(String[] args)
			throws IOException, ParserConfigurationException, SAXException, ParseException {
		ArrayList<String> list = new ArrayList<>();
		list.add("One");
		list.add("OneAndOnly");
		list.add("Derek");
		list.add("Change");
		list.add("factory");
		list.add("justBefore");
		list.add("Italy");
		list.add("Italy");
		list.add("Thursday");
		list.add("");
		list.add("");

		Stream<String> stream = list.stream().filter(element -> element.contains("d"));
		stream.forEach(x -> System.out.println(x));
	}

}
