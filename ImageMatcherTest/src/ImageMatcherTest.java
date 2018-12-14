import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import imagematcher.*;

class ImageMatcherTest {

	@Test
	public void testFromXml() throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException {
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		fileList.add(getFileEntry(2015, 1, 1, "file1.jpg"));

		LocalDateTime tp = LocalDateTime.of(2015, 1, 1, 13, 12, 0);
		List<MatchEntry> matchList = new ArrayList<MatchEntry>();
		MatchEntry matchEntry = new MatchEntry(tp, fileList, 0);
		matchList.add(matchEntry);
		ImageMatcher imWriter = new ImageMatcher();
		imWriter.setMatchList(matchList);
		String xml = imWriter.toXml();
		
		//Files.write(Paths.get("/tmp/out.xml"), xml));
	}
	
	@Test
	public void testPick_Load_SingleMatch()
			throws ParserConfigurationException, SAXException, IOException, ParseException, TransformerFactoryConfigurationError, TransformerException {
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		fileList.add(getFileEntry(2015, 1, 1, "file1.jpg"));

		LocalDateTime tp = LocalDateTime.of(2015, 1, 1, 13, 12, 0);
		List<MatchEntry> matchList = new ArrayList<MatchEntry>();
		MatchEntry matchEntry = new MatchEntry(tp, fileList, 0);
		matchList.add(matchEntry);
		ImageMatcher imWriter = new ImageMatcher();
		imWriter.setMatchList(matchList);
		String matchXML = imWriter.toXml();

		ImageMatcher imReader = ImageMatcher.fromXml(matchXML);
		imReader.setSearchMode(false);

		MatchEntry entry = imReader.pick(tp);
		assertTrue(entry.getTimePoint().equals(tp));

		List<FileEntry> fileMatches = entry.getFileMatches();
		assertEquals(1, fileMatches.size());
		assertEquals("file1.jpg", fileMatches.get(0).getFileName());
	}

	@Test
	public void testPick_Load_NoMatch() throws ParserConfigurationException, SAXException, IOException, ParseException, TransformerFactoryConfigurationError, TransformerException {
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		fileList.add(getFileEntry(2015, 1, 1, "file1.jpg"));

		LocalDateTime tpInput = LocalDateTime.of(2015, 1, 1, 13, 12, 0);
		List<MatchEntry> matchList = new ArrayList<MatchEntry>();
		MatchEntry matchEntry = new MatchEntry(tpInput, fileList, 0);
		matchList.add(matchEntry);
		ImageMatcher imWriter = new ImageMatcher();
		imWriter.setMatchList(matchList);
		String matchXML = imWriter.toXml();

		ImageMatcher imReader = ImageMatcher.fromXml(matchXML);
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

	private FileEntry getFileEntry(int year, int month, int day, String filename) {
		LocalDateTime tp = LocalDateTime.of(year, month, day, 0, 0);
		String rp = String.format("path/to/%s", filename);
		FileEntry fe = new FileEntry();
		fe.setTimePoint(tp);
		fe.setRelPath(rp);
		fe.setFileName(filename);
		return fe;
	}
}
