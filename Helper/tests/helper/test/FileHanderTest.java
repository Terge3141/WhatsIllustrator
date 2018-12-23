package helper.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileHanderTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testListDir() throws IOException {
		Path dir = folder.newFolder("listDir").toPath();

		Path file1 = dir.resolve("a.txt");
		Path file2 = dir.resolve("b.dat");
		Path file3 = dir.resolve("c.txt");

		Files.createFile(file1);
		Files.createFile(file2);
		Files.createFile(file3);

		List<String> list1 = helper.FileHandler.listDir(dir, ".*");
		Collections.sort(list1);
		assertEquals(3, list1.size());
		assertEquals(file1.toString(), list1.get(0));
		assertEquals(file2.toString(), list1.get(1));
		assertEquals(file3.toString(), list1.get(2));

		List<String> list2 = helper.FileHandler.listDir(dir, ".*.txt");
		Collections.sort(list2);
		assertEquals(2, list2.size());
		assertEquals(file1.toString(), list1.get(0));
		assertEquals(file3.toString(), list1.get(2));
	}

	@Test
	public void testListDir_Recursive() throws IOException {
		Path dir = folder.newFolder("listDir_recursive").toPath();
		Path subDirPath = dir.resolve("subdir");

		Files.createDirectory(subDirPath);

		Path fileDir = dir.resolve("a.txt");
		Path fileSubDir = Paths.get(subDirPath.toString(), "b.txt");

		Files.createFile(fileDir);
		Files.createFile(fileSubDir);
		List<String> list = helper.FileHandler.listDir(dir, ".*");
		Collections.sort(list);
		assertEquals(2, list.size());
		assertEquals(fileDir.toString(), list.get(0));
		assertEquals(fileSubDir.toString(), list.get(1));
	}
}
