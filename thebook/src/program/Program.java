package program;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import imagematcher.*;
import helper.Misc;
import helper.Container;

// Parses the old Soft Bank unicode characters to new unicode characters
// Table from https://github.com/iamcal/emoji-data
public class Program {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main2(String[] args) throws IOException {
		// TODO read command line arguments
		String inputDir = "/tmp/mychat";
		String emojiDir = "/tmp/emojis";

		Config config = new Config();
		config.InputDir = inputDir;
		config.EmojiDir = emojiDir;

		config.OutputDir = Misc.isNullOrWhiteSpace(config.OutputDir) ? config.InputDir
				: config.OutputDir;

		if (!Misc.isNullOrWhiteSpace(config.DebugDir)) {
			Container.Debug = new PrintWriter(Paths.get(config.DebugDir,
					"output.log").toString());
		}

		BookCreator creator = new BookCreator(config.InputDir,
				config.OutputDir, config.EmojiDir);
		creator.ImagePoolDir = config.OutputDir;
		creator.writeTex();

		if (Container.Debug != null) {
			Container.Debug.close();
			Container.Debug = null;
		}
	}

	// TODO use of get/set
	// TODO org.apache.commons.lang3.text.StrBuilder
	// TODO Some Softbank icons don't have a mapping

	public static void main(String[] args) throws IOException,
			ParserConfigurationException, SAXException, ParseException {
		/*String path = "/tmp/try.xml";
		
		String xml = Misc.readAllText(path);
		
		ImageMatcher.FromXml(xml);*/
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm:ss");
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss");
		LocalDate parsedDate = LocalDate.parse("2013-03-31T21:08:00", formatter);
	}

}
