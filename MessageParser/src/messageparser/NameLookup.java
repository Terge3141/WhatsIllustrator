package messageparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import helper.Misc;

public class NameLookup {

	private Hashtable<String, String> hashtable;

	public NameLookup() {
		this.hashtable = new Hashtable<>();
	}

	public static NameLookup fromXmlString(String xml) {
		try {
			NameLookup nl = new NameLookup();

			SAXReader reader = new SAXReader();
			InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));
			Document document = reader.read(stream);
			List<Node> elements = document.selectNodes("//NameReplacer/ReplaceItem");
			for (Node element : elements) {
				String oldName = element.selectSingleNode("oldName").getText();
				String newName = element.selectSingleNode("newName").getText();
				nl.add(oldName, newName);
			}
			
			return nl;
		} catch (DocumentException de) {
			throw new IllegalArgumentException("Cannot parse string",de);
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
