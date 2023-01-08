package helper;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Xml {
	public static Node selectNode(Node node, String xPathExpression) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		return (Node)xpath.compile(xPathExpression).evaluate(node, XPathConstants.NODE);
	}
	
	public static String getTextFromNode(Node parent, String xPathExpression) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		Node node = (Node)xpath.compile(xPathExpression).evaluate(parent, XPathConstants.NODE);
		return node.getTextContent();
	}
	
	public static void addTextElement(Element el, String name, String value) {
		Document doc = el.getOwnerDocument();
		Element te = doc.createElement(name);
		te.setTextContent(value);
		el.appendChild(te);
	}
}
