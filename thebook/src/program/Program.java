package program;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import helper.Misc;
import helper.Container;

public class Program {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO read command line arguments
		String inputDir = "/tmp/mychat";
		String emojiDir="/tmp/emojis";

		Config config = new Config();
		config.InputDir = inputDir;
		config.EmojiDir=emojiDir;

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
	
	public static void main1(String[] args){
		String str = "08/08/2011, 18:20 - melvers: Hallo!";
		//boolean b = str.matches("^[0-9].*");
		//boolean b = str.matches("^[0-3][0-9]/[0-1][0-9]/[0-9]{4},\\ [0-2][0-9]:[0-5][0-9].*");
		//System.out.println(b);
		Pattern p = Pattern.compile("^[0-3][0-9]/[0-1][0-9]/[0-9]{4},\\ [0-2][0-9]:[0-5][0-9]");
		Matcher m = p.matcher(str);
		System.out.println(m.find());
		System.out.println(m.start());
		System.out.println(m.end());
	}

	// TODO use of get/set
	// TODO org.apache.commons.lang3.text.StrBuilder

	public static void main2(String[] args) throws IOException {
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
