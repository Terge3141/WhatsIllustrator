package messageparser;

import org.dom4j.Node;

public interface IParser {
	IMessage nextMessage();
	void init(String xmlConfig) throws Exception;
}