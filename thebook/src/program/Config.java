package program;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
	private Path inputDir;
	private Path outputDir;
	private Path emojiDir;
	private Path imagePoolDir;
	private Path debugDir;

	public Path getInputDir() {
		return inputDir;
	}

	public void setInputDir(Path inputDir) throws FileNotFoundException {
		check(inputDir);
		this.inputDir = inputDir;
	}

	public void setInputDir(String inputDir) throws FileNotFoundException {
		setInputDir(getDir(inputDir));
	}

	public Path getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(Path outputDir) throws FileNotFoundException {
		check(outputDir);
		this.outputDir = outputDir;
	}

	public void setOutputDir(String outputDir) throws FileNotFoundException {
		setOutputDir(getDir(outputDir));
	}

	public Path getEmojiDir() {
		return emojiDir;
	}

	public void setEmojiDir(Path emojiDir) throws FileNotFoundException {
		check(emojiDir);
		this.emojiDir = emojiDir;
	}

	public void setEmojiDir(String emojiDir) throws FileNotFoundException {
		setEmojiDir(getDir(emojiDir));
	}

	public Path getImagePoolDir() {
		return imagePoolDir;
	}

	public void setImagePoolDir(Path imagePoolDir) throws FileNotFoundException {
		check(imagePoolDir);
		this.imagePoolDir = imagePoolDir;
	}

	public void setImagePoolDir(String imagePoolDir) throws FileNotFoundException {
		setImagePoolDir(getDir(imagePoolDir));
	}

	public Path getDebugDir() {
		return debugDir;
	}

	public void setDebugDir(Path debugDir) throws FileNotFoundException {
		check(debugDir);
		this.debugDir = debugDir;
	}

	public void setDebugDir(String debugDir) throws FileNotFoundException {
		setDebugDir(getDir(debugDir));
	}

	private Path getDir(String dirStr) {
		if (dirStr == null) {
			return null;
		} else {

			return Paths.get(dirStr);
		}
	}

	private void check(Path dir) throws FileNotFoundException {
		if(dir==null) {
			return;
		}
		
		if (!dir.toFile().isDirectory()) {
			throw new FileNotFoundException(String.format("Directory '%s' does not exist", dir.toString()));
		}
	}
}
