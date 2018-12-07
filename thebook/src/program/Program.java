package program;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;

import helper.FileHandler;
import helper.Misc;
import helper.Container;

public class Program {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main2(String[] args) throws IOException {
		// TODO read command line arguments
		String inputDir = "SETPATH";

		Config config = new Config();

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

	public static void main(String[] args) throws IOException {
		String str = Misc
				.readAllText("/home/michael/whatsappprint/whatsbook/smiler.txt");
		System.out.println(convert16to32(str));
	}

	public static String convert16to32(String toConvert) {
		for (int i = 0; i < toConvert.length();) {
			int codePoint = Character.codePointAt(toConvert, i);
			i += Character.charCount(codePoint);
			// System.out.printf("%x%n", codePoint);
			String utf32 = String.format("0x%x%n", codePoint);
			return utf32;
		}
		return null;
	}
}
