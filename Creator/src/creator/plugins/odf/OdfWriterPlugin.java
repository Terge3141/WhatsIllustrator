package creator.plugins.odf;

import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.Span;

import creator.plugins.IWriterPlugin;
import creator.plugins.WriterConfig;
import creator.plugins.WriterException;
import helper.EmojiParser;
import helper.EmojiParser.Token;
import helper.Misc;
import messageparser.IMessage;
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;

public class OdfWriterPlugin implements IWriterPlugin {

	private final double IMAGE_HEIGHT_CM = 2.0;

	private final double EMOJI_HEIGHT_CM = 0.5;

	private static Logger logger = LogManager.getLogger(OdfWriterPlugin.class);

	private Path odfOutputPath;

	private boolean firstDateHeader = true;
	private TextDocument doc;

	private WriterConfig config;

	private EmojiParser emojis;

	@Override
	public void preAppend(WriterConfig config) throws WriterException {
		this.config = config;

		this.emojis = new EmojiParser(config.getEmojiList());

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
		appendSenderAndDate(msg, msg.getContent());
	}

	@Override
	public void appendImageMessage(ImageMessage msg) throws WriterException {
		appendSenderAndDate(msg, null);

		Path absoluteImgPath = this.config.getImageDir().resolve(msg.getFilename());

		appendImage(absoluteImgPath, msg.getSubscription());
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

			appendImage(this.config.getImagePoolDir().resolve(relPath), hint);
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
			Paragraph subscriptionParagraph = doc.addParagraph("");
			subscriptionParagraph.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
			addEmojiEncodedText(subscriptionParagraph, subscription + "\n", FontStyle.ITALIC);
		}

		return paragraph;
	}

	private Paragraph appendSenderAndDate(IMessage msg, String extraText) {
		String sender = msg.getSender();
		String time = this.config.getDateUtils().formatTimeString(msg.getTimepoint());

		Paragraph paragraph = doc.addParagraph("");
		paragraph.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
		addFontStyledText(paragraph, sender, FontStyle.BOLD);
		addFontStyledText(paragraph, String.format(" (%s):", time));
		if (extraText != null) {
			addEmojiEncodedText(paragraph, extraText);
		}

		return paragraph;
	}

	private void addEmojiEncodedText(Paragraph paragraph, String text, FontStyle fontStyle) {
		List<Token> tokens = this.emojis.getTokens(text);
		for (Token token : tokens) {
			if (token.isEmoji()) {
				URI uri = this.config.getEmojiInputDir()
						.resolve(this.emojis.getEmojiPrefix() + token.getString() + ".png").toUri();
				Image image = Image.newImage(paragraph, uri);

				FrameRectangle rectangle = image.getRectangle();
				double scaleFactor = EMOJI_HEIGHT_CM / rectangle.getHeight();
				rectangle.setWidth(rectangle.getWidth() * scaleFactor);
				rectangle.setHeight(EMOJI_HEIGHT_CM);
				image.setRectangle(rectangle);

				FrameStyleHandler imageStyleHandler = image.getStyleHandler();
				imageStyleHandler.setAchorType(AnchorType.AS_CHARACTER);
				imageStyleHandler.setVerticalPosition(FrameVerticalPosition.BELOW);
			} else {
				addFontStyledText(paragraph, token.getString(), fontStyle);
			}
		}
	}

	private void addEmojiEncodedText(Paragraph paragraph, String text) {
		addEmojiEncodedText(paragraph, text, null);
	}

	private void addFontStyledText(Paragraph paragraph, String text, FontStyle fontStyle) {
		if (fontStyle == null) {
			paragraph.appendTextContent(text);
		} else {
			// is there an easier way?
			String uuid = UUID.randomUUID().toString();
			String textDummy = String.format("@@@@%s@@@@", uuid);

			paragraph.appendTextContent(textDummy);

			TextNavigation textNavigation = new TextNavigation(textDummy, doc);
			TextSelection textSelection = (TextSelection) textNavigation.nextSelection();

			Font font = paragraph.getFont();
			font.setFontStyle(fontStyle);
			Span span = Span.newSpan(textSelection);
			span.setTextContent(text);
			DefaultStyleHandler styleHandler = span.getStyleHandler();
			styleHandler.getTextPropertiesForWrite().setFont(font);
		}
	}

	private void addFontStyledText(Paragraph paragraph, String text) {
		addFontStyledText(paragraph, text, null);
	}
}
