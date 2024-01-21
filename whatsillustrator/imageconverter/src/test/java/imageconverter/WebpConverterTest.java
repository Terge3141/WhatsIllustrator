package imageconverter;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WebpConverterTest {

	@Test
	void testToPng(@TempDir Path tmpDir) throws IOException {
		Path webpPath = tmpDir.resolve("mytest.webp");
		Path pngComparePath = tmpDir.resolve("mytest.expected.png");
		Path pngActualPath = tmpDir.resolve("mytest.png");

		writeResource("/imageconverter/mytest.webp", webpPath);
		writeResource("/imageconverter/mytest.png", pngComparePath);

		Path p = WebpConverter.toPng(webpPath, tmpDir);
		assertEquals(pngActualPath, p);
		
		assertTrue(pngActualPath.toFile().exists());
		assertImagesEqual(pngComparePath, pngActualPath);
	}
	
	private void assertImagesEqual(Path pngExpectedPath, Path pngActualPath) throws IOException {
		BufferedImage imgExpected = ImageIO.read(pngExpectedPath.toFile());
		BufferedImage imgActual = ImageIO.read(pngActualPath.toFile());

		assertEquals(imgExpected.getHeight(), imgActual.getHeight());
		assertEquals(imgExpected.getWidth(), imgActual.getWidth());
		
		double buf = 0.0;

		for (int x = 0; x < imgExpected.getWidth(); x++) {
			for (int y = 0; y < imgExpected.getHeight(); y++) {
				int pixelExpected = imgExpected.getRGB(x, y);
				int pixelActual = imgActual.getRGB(x, y);
				buf += squaredRGBADiff(pixelExpected, pixelActual);
			}
		}
		
		// pixel values aren't 100% identical
		buf = Math.sqrt(buf);
		assertTrue(buf < 1500.);
	}
	
	double squaredRGBADiff(int rgba1, int rgba2) {
		int alpha1 = (rgba1 >> 24) & 255;
        int red1 = (rgba1 >> 16) & 255;
        int green1 = (rgba1 >> 8) & 255;
        int blue1 = rgba1 & 255;
        
        int alpha2 = (rgba2 >> 24) & 255;
        int red2 = (rgba2 >> 16) & 255;
        int green2 = (rgba2 >> 8) & 255;
        int blue2 = rgba2 & 255;
        
        double buf = 0.0;
        buf += squaredDiff(alpha1, alpha2);
        buf += squaredDiff(red1, red2);
        buf += squaredDiff(green1, green2);
        buf += squaredDiff(blue1, blue2);
        
        return buf;
	}
	
	double squaredDiff(int a, int b) {
		double ad = (double)a;
		double bd = (double)b;
		
		return (ad-bd)*(ad-bd);
	}
	
	void dump(int rgba) {
		int alpha = (rgba >> 24) & 255;
        int red = (rgba >> 16) & 255;
        int green = (rgba >> 8) & 255;
        int blue = rgba & 255;
        
        System.out.println("\t alpha " + alpha);
        System.out.println("\t red   " + red);
        System.out.println("\t green " + green);
        System.out.println("\t blue  " + blue);
	}

	void writeResource(String src, Path dst) throws IOException {
		Files.copy(getClass().getResourceAsStream(src), dst, StandardCopyOption.REPLACE_EXISTING);
	}
}
