package texcreator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TexWriterPluginDirMocker {
	private Path tmpDir;
	private Path inputDir;
	private Path outputDir;
	private Path texDir;
	private Path emojiDir;
	private Path imageDir;
	
	
	public TexWriterPluginDirMocker(Path tmpDir) throws IOException {
		this.tmpDir = tmpDir;
		
		this.inputDir = this.tmpDir.resolve("input");
		Files.createDirectories(this.inputDir);
		
		this.outputDir = tmpDir.resolve("output");
		this.texDir = outputDir.resolve("Myname").resolve("tex");
		this.emojiDir = texDir.resolve("emojis");
		this.imageDir  = texDir.resolve("images");
	}

	public Path getInputDir() {
		return inputDir;
	}

	public Path getOutputDir() {
		return outputDir;
	}

	public Path getTexDir() {
		return texDir;
	}
	
	public Path getEmojiDir() {
		return emojiDir;
	}
	
	public Path getImageDir() {
		return imageDir;
	}
}
