package program;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.ParseException;

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
		config.setInputDir(inputDir);
		config.setEmojiDir(emojiDir);
		config.setImagePoolDir(imagePoolDir);

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

	// TODO use of get/set
	// TODO org.apache.commons.lang3.text.StrBuilder
	// TODO Some Softbank icons don't have a mapping
	// TODO use streams
}
