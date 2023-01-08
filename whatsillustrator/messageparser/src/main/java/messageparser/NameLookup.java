package messageparser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import helper.Misc;
import helper.Xml;

public class NameLookup {

	private Hashtable<String, String> hashtable;

	public NameLookup() {
		this.hashtable = new Hashtable<>();
	}

	public static NameLookup fromXmlString(String xml) {
		try {
			NameLookup nl = new NameLookup();
			Document document = Xml.documentFromString(xml);
			
			NodeList nodes = Xml.selectNodes(document.getDocumentElement(), "/NameLookup/ReplaceItem");
			for(int i=0; i<nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String oldName = Xml.getTextFromNode(node, "oldName");
				String newName = Xml.getTextFromNode(node, "newName");
				nl.add(oldName, newName);
			}
			
			return nl;
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			throw new IllegalArgumentException("Cannot parse string", e);
		}

	}

	public static NameLookup fromXmlFile(Path path) throws IOException   {
		return fromXmlString(Misc.readAllText(path));
	}

	public void add(String oldName, String newName) {
		this.hashtable.put(oldName, newName);
	}

	public String tryLookup(String oldName) {
		if (hashtable.containsKey(oldName)) {
			return hashtable.get(oldName);
		} else {
			return oldName;
		}
	}
}
