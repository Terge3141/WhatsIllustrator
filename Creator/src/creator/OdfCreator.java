package creator;

import java.io.IOException;
import java.net.URI;
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
import org.odftoolkit.simple.draw.FrameRectangle;
import org.odftoolkit.simple.draw.FrameStyleHandler;
import org.odftoolkit.simple.draw.Image;
import org.odftoolkit.simple.style.DefaultStyleHandler;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.GraphicProperties;
import org.odftoolkit.simple.style.ParagraphProperties;
import org.odftoolkit.simple.style.StyleTypeDefinitions.AnchorType;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FontStyle;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FrameHorizontalPosition;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FrameVerticalPosition;
import org.odftoolkit.simple.style.StyleTypeDefinitions.HorizontalAlignmentType;
import org.odftoolkit.simple.style.StyleTypeDefinitions.TextLinePosition;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.Span;

import helper.DateUtils;
import helper.FileHandler;
import imagematcher.ImageMatcher;
import messageparser.IMessage;
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.NameLookup;
import messageparser.TextMessage;
import messageparser.WhatsappParser;

public class OdfCreator {

	// TODO remove static
	private static final double IMAGE_HEIGHT_CM = 2.0;

	private static Logger logger = LogManager.getLogger(OdfCreator.class);

	private Path inputDir;
	private Path outputDir;
	private Path chatDir;

	private Path imageDir;
	/*
	 * private Path configDir; private Object imagePoolDir;
	 */

	private Path odfOutputPath;

	private DateUtils dateUtils;

	public OdfCreator(Path inputDir, Path outputDir, Path emojiInputDir) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.chatDir = this.inputDir.resolve("chat");

		this.imageDir = this.chatDir;
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
				appendTextMessage(doc, (TextMessage) msg);
			} else if (msg instanceof ImageMessage) {
				appendImageMessage(doc, (ImageMessage) msg);
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

	private void appendImageMessage(TextDocument doc, ImageMessage msg) {
		String sender = msg.getSender();
		String time = this.dateUtils.formatTimeString(msg.timepoint);

		Path absoluteImgPath = this.imageDir.resolve(msg.getFilename());

		String uuid = UUID.randomUUID().toString();

		String senderDummy = String.format("@@@@%s@@@@", uuid);

		String str = String.format("%s (%s):\n", senderDummy, time);

		doc.addParagraph(str);

		Paragraph imageParagraph = doc.addParagraph("");
		imageParagraph.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
		Image image = Image.newImage(imageParagraph, absoluteImgPath.toUri());
		// image.setHorizontalPosition(FrameHorizontalPosition.CENTER);
		FrameRectangle rectangle = image.getRectangle();
		double scaleFactor = IMAGE_HEIGHT_CM / rectangle.getHeight();
		rectangle.setWidth(rectangle.getWidth() * scaleFactor);
		rectangle.setHeight(IMAGE_HEIGHT_CM);
		image.setRectangle(rectangle);

		FrameStyleHandler imageStyleHandler = image.getStyleHandler();
		imageStyleHandler.setAchorType(AnchorType.AS_CHARACTER);

		if (msg.getSubscription() != null) {
			Paragraph subscriptionParagraph = doc.addParagraph(msg.getSubscription());
			subscriptionParagraph.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
		}

		TextNavigation textNavigation = new TextNavigation(senderDummy, doc);
		TextSelection textSelection = (TextSelection) textNavigation.nextSelection();

		Font font = imageParagraph.getFont();
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

	public static void main(String args[]) throws Exception {
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

		Paragraph paragraph4 = textDoc.addParagraph("");
		paragraph4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
		Image image = Image.newImage(paragraph4, new URI("/tmp/imagepool/IMG-20181024-WA0008.jpg"));
		// image.setHorizontalPosition(FrameHorizontalPosition.CENTER);
		FrameRectangle rectangle = image.getRectangle();
		double scaleFactor = IMAGE_HEIGHT_CM / rectangle.getHeight();
		rectangle.setWidth(rectangle.getWidth() * scaleFactor);
		rectangle.setHeight(IMAGE_HEIGHT_CM);
		image.setRectangle(rectangle);

		FrameStyleHandler styleHandler2 = image.getStyleHandler();
		styleHandler2.setAchorType(AnchorType.AS_CHARACTER);
		// styleHandler2.setHorizontalPosition(FrameHorizontalPosition.CENTER);
		// styleHandler2.setBackgroundFrame(false);

		/*
		 * GraphicProperties graphicProperties =
		 * styleHandler2.getGraphicPropertiesForWrite();
		 * graphicProperties.setStyleRunThrough(false);
		 */

		System.out.println(rectangle.getHeight() + " " + rectangle.getWidth());

		Paragraph paragraph5 = textDoc.addParagraph("And it goes weitre");

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
