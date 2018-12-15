package helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileHandler {

	public static List<String> listDir(String dir, final String searchPattern) throws IOException {
		return Files.walk(Paths.get(dir)).filter(x -> Files.isRegularFile(x))
				.filter(x -> x.getFileName().toString().matches(searchPattern)).map(x -> x.toAbsolutePath().toString())
				.collect(Collectors.toList());
	}

	public static String getFileName(String path) {
		File f = new File(path);
		return f.getName();
	}

	public static boolean fileExists(String path) {
		File f = new File(path);
		if (f.isDirectory()) {
			return false;
		}

		return f.exists();
	}

}
