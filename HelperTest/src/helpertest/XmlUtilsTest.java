package helpertest;

import static org.junit.Assert.*;

import helper.XmlUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlUtilsTest {

	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException {
		String xml="<root><a>one</a><b><a>wrong</a></b><c>three</c></root>";
		NodeList nodeList=GetNodeList(xml);
		assertEquals("one", XmlUtils.GetTextNode(nodeList, "a"));
		assertEquals("three", XmlUtils.GetTextNode(nodeList, "c"));
		assertNull(XmlUtils.GetTextNode(nodeList, "d"));
	}

	private NodeList GetNodeList(String xml) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
		Document doc=dBuilder.parse(stream);
		Element root = doc.getDocumentElement();
		return root.getChildNodes();
	}
}
