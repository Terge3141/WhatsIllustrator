package helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileHandler {

	public static List<String> listDir(String dir, final String searchPattern) throws IOException {

		/*
		 * FilenameFilter filter = new FilenameFilter() { public boolean accept(File
		 * dir, String name) { return name.matches(searchPattern); } };
		 * 
		 * List<String> list = new ArrayList<String>(); for (File file :
		 * dir.listFiles(filter)) { list.add(file.getAbsolutePath()); }
		 * 
		 * return list;
		 */
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
