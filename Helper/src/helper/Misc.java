package helper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.text.TextStringBuilder;

public class Misc {

	/**
	 * Return the content of a given file to a string.
	 * @param path The path of the file
	 * @return The content of the file
	 * @throws IOException
	 */
	public static String readAllText(Path path) throws IOException {
		TextStringBuilder tsb = new TextStringBuilder();
		List<String> lines = Files.readAllLines(path);
		for (String line : lines) {
			tsb.appendln(line);
		}

		return tsb.toString();
	}

	/**
	 * Writes as string to a given file
	 * @param path Path of the file
	 * @param text Text to be written
	 * @throws IOException
	 */
	public static void writeAllText(Path path, String text) throws IOException {
		PrintWriter writer = new PrintWriter(path.toFile());
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
