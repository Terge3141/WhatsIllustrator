package helper;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtils {
	public static String GetTextNode(NodeList nodeList, String name) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if(node.getNodeName().equals(name)){
					return node.getTextContent();
				}
			}
		}
		
		return null;
	}
}
