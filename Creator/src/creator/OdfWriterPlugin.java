package creator;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
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
import org.odftoolkit.simple.style.StyleTypeDefinitions.AnchorType;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FontStyle;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FrameVerticalPosition;
import org.odftoolkit.simple.style.StyleTypeDefinitions.HorizontalAlignmentType;
import org.odftoolkit.simple.style.StyleTypeDefinitions.TextLinePosition;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.ParagraphStyleHandler;
import org.odftoolkit.simple.text.Span;

import helper.Misc;
import messageparser.IMessage;
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;

public class OdfWriterPlugin implements IWriterPlugin {

	private final double IMAGE_HEIGHT_CM = 2.0;

	private static Logger logger = LogManager.getLogger(OdfWriterPlugin.class);

	private Path odfOutputPath;

	private boolean firstDateHeader = true;
	private TextDocument doc;

	private WriterConfig config;

	@Override
	public void preAppend(WriterConfig config) throws WriterException {
		this.config = config;

		this.firstDateHeader = true;
		this.odfOutputPath = this.config.getOutputDir().resolve(this.config.getNamePrefix() + ".odt");

		try {
			this.doc = TextDocument.newTextDocument();
		} catch (Exception e) {
			throw new WriterException(e);
		}
	}

	@Override
	public void postAppend() throws WriterException {
		logger.info("Writing odf file to '{}'", this.odfOutputPath);
		try {
			doc.save(this.odfOutputPath.toFile());
		} catch (Exception e) {
			throw new WriterException(e);
		}
	}

	@Override
	public void appendDateHeader(LocalDateTime timepoint) throws WriterException {
		Paragraph paragraph;
		if (this.firstDateHeader) {
			paragraph = doc.getParagraphByIndex(0, false);
		} else {
			paragraph = doc.addParagraph("\n");
		}

		paragraph.appendTextContent(this.config.getDateUtils().formatDateString(timepoint) + "\n");
		paragraph.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
		this.firstDateHeader = false;
	}

	@Override
	public void appendTextMessage(TextMessage msg) throws WriterException {
		appendSenderAndDate(msg, msg.content);
	}

	@Override
	public void appendImageMessage(ImageMessage msg) throws WriterException {
		appendSenderAndDate(msg, null);

		Path absoluteImgPath = this.config.getImageDir().resolve(msg.getFilename());

		appendImage( absoluteImgPath, msg.getSubscription());
	}

	@Override
	public void appendMediaOmittedMessage(MediaOmittedMessage msg) throws WriterException {
		appendSenderAndDate(msg, null);
		Iterator<String> it = msg.getRelpaths().iterator();
		while (it.hasNext()) {
			String relPath = it.next();
			String hint = this.config.isWriteMediaOmittedHints()
					? String.format("%s;%s;%d", msg.getTimepoint(), relPath, msg.getCnt())
					: null;

			appendImage( this.config.getImagePoolDir().resolve(relPath), hint);
		}
	}

	@Override
	public void appendMediaMessage(MediaMessage msg) throws WriterException {
		String uuid = UUID.randomUUID().toString();
		String strDummy = String.format("@@@@%s@@@@", uuid);
		Paragraph paragraph = appendSenderAndDate(msg, strDummy);

		String str = msg.getFilename();
		if (!Misc.isNullOrWhiteSpace(msg.getSubscription())) {
			str = str + " - " + msg.getSubscription();
		}

		TextNavigation textNavigation = new TextNavigation(strDummy, doc);
		TextSelection textSelection = (TextSelection) textNavigation.nextSelection();

		Font font = paragraph.getFont();
		font.setFontStyle(FontStyle.ITALIC);
		Span span = Span.newSpan(textSelection);
		span.setTextContent(str);
		DefaultStyleHandler styleHandler = span.getStyleHandler();
		styleHandler.getTextPropertiesForWrite().setFont(font);
	}

	private Paragraph appendImage(Path path, String subscription) {
		Paragraph paragraph = this.doc.addParagraph("");
		paragraph.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
		Image image = Image.newImage(paragraph, path.toUri());
		FrameRectangle rectangle = image.getRectangle();
		double scaleFactor = IMAGE_HEIGHT_CM / rectangle.getHeight();
		rectangle.setWidth(rectangle.getWidth() * scaleFactor);
		rectangle.setHeight(IMAGE_HEIGHT_CM);
		image.setRectangle(rectangle);

		FrameStyleHandler imageStyleHandler = image.getStyleHandler();
		imageStyleHandler.setAchorType(AnchorType.AS_CHARACTER);

		if (subscription != null) {
			Paragraph subscriptionParagraph = doc.addParagraph(subscription + "\n");
			subscriptionParagraph.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
			ParagraphStyleHandler styleHandler = subscriptionParagraph.getStyleHandler();
			Font font = subscriptionParagraph.getFont();
			font.setFontStyle(FontStyle.ITALIC);
			styleHandler.getTextPropertiesForWrite().setFont(font);
		}

		return paragraph;
	}

	private Paragraph appendSenderAndDate(IMessage msg, String extraText) {
		String sender = msg.getSender();
		String time = this.config.getDateUtils().formatTimeString(msg.getTimepoint());

		String uuid = UUID.randomUUID().toString();
		String senderDummy = String.format("@@@@%s@@@@", uuid);

		String str = String.format("%s (%s):", senderDummy, time);
		if (extraText != null) {
			str = str + " " + extraText;
		}

		Paragraph paragraph = doc.addParagraph(str);
		paragraph.setHorizontalAlignment(HorizontalAlignmentType.LEFT);

		TextNavigation textNavigation = new TextNavigation(senderDummy, doc);
		TextSelection textSelection = (TextSelection) textNavigation.nextSelection();

		Font font = paragraph.getFont();
		font.setFontStyle(FontStyle.BOLD);
		Span span = Span.newSpan(textSelection);
		span.setTextContent(sender);
		DefaultStyleHandler styleHandler = span.getStyleHandler();
		styleHandler.getTextPropertiesForWrite().setFont(font);

		return paragraph;
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
		// Paragraph paragraph1 =textDoc.getParagraphByIndex(0, false);
		// paragraph1.appendTextContent("First Paragraph");
		Paragraph paragraph1 = textDoc.addParagraph("First Paragraph");
		paragraph1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
		Paragraph paragraph2 = textDoc.addParagraph("Second Paragraph\nAnd some more words");
		paragraph2.setHorizontalAlignment(HorizontalAlignmentType.LEFT);

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
		double scaleFactor = 2.0 / rectangle.getHeight();
		rectangle.setWidth(rectangle.getWidth() * scaleFactor);
		rectangle.setHeight(2.0);
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
		
		Paragraph paragraph6 = textDoc.addParagraph("And it even more weitre");
		Image image2 = Image.newImage(paragraph6, new URI("/tmp/imagepool/IMG-20181024-WA0008.jpg"));
		FrameRectangle rectangle2 = image2.getRectangle();
		rectangle2.setWidth(0.5);
		rectangle2.setHeight(0.5);
		image2.setRectangle(rectangle2);
		FrameStyleHandler styleHandler3 = image2.getStyleHandler();
		styleHandler3.setAchorType(AnchorType.AS_CHARACTER);
		styleHandler3.setVerticalPosition(FrameVerticalPosition.BELOW);
		paragraph6.appendTextContent("End something after the image");

		textDoc.save(Paths.get("/tmp/odf/bla.odt").toFile());
		System.out.println("Moin");
	}

}
