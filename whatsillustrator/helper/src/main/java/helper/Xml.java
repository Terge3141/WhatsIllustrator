package helper;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Xml {
	public static Node selectNode(Node node, String xPathExpression) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		return (Node)xpath.compile(xPathExpression).evaluate(node, XPathConstants.NODE);
	}
	
	public static NodeList selectNodes(Node node, String xPathExpression) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		return (NodeList)xpath.compile(xPathExpression).evaluate(node, XPathConstants.NODESET);
	}
	
	public static String getTextFromNode(Node parent, String xPathExpression) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		Node node = (Node)xpath.compile(xPathExpression).evaluate(parent, XPathConstants.NODE);
		
		if(node==null) {
			return null;
		}
		
		return node.getTextContent();
	}
	
	public static Path getPathFromNode(Node parent, String xPathExpression) throws XPathExpressionException {
		String str = getTextFromNode(parent, xPathExpression);
		
		if(str==null) {
			return null;
		}
		
		return Paths.get(str);
	}
	
	public static boolean getBooleanFromNode(Node parent, String xPathExpression) throws XPathExpressionException {
		String val = getTextFromNode(parent, xPathExpression);
		if(val==null) {
			return false;
		}
		
		return Boolean.parseBoolean(val);
	}
	
	public static LocalDateTime getLocalDateTimeFromNode(Node parent, String xPathExpression) throws XPathExpressionException {
		String val = getTextFromNode(parent, xPathExpression);
		if(val==null) {
			return null;
		}
		
		return LocalDateTime.parse(val);
	}
	
	public static void addTextElement(Element el, String name, String value) {
		Document doc = el.getOwnerDocument();
		Element te = doc.createElement(name);
		te.setTextContent(value);
		el.appendChild(te);
	}
	
	public static Document documentFromString(String xml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(xml)));
	}
}
