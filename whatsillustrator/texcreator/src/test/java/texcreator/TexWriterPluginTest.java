package texcreator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import configurator.Global;
import creator.plugins.WriterException;
import helper.Misc;
import messageparser.ImageMessage;
import messageparser.LinkMessage;
import messageparser.MediaMessage;
import messageparser.TextMessage;
import messageparser.VideoMessage;

class TexWriterPluginTest {

	@Test
	void testWorkflow(@TempDir Path tmpDir) throws IOException {
		TexWriterPluginDirMocker dm = new TexWriterPluginDirMocker(tmpDir);
		
		TexWriterPlugin twp = new TexWriterPlugin();
		try {
			twp.preAppend("", createGlobalConfig(dm.getOutputDir(), "Myname"));
			twp.appendDateHeader(LocalDateTime.of(2023, 1, 22, 13, 51, 12));
			twp.appendTextMessage(new TextMessage(LocalDateTime.of(2023, 1, 22, 13, 52, 12), "From", "This is the message text"));
			twp.appendLinkMessage(new LinkMessage(LocalDateTime.of(2023, 1, 22, 13, 52, 14), "From", "http://github.com"));
			twp.appendMediaMessage(new MediaMessage(LocalDateTime.of(2023, 1, 22, 13, 52, 15), "From", "Filename", "Subscription"));
			twp.postAppend();
		} catch (WriterException e) {
			fail(e);
		}
		
		checkFile(dm.getTexDir().resolve("Myname.tex"));
	}
	
	@Test
	void testEmoji(@TempDir Path tmpDir) throws IOException {
		TexWriterPluginDirMocker dm = new TexWriterPluginDirMocker(tmpDir);

		String text = "This is an emoji: " + new String(Character.toChars(0x1F601));
		TextMessage tm = new TextMessage(LocalDateTime.of(2023, 1, 22, 14, 24, 12), "From", text);
		
		TexWriterPlugin twp = new TexWriterPlugin();
		try {
			twp.preAppend("", createGlobalConfig(dm.getOutputDir(), "Myname"));
			twp.appendTextMessage(tm);
			twp.postAppend();
		} catch (WriterException e) {
			fail(e);
		}
		
		checkFile(dm.getTexDir().resolve("Myname.tex"));
		checkFile(dm.getEmojiDir().resolve("1f601.png"));
	}
	
	@Test
	void testImage(@TempDir Path tmpDir) throws IOException {
		TexWriterPluginDirMocker dm = new TexWriterPluginDirMocker(tmpDir);
		Path imagePath = dm.getInputDir().resolve("image.jpg");
		Misc.writeAllText(imagePath, "DUMMYIMAGETEXT12345");

		String text = "This is an image";
		
		TexWriterPlugin twp = new TexWriterPlugin();
		try {
			twp.preAppend("", createGlobalConfig(dm.getOutputDir(), "Myname"));
			twp.appendImageMessage(new ImageMessage(LocalDateTime.of(2023, 1, 22, 13, 52, 13), "From", imagePath, text));
			twp.postAppend();
		} catch (WriterException e) {
			fail(e);
		}
		
		checkFile(dm.getTexDir().resolve("Myname.tex"));
		checkFile(dm.getImageDir().resolve("image.jpg"), "DUMMYIMAGETEXT12345");
	}
	
	@Test
	void testVideo(@TempDir Path tmpDir) throws IOException {
		TexWriterPluginDirMocker dm = new TexWriterPluginDirMocker(tmpDir);
		
		String resourceName = "sample.mp4";
		Path videoPath = dm.getInputDir().resolve(resourceName);
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(resourceName).getFile());
		Files.copy(file.toPath(), videoPath, StandardCopyOption.REPLACE_EXISTING);
		
		String text = "This is a video";
		TexWriterPlugin twp = new TexWriterPlugin();
		try {
			twp.preAppend("", createGlobalConfig(dm.getOutputDir(), "Myname"));
			twp.appendVideoMessage(new VideoMessage(LocalDateTime.of(2023, 1, 22, 13, 52, 13), "From", videoPath, text));
			twp.postAppend();
		} catch (WriterException e) {
			fail(e);
		}
		
		// thorough testing will be done in ThumbnailCreator, we only test if one jpeg exists
		long cnt = Files.list(dm.getImageDir())
			.filter(p -> p.toString().toLowerCase().endsWith("jpg"))
			.count();
		assertTrue(cnt > 0);
	}
	
	private void checkFile(Path filePath) {
		File file = filePath.toFile();
		assertTrue(file.exists());
		assertTrue(file.length() > 0);
	}
	
	private void checkFile(Path filePath, String contentExp) throws IOException {
		assertTrue(filePath.toFile().exists());
		String contentActual = Files.readString(filePath);
		assertEquals(contentExp, contentActual.trim());
	}

	private Global createGlobalConfig(Path outputDir, String nameSuggestion) {
		Global global = new Global(outputDir, null);
		global.setNameSuggestion(nameSuggestion);
		
		return global;
	}
}
