package imagematcher.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import imagematcher.FileEntry;
import imagematcher.ImageMatcher;
import imagematcher.MatchEntry;

class ImageMatcherTest {
	@Test
	public void testPick_Load_SingleMatch() throws IOException  {
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		fileList.add(getFileEntry(2015, 1, 1, "file1.jpg"));

		LocalDateTime tp = LocalDateTime.of(2015, 1, 1, 13, 12, 0);
		List<MatchEntry> matchList = new ArrayList<MatchEntry>();
		MatchEntry matchEntry = new MatchEntry(tp, fileList, 0);
		matchList.add(matchEntry);
		ImageMatcher imWriter = new ImageMatcher();
		imWriter.setMatchList(matchList);
		String matchXML = imWriter.toXmlString();

		ImageMatcher imReader = ImageMatcher.fromXmlString(matchXML);
		imReader.setSearchMode(false);

		MatchEntry entry = imReader.pick(tp);
		assertTrue(entry.getTimePoint().equals(tp));

		List<FileEntry> fileMatches = entry.getFileMatches();
		assertEquals(1, fileMatches.size());
		assertEquals("file1.jpg", fileMatches.get(0).getFileName());
	}

	@Test
	public void testPick_Load_NoMatch() throws IOException  {
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		fileList.add(getFileEntry(2015, 1, 1, "file1.jpg"));

		LocalDateTime tpInput = LocalDateTime.of(2015, 1, 1, 13, 12, 0);
		List<MatchEntry> matchList = new ArrayList<MatchEntry>();
		MatchEntry matchEntry = new MatchEntry(tpInput, fileList, 0);
		matchList.add(matchEntry);
		ImageMatcher imWriter = new ImageMatcher();
		imWriter.setMatchList(matchList);
		String matchXML = imWriter.toXmlString();
		
		ImageMatcher imReader = ImageMatcher.fromXmlString(matchXML);
		imReader.setSearchMode(false);

		try {
			LocalDateTime tpSearch = LocalDateTime.of(2015, 1, 2, 13, 12, 0);
			imReader.pick(tpSearch);
			fail("No exception thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testPick_FileList_Match() {
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		fileList.add(getFileEntry(2015, 1, 1, "file1.jpg"));
		fileList.add(getFileEntry(2015, 1, 2, "file2.jpg"));
		fileList.add(getFileEntry(2015, 1, 3, "file3.jpg"));

		ImageMatcher im = new ImageMatcher();
		im.setFileList(fileList);
		im.setSearchMode(true);

		MatchEntry entry = im.pick(LocalDateTime.of(2015, 1, 2, 16, 10, 0), 0);
		assertEquals(1, entry.getFileMatches().size());
		assertEquals("file2.jpg", entry.getFileMatches().get(0).getFileName());
	}

	@Test
	public void testPick_FileList_NoMatch() {
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		fileList.add(getFileEntry(2015, 1, 1, "file1.jpg"));
		fileList.add(getFileEntry(2015, 1, 2, "file2.jpg"));
		fileList.add(getFileEntry(2015, 1, 3, "file3.jpg"));

		ImageMatcher im = new ImageMatcher();
		im.setFileList(fileList);
		im.setSearchMode(true);

		MatchEntry entry = im.pick(LocalDateTime.of(2015, 1, 4, 16, 10, 0), 0);
		assertEquals(0, entry.getFileMatches().size());
	}

	@Test
	public void testPick_FileList_Cnt() {
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		fileList.add(getFileEntry(2013, 10, 11, "file1.jpg"));
		fileList.add(getFileEntry(2013, 10, 11, "file2.jpg"));
		fileList.add(getFileEntry(2013, 10, 11, "file3.jpg"));

		ImageMatcher im = new ImageMatcher();
		im.setFileList(fileList);
		im.setSearchMode(true);

		MatchEntry entry1 = im.pick(LocalDateTime.of(2013, 10, 11, 8, 25, 0), 0);
		MatchEntry entry2 = im.pick(LocalDateTime.of(2013, 10, 11, 8, 25, 0), 1);

		assertEquals(0, entry1.getCnt());
		assertEquals(1, entry2.getCnt());
		assertEquals(3, entry1.getFileMatches().size());
		assertEquals(3, entry2.getFileMatches().size());
	}

	@Test
	public void testExcludeExcept() {
		List<FileEntry> fileList1 = new ArrayList<FileEntry>();
		fileList1.add(getFileEntry(2013, 10, 11, "file1.jpg"));
		fileList1.add(getFileEntry(2013, 10, 11, "file2.jpg"));
		fileList1.add(getFileEntry(2013, 10, 11, "file3.jpg"));

		List<FileEntry> fileList2 = new ArrayList<FileEntry>();
		fileList2.add(getFileEntry(2013, 10, 12, "file1.jpg"));
		fileList2.add(getFileEntry(2013, 10, 12, "file2.jpg"));
		fileList2.add(getFileEntry(2013, 10, 12, "file3.jpg"));

		MatchEntry matchEntry1 = new MatchEntry(LocalDateTime.of(2013, 10, 11, 8, 25, 0), fileList1, 0);
		MatchEntry matchEntry2 = new MatchEntry(LocalDateTime.of(2013, 10, 11, 8, 25, 0), fileList1, 1);
		MatchEntry matchEntry3 = new MatchEntry(LocalDateTime.of(2013, 10, 12, 8, 25, 0), fileList2, 0);
		ImageMatcher im = new ImageMatcher();
		im.setMatchList(Arrays.asList(matchEntry1, matchEntry2, matchEntry3));

		im.excludeExcept(LocalDateTime.of(2013, 10, 11, 8, 25, 0), "path/to/file2.jpg", 0);
		List<MatchEntry> ml = im.getMatchList();
		assertEquals(3, ml.size());
		assertEquals(1, ml.get(0).getFileMatches().size());
		assertEquals(3, ml.get(1).getFileMatches().size());
		assertEquals(3, ml.get(2).getFileMatches().size());

		FileEntry fe = ml.get(0).getFileMatches().get(0);
		assertEquals("path/to/file2.jpg", fe.getRelPath());
	}

	@Test
	public void testExcludeExcept_Exceptions() {
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		fileList.add(getFileEntry(2013, 10, 11, "file1.jpg"));
		fileList.add(getFileEntry(2013, 10, 11, "file2.jpg"));
		fileList.add(getFileEntry(2013, 10, 11, "file3.jpg"));

		MatchEntry matchEntry = new MatchEntry(LocalDateTime.of(2013, 10, 11, 8, 25, 0), fileList, 0);
		ImageMatcher im = new ImageMatcher();
		im.setMatchList(Arrays.asList(matchEntry));

		// no match entry found
		try {
			im.excludeExcept(LocalDateTime.of(2013, 10, 11, 8, 25, 0), "path/to/file2.jpg", 1);
			fail("No exception thrown for now match entry found");
		} catch (IllegalArgumentException iae) {
		}

		// no file entry found
		try {
			im.excludeExcept(LocalDateTime.of(2013, 10, 11, 8, 25, 0), "path/to/file4.jpg", 0);
			fail("No exception thrown for now file entry found");
		} catch (IllegalArgumentException iae) {
		}
	}

	@Test
	public void testExcludeExcept_ExcludeString() {
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		fileList.add(getFileEntry(2013, 10, 11, "file1.jpg"));
		fileList.add(getFileEntry(2013, 10, 11, "file2.jpg"));
		fileList.add(getFileEntry(2013, 10, 11, "file3.jpg"));

		MatchEntry matchEntry = new MatchEntry(LocalDateTime.of(2013, 10, 11, 8, 25, 0), fileList, 0);
		ImageMatcher im = new ImageMatcher();
		im.setMatchList(Arrays.asList(matchEntry));

		im.excludeExcept("2013-10-11T08:25;path/to/file2.jpg;0");

		assertEquals(1, im.getMatchList().size());

		List<FileEntry> fileMatches = im.getMatchList().get(0).getFileMatches();
		assertEquals(1, fileMatches.size());
		assertEquals("path/to/file2.jpg", fileMatches.get(0).getRelPath());
	}

	@Test
	public void testExcludeExcept_ExcludeString_Exception() {
		ImageMatcher im = new ImageMatcher();
		try {
			im.excludeExcept("a;b");
		} catch (IllegalArgumentException iae) {
		}
	}

	private FileEntry getFileEntry(int year, int month, int day, String filename) {
		LocalDate tp = LocalDate.of(year, month, day);
		String rp = String.format("path/to/%s", filename);
		FileEntry fe = new FileEntry();
		fe.setTimePoint(tp);
		fe.setRelPath(rp);
		fe.setFileName(filename);
		return fe;
	}
}
