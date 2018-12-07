package program;

import helper.EmojiParser;
import helper.IEmojiFormatFunction;
import helper.ImageMatcher;
import helper.Misc;

import messageparser.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

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

	private String header;
	private String footer;

	private final String EMOJIPREFIX = "emoji_u";

	private List<CopyItem> copyList;

	public BookCreator(String inputDir, String outputDir, String emojiInputDir)
			throws IOException {
		List<String> emojiList = ReadEmojiList(emojiInputDir);
		this.emojiInputDir = emojiInputDir;

		this.emojis = new EmojiParser(emojiList, this);

		header = Misc.ReadAllText("header.tex.tmpl");
		footer = Misc.ReadAllText("footer.tex.tmpl");

		InputDir = inputDir;
		OutputDir = outputDir;
		ChatDir = Paths.get(InputDir, "chat").toString();
		ConfigDir = Paths.get(InputDir, "config").toString();
		ImageDir = Paths.get(ChatDir).toString();
		ImagePoolDir = null;
		EmojiOutputDir = Paths.get(OutputDir, "emojis").toString();

		File dir = new File(EmojiOutputDir);
		dir.mkdir();
	}

	public void WriteTex() throws IOException {
		this.copyList = new ArrayList<BookCreator.CopyItem>();
		
		File dir = new File(ChatDir);
		List<String> txtFiles=Misc.ListDir(ChatDir, ".*.txt");
		if(txtFiles.size()!=1){
			throw new IllegalArgumentException(String.format("Invalid number of .txt-files found: %d", txtFiles.size()));
		}
		
		String txtInputPath=txtFiles.get(0);
		System.out.format("Using %s as input", txtInputPath);
		
		String namePrefix = Misc.getFileName(txtInputPath);
		namePrefix=namePrefix.substring(0, namePrefix.length()-4);
		String texOutputPath=Paths.get(OutputDir,namePrefix+".tex").toString();
		
		String matchInputPath=Paths.get(ConfigDir,namePrefix+".match.xml").toString();
		String matchOutputPath=Paths.get(OutputDir,namePrefix+".match.xml").toString();
		ImageMatcher im = new ImageMatcher();		
		if(Misc.fileExists(matchInputPath)){
			System.out.format("Loading matches '%s'\n", matchInputPath);
			im.LoadMatches(matchInputPath);
			im.SearchMode=false;
		}
		else		{
			if(ImagePoolDir==null){
				im.SearchMode=false;
			}
			else{
				System.out.format("Loading pool images from '%s'\n", ImagePoolDir);
				im.LoadFiles(ImagePoolDir);
                im.SearchMode = true;
			}
		}
		

		WhatsappParser parser=new WhatsappParser(txtInputPath,im);
		
		StringBuilder sb = new StringBuilder();
		sb.append(header+ "\n");
		
		// https://stackoverflow.com/questions/999172/how-to-parse-a-date
		// String input = "Thu Jun 18 20:56:02 EDT 2009";
        // SimpleDateFormat parser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
        // Date date = parser.parse(input);
		
		IMessage msg;
		Date last = new Date(0);
		while ((msg = parser.NextMessage()) != null){
			if(TimeDiffer(last, msg.getTimepoint())){	
				// TODO via sb.format??
				sb.append("\\begin{center}" + GetDateString(msg.getTimepoint()) + "\\end{center}\n");
			}
			
			last = msg.getTimepoint();
			
			if (msg instanceof TextMessage)
            {
                AppendTextMessage((TextMessage)msg , sb);
            }
            else if (msg instanceof ImageMessage)
            {
                AppendImageMessage((ImageMessage)msg , sb);
            }
            else if (msg instanceof MediaOmittedMessage)
            {
                AppendMediaOmittedMessage((MediaOmittedMessage)msg , sb);
            }
            else if (msg instanceof MediaMessage)
            {
                AppendMediaMessage((MediaMessage)msg  , sb);
            }
		}
		
		sb.append(this.footer + "\n");
		
		System.out.format("Writing tex file to '%s'\n", texOutputPath);
		Misc.WriteAllText(texOutputPath, sb.toString());
		
		System.out.format("Writing match file to '%s'\n",matchOutputPath);
        im.Save(matchOutputPath);

        System.out.format("Copy emojis to '%s'\n",EmojiOutputDir);
        CopyList();
	}

	public String Format(String str) {
		return GetEmojiPath(str);
	}

	private List<String> ReadEmojiList(String dir) {
		List<String> list = new ArrayList<>();

		File lister = new File(dir);
		for (File x : lister.listFiles()) {
			String fileName = x.getName();
			String nr = fileName.replace(EMOJIPREFIX, "").replace("\\.png", "");
			list.add(nr);

			String[] excludes = { "0023", "002a", "0030", "0031", "0032",
					"0033", "0034", "0035", "0036", "0037", "0038", "0039" };

			for (String str : excludes) {
				list.remove(str);
			}
		}

		return list;
	}
	
	private void CopyList(){
		
		throw new UnsupportedOperationException();
	}
	
	private static boolean TimeDiffer(Date date1, Date date2)
    {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		
		return cal1.get(Calendar.YEAR)!=cal2.get(Calendar.YEAR) || cal1.get(Calendar.MONTH)!=cal2.get(Calendar.MONTH)||cal1.get(Calendar.DAY_OF_MONTH)!=cal2.get(Calendar.DAY_OF_MONTH);
    }
	
	private static String GetDateString(Date date){
		throw new UnsupportedOperationException();
	}

	private String GetEmojiPath(String str) {
		String src = String.format("%s/%s%s.png", emojiInputDir, EMOJIPREFIX,
				str);
		String dst = String.format("%s/%s.png", EmojiOutputDir, str);

		copyList.add(new CopyItem(src, dst));

		return String.format("\\includegraphics[scale=0.075]{emojis/%s.png}",
				str);
	}
	
	private void AppendTextMessage(TextMessage msg, StringBuilder sb)
    {throw new UnsupportedOperationException();
        /*var senderAndTime = FormatSenderAndTime(msg);
        var content = Encode(msg.Content);
        sb.AppendLine($"{senderAndTime} {content}");
        sb.AppendLine(@"\\");*/
    }

    private void AppendImageMessage(ImageMessage msg, StringBuilder sb)
    {
    	throw new UnsupportedOperationException();
        /*sb.AppendLine(FormatSenderAndTime(msg) + @"\\");
        sb.AppendLine(@"\begin{center}");
        sb.AppendLine(@"\includegraphics[height=0.1\textheight]{" + Path.Combine(ImageDir, msg.Filename) + @"}\\");
        sb.AppendFormat(@"\small{{\textit{{{0}}}}}", Encode(msg.Subscription));
        sb.AppendLine(@"\end{center}");*/
    }

    private void AppendMediaOmittedMessage(MediaOmittedMessage msg, StringBuilder sb)
    {
    	throw new UnsupportedOperationException();
        /*sb.AppendLine(FormatSenderAndTime(msg) + @"\\");
        sb.AppendLine(@"\begin{center}");
        foreach (var x in msg.Relpaths)
        {
            sb.AppendLine(@"\includegraphics[height=0.1\textheight]{" + Path.Combine(ImagePoolDir, x) + @"}\\");
            sb.AppendFormat(@"\small{{\textit{{{0}}}}}\\", Encode(x));
        }
        sb.AppendLine(@"\end{center}");*/
    }

    private void AppendMediaMessage(MediaMessage msg, StringBuilder sb)
    {
    	throw new UnsupportedOperationException();
        /*var str = string.Format(@"{0} \textit{{{1}}}", FormatSenderAndTime(msg), Latex.EncodeLatex(msg.Filename));
        if (!string.IsNullOrWhiteSpace(msg.Subscription))
        {
            str = str + " - " + Encode(msg.Subscription);
        }

        sb.AppendLine(str);
        sb.AppendLine(@"\\");*/
    }

	private class CopyItem {

		private String src;
		private String dst;

		public CopyItem(String src, String dst) {
			this.src = src;
			this.dst = dst;
		}

		public String getSrc() {
			return this.src;
		}

		public String getDst() {
			return this.dst;
		}
	}
}
