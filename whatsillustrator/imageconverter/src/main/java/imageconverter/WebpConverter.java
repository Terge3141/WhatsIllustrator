package imageconverter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

public class WebpConverter {
	
	public static Path toPng(Path inputPath, Path outputDir) throws IOException {
		String fileName = inputPath.getFileName().toString();
		Path outputPath = outputDir.resolve(FilenameUtils.removeExtension(fileName) + ".png");
		
		BufferedImage img = ImageIO.read(inputPath.toFile());
		ImageIO.write(img, "png", outputPath.toFile());
		
		return outputPath;
	}
}
