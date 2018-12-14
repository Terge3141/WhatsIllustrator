package helpertest;

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
		String dir = folder.newFolder("listDir").toString();

		Path file1 = Paths.get(dir, "a.txt");
		Path file2 = Paths.get(dir, "b.dat");
		Path file3 = Paths.get(dir, "c.txt");

		Files.createFile(file1);
		Files.createFile(file2);
		Files.createFile(file3);

		List<String> list1 = helper.FileHandler.listDir(dir.toString(), ".*");
		Collections.sort(list1);
		assertEquals(3, list1.size());
		assertEquals(file1.toString(), list1.get(0));
		assertEquals(file2.toString(), list1.get(1));
		assertEquals(file3.toString(), list1.get(2));

		List<String> list2 = helper.FileHandler.listDir(dir.toString(),
				".*.txt");
		Collections.sort(list2);
		assertEquals(2, list2.size());
		assertEquals(file1.toString(), list1.get(0));
		assertEquals(file3.toString(), list1.get(2));
	}
	
	@Test
	public void testListDir_Recursive() throws IOException {
		String dir = folder.newFolder("listDir_recursive").toString();
		Path subDirPath = Paths.get(dir, "subdir");
		
		Files.createDirectory(subDirPath);
						
		Path fileDir = Paths.get(dir, "a.txt");
		Path fileSubDir = Paths.get(subDirPath.toString(), "b.txt");
		
		Files.createFile(fileDir);
		Files.createFile(fileSubDir);
		List<String> list = helper.FileHandler.listDir(dir.toString(), ".*");
		Collections.sort(list);
		assertEquals(2, list.size());
		assertEquals(fileDir.toString(), list.get(0));
		assertEquals(fileSubDir.toString(), list.get(1));
	}

	@Test
	public void testGetFileName() {
		assertEquals("b.txt", helper.FileHandler.getFileName("/blub/b.txt"));
	}
	
	@Test
	public void testFileExists() throws IOException{
		String dir = folder.newFolder("fileExists").toString();
		Path fileExist = Paths.get(dir, "a.txt");
		Path fileNotExist = Paths.get(dir, "b.txt");
		
		Files.createFile(fileExist);
		
		assertTrue(helper.FileHandler.fileExists(fileExist.toString()));
		assertFalse(helper.FileHandler.fileExists(fileNotExist.toString()));
	}
}
