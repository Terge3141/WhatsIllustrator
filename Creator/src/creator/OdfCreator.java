package creator;

import java.nio.file.Paths;
import java.util.UUID;

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

public class OdfCreator {

	public static void appendDateHeader(TextDocument doc, String dateStr) {
		Paragraph paragraph=doc.addParagraph(dateStr);
		paragraph.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
	}
	
	public static void appendTextMessage(TextDocument doc,String sender, String time, String content) {
		String uuid = UUID.randomUUID().toString();
		
		String senderDummy = String.format("@@@@%s@@@@", uuid);
		
		String str=String.format("%s (%s): %s", senderDummy,time,content);
		
		Paragraph paragraph=doc.addParagraph(str);		
		TextNavigation textNavigation = new TextNavigation(senderDummy, doc);
		TextSelection textSelection = (TextSelection) textNavigation.nextSelection();

		Font font = paragraph.getFont();
		font.setFontStyle(FontStyle.BOLD);
		Span span = Span.newSpan(textSelection);
		span.setTextContent(sender);
		DefaultStyleHandler styleHandler = span.getStyleHandler();
		styleHandler.getTextPropertiesForWrite().setFont(font);
	}
	
	public static void main(String args[]) throws Exception {
		TextDocument textDoc = TextDocument.newTextDocument();
		
		appendDateHeader(textDoc,"Freitag, der 7. April 2017");
		appendTextMessage(textDoc, "Firstname Lastname", "11:11", "It is Karneval");
		
		textDoc.save(Paths.get("/tmp/odf/bla.odt").toFile());
		
		System.out.println("Done");
	}

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
		/*ParagraphProperties paragraphProperties = styleHandler.getParagraphPropertiesForWrite();
		paragraphProperties.setHorizontalAlignment(HorizontalAlignmentType.CENTER);*/

		// paragraph.appendTextContent("This is some text content");

		textDoc.save(Paths.get("/tmp/odf/bla.odt").toFile());
		System.out.println("Moin");
	}
}
