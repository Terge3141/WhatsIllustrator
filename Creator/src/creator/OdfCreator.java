package creator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.navigation.TextNavigation;
import org.odftoolkit.simple.common.navigation.TextSelection;
import org.odftoolkit.simple.style.DefaultStyleHandler;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.ParagraphProperties;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FontStyle;
import org.odftoolkit.simple.style.StyleTypeDefinitions.HorizontalAlignmentType;
import org.odftoolkit.simple.style.StyleTypeDefinitions.TextLinePosition;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.Span;

import helper.DateUtils;
import helper.FileHandler;
import helper.Latex;
import imagematcher.ImageMatcher;
import messageparser.IMessage;
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.NameLookup;
import messageparser.TextMessage;
import messageparser.WhatsappParser;

public class OdfCreator {

	private static Logger logger = LogManager.getLogger(OdfCreator.class);

	private Path inputDir;
	private Path outputDir;
	private Path chatDir;
	/*
	 * private Path configDir; private Path imageDir; private Object imagePoolDir;
	 */

	private Path odfOutputPath;

	private DateUtils dateUtils;

	public OdfCreator(Path inputDir, Path outputDir, Path emojiInputDir) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.chatDir = this.inputDir.resolve("chat");
		/*
		 * this.configDir = this.inputDir.resolve("config"); this.imageDir =
		 * this.chatDir; this.imagePoolDir = null;
		 */

		this.dateUtils = new DateUtils("de");
	}

	public void writeOdf() throws Exception {
		List<String> txtFiles = FileHandler.listDir(chatDir, ".*.txt");
		if (txtFiles.size() != 1) {
			throw new IllegalArgumentException(
					String.format("Invalid number of .txt-files found: %d", txtFiles.size()));
		}

		Path txtInputPath = Paths.get(txtFiles.get(0));
		logger.info("Using {} as input", txtInputPath);

		String namePrefix = txtInputPath.toFile().getName();
		namePrefix = namePrefix.substring(0, namePrefix.length() - 4);
		this.odfOutputPath = this.outputDir.resolve(namePrefix + ".odt");

		// WhatsappParser parser = WhatsappParser.of(txtInputPath, im, nl);
		WhatsappParser parser = WhatsappParser.of(txtInputPath, new ImageMatcher(), new NameLookup());

		TextDocument doc = TextDocument.newTextDocument();

		IMessage msg;
		LocalDateTime last = LocalDateTime.MIN;
		while ((msg = parser.nextMessage()) != null) {

			if (DateUtils.dateDiffer(last, msg.getTimepoint())) {
				appendDateHeader(doc, this.dateUtils.formatDateString(msg.getTimepoint()));
			}

			last = msg.getTimepoint();

			if (msg instanceof TextMessage) {
				appendTextMessage(doc, (TextMessage)msg);
			} else if (msg instanceof ImageMessage) {
				logger.warn("ImageMessage not implemented");
				// appendImageMessage((ImageMessage) msg, tsb);
			} else if (msg instanceof MediaOmittedMessage) {
				logger.warn("MediaOmittedMessage not implemented");
				// appendMediaOmittedMessage((MediaOmittedMessage) msg, tsb);
			} else if (msg instanceof MediaMessage) {
				logger.warn("MediaMessage not implemented");
				// appendMediaMessage((MediaMessage) msg, tsb);
			}
		}
		
		doc.save(this.odfOutputPath.toFile());
	}

	private void appendDateHeader(TextDocument doc, String dateStr) {
		Paragraph paragraph = doc.addParagraph(dateStr);
		paragraph.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
	}

	private void appendTextMessage(TextDocument doc, TextMessage msg) {
		String sender = msg.getSender();
		String time = this.dateUtils.formatTimeString(msg.timepoint);
		String content = msg.getContent();
		
		String uuid = UUID.randomUUID().toString();

		String senderDummy = String.format("@@@@%s@@@@", uuid);

		String str = String.format("%s (%s): %s", senderDummy, time, content);

		Paragraph paragraph = doc.addParagraph(str);
		TextNavigation textNavigation = new TextNavigation(senderDummy, doc);
		TextSelection textSelection = (TextSelection) textNavigation.nextSelection();

		Font font = paragraph.getFont();
		font.setFontStyle(FontStyle.BOLD);
		Span span = Span.newSpan(textSelection);
		span.setTextContent(sender);
		DefaultStyleHandler styleHandler = span.getStyleHandler();
		styleHandler.getTextPropertiesForWrite().setFont(font);
	}

	/*
	 * public static void main3(String args[]) throws Exception { TextDocument
	 * textDoc = TextDocument.newTextDocument();
	 * 
	 * appendDateHeader(textDoc,"Freitag, der 7. April 2017");
	 * appendTextMessage(textDoc, "Firstname Lastname", "11:11", "It is Karneval");
	 * 
	 * textDoc.save(Paths.get("/tmp/odf/bla.odt").toFile());
	 * 
	 * System.out.println("Done"); }
	 */

	public static void main2(String args[]) throws Exception {
		TextDocument textDoc = TextDocument.newTextDocument();
		Paragraph paragraph1 = textDoc.addParagraph("First Paragraph");
		Paragraph paragraph2 = textDoc.addParagraph("Second Paragraph\nAnd some more words");

		paragraph1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);

		/*
		 * Font font = paragraph2.getFont(); font.setFontStyle(FontStyle.BOLD);
		 * paragraph2.setFont(font);
		 */
		paragraph2.appendTextContent("Now something bold");
		Paragraph paragraph3 = textDoc.addParagraph("Now bold?");

		TextNavigation textNavigation = new TextNavigation("something", textDoc);
		TextSelection textSelection = (TextSelection) textNavigation.nextSelection();

		Span span = Span.newSpan(textSelection);
		DefaultStyleHandler styleHandler = span.getStyleHandler();
		/*
		 * Font font =styleHandler.getTextPropertiesForWrite().getFont();
		 * font.setFontStyle(FontStyle.BOLD);
		 */
		Font font = new Font("Arial", FontStyle.ITALIC, 10, Color.BLACK, TextLinePosition.THROUGH);
		styleHandler.getTextPropertiesForWrite().setFont(font);
		/*
		 * ParagraphProperties paragraphProperties =
		 * styleHandler.getParagraphPropertiesForWrite();
		 * paragraphProperties.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
		 */

		// paragraph.appendTextContent("This is some text content");

		textDoc.save(Paths.get("/tmp/odf/bla.odt").toFile());
		System.out.println("Moin");
	}
}
