package helper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Misc {

	public static String readAllText(String path) throws IOException {
		StringBuilder sb = new StringBuilder();
		List<String> lines = Files.readAllLines(Paths.get(path));
		for (String line : lines) {
			sb.append(line);
			sb.append("\n");
		}

		return sb.toString();
	}

	public static void writeAllText(String path, String text) throws IOException {
		PrintWriter writer = new PrintWriter(path);
		writer.print(text);
		writer.close();
	}

	public static boolean isNullOrEmpty(String str) {
		if (str == null) {
			return true;
		}

		return str.isEmpty();
	}

	public static boolean isNullOrWhiteSpace(String str) {
		if (str == null) {
			return true;
		}

		return str.trim().isEmpty();
	}

	public static boolean arrayContains(String[] arr, String needle) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(needle)) {
				return true;
			}
		}

		return false;
	}

	public static boolean listContains(List<String> list, String needle) {
		String[] arr = list.toArray(new String[list.size()]);
		return arrayContains(arr, needle);
	}
}
