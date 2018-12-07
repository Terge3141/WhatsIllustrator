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
	
	public static void main(String[] args){
		String path = "/home/michael/whatsappprint/chats/data/tst/chat";
		List<String> bla = FileHandler.listDir(path, ".*.txt");
		int a = 1;
		/*String str = "filename.dat";
		boolean b = str.matches(".*.txt");
		int a = 1;*/
	}
}
