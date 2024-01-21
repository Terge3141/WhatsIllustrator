package imageconverter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

public class WebpConverter {
	
	public static void toPng(Path inputPath, Path outputDir) throws IOException {
		Path outputPath = outputDir.resolve(FilenameUtils.removeExtension(inputPath.toString()) + ".png");
		
		BufferedImage img = ImageIO.read(inputPath.toFile());
		ImageIO.write(img, "png", outputPath.toFile());
	}
}
