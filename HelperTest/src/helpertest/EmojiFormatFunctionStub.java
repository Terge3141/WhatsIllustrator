package helpertest;

import helper.IEmojiFormatFunction;

public class EmojiFormatFunctionStub implements IEmojiFormatFunction {

	public String format(String str) {
		return String.format("ICON{s}", str);
	}

}
