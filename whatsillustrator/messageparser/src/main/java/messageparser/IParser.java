package messageparser;

import configurator.Global;

public interface IParser {
	IMessage nextMessage() throws ParserException;
	void init(String xmlConfig, Global globalConfig) throws ParserException;
	String getNameSuggestion();
}