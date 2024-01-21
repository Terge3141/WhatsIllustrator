package imageconverter;

import java.nio.file.Path;

public class WebpConverter {
	
	public static void toPng(Path inputPath, Path outputDir) {
		FilenameUtils.removeExtension(inputPath);
	}
}
