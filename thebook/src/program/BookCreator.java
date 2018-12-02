package program;

import helper.EmojiParser;
import helper.IEmojiFormatFunction;

import java.util.List;

public class BookCreator implements IEmojiFormatFunction {

	// The top level input directory. It typically contains the subdirectories
	// chat and config
	public String InputDir;

	// This is the directory where the tex file and other output files are
	// written
	public String OutputDir;

	// This is the directory where the used written emojis are written to.
	// Default is OutputDir/emojis
	public String EmojiOutputDir;

	// It is the directory where the chat txt file and the images are stored.
	// These files can be obtained by exporting the chat in the Whatsapp app
	// By default the directory is set to InputDir/Chat
	public String ChatDir;

	// In this directory all configuration files are, e.g. the
	// chatname.match.xml file
	public String ConfigDir;

	// It contains all images for the chat.
	// By default it is set to ChatDir
	public String ImageDir;

	// It should contain all whatsapp images.
	// This directory is used if there "<Media omitted>" lines in the chat file.
	// and if no chatname.match.xml file is available.
	// It is set to null by default.
	public String ImagePoolDir;
	
	private String emojiInputDir;
	
	private EmojiParser emojis;

	public BookCreator(String inputDir, String outputDir, String emojiInputDir) {
		List<String> emojiList = ReadEmojiList(emojiInputDir);
        this.emojiInputDir = emojiInputDir;

        /*this.emojis = new EmojiParser(emojiList, x => GetEmojiPath(x));

        _header = File.ReadAllText("header.tex.tmpl");
        _footer = File.ReadAllText("footer.tex.tmpl");

        InputDir = inputDir;
        OutputDir = outputDir;

        ChatDir = Path.Combine(InputDir, "chat");
        ConfigDir = Path.Combine(InputDir, "config");
        ImageDir = ChatDir;
        ImagePoolDir = null;

        EmojiOutputDir = Path.Combine(OutputDir, "emojis");
        Directory.CreateDirectory(EmojiOutputDir);*/
	}

	public void WriteTex() {
		throw new java.lang.UnsupportedOperationException();
	}
	
	public String Format(String str){
		return GetEmojiPath(str);
	}
	
	private List<String> ReadEmojiList(String dir)
    {
		throw new java.lang.UnsupportedOperationException(); 
        /*var list = new List<string>();
        foreach (var x in Directory.EnumerateFiles(dir))
        {
            var fileName = Path.GetFileName(x);

            var regex = new Regex(EMOJIPREFIX);
            var nr = regex.Replace(fileName, "");

            regex = new Regex(@"\.png");
            nr = regex.Replace(nr, "");
            list.Add(nr);
        }

        // TODO find better solution
        var excludes = new string []{ "0023", "002a", "0030", "0031", "0032", "0033", "0034", "0035", "0036", "0037", "0038", "0039" };
        foreach(var x in excludes)
        {
            list.Remove(x);
        }

        return list;*/
    }
	
	private String GetEmojiPath(String str)
    {
		throw new java.lang.UnsupportedOperationException();
        /*var src = $"{_emojiInputDir}/{EMOJIPREFIX}{str}.png";
        var dst = $"{EmojiOutputDir}/{str}.png";

        _copyList.Add(new Tuple<string, string>(src, dst));

        return $"\\includegraphics[scale=0.075]{{emojis/{str}.png}}";*/
    }
}
