package messageparser;

import configurator.Global;

public interface IParser {
	IMessage nextMessage();
	void init(String xmlConfig, Global globalConfig) throws Exception;
	String getNameSuggestion();
}