package helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FileHandler {

	/**
	 * Walks a directory recursively and returns the absolute paths of files that
	 * match the regular expression.
	 * 
	 * @param dir Directory to be searched
	 * @param regex Regular expression
	 * @return List of the files
	 * @throws IOException
	 */
	public static List<String> listDir(Path dir, final String regex) throws IOException {
		return Files.walk(dir).filter(x -> Files.isRegularFile(x))
				.filter(x -> x.getFileName().toString().matches(regex)).map(x -> x.toAbsolutePath().toString())
				.collect(Collectors.toList());
	}
}
