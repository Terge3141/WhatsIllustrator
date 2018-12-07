package helper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

	public static List<String> listDir(String path, final String searchPattern) {
		File dir = new File(path);
	
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches(searchPattern);
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
