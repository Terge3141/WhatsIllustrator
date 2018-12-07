package helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Misc {

	public static String ReadAllText(String path) throws IOException {
		StringBuilder sb = new StringBuilder();
		List<String> lines = Files.readAllLines(Paths.get(path));
		for (String line : lines) {
			sb.append(line);
			sb.append("\n");
		}

		return sb.toString();
	}

	public static void WriteAllText(String path, String text)
			throws IOException {
		PrintWriter writer = new PrintWriter(path);
		writer.print(text);
		writer.close();
	}

	public static boolean IsNullOrEmpty(String str) {
		if (str == null) {
			return true;
		}

		return str.isEmpty();
	}

	public static boolean IsNullOrWhiteSpace(String str) {
		if (str == null) {
			return true;
		}

		return str.trim().isEmpty();
	}

	public static boolean ArrayContains(String[] arr, String needle) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(needle)) {
				return true;
			}
		}

		return false;
	}

	public static List<String> ListDir(String path, final String searchPattern) {
		File dir = new File(path);

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches(searchPattern);
				// return true;
			}
		};

		List<String> list = new ArrayList<String>();
		for (File file : dir.listFiles(filter)) {
			list.add(file.getAbsolutePath());
		}

		return list;
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
