package helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FileHandler {

	public static List<String> listDir(Path dir, final String searchPattern) throws IOException {
		return Files.walk(dir).filter(x -> Files.isRegularFile(x))
				.filter(x -> x.getFileName().toString().matches(searchPattern)).map(x -> x.toAbsolutePath().toString())
				.collect(Collectors.toList());
	}
}
