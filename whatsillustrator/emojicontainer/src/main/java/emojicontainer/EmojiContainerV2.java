package emojicontainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmojiContainerV2 {
	
	private final String EMOJIPREFIX = "emoji_u";
	private static final String SEPERATOR = "_";
	
	private static Logger logger = LogManager.getLogger(EmojiContainerV2.class);
	
	public EmojiContainerV2() throws IOException {
		List<String> list = readEmojiList();
		/*for(String str : list) {
			System.out.println(str);
		}*/
		
		Branch main = new Branch(-1);
		for(String str : list) {
			StringTokenizer st = new StringTokenizer(str, SEPERATOR);
			List<Integer> tokens = new ArrayList<Integer>();
			while(st.hasMoreTokens()) {
				int i = Integer.parseInt(st.nextToken(), 16);
				tokens.add(i);
			}
			
			main.add(tokens);
		}
		
		main.print();
	}
	
	// TODO refactor
	private List<String> readEmojiList() throws IOException {
		List<String> list = new ArrayList<>();
		
		InputStream in = this.getClass().getResourceAsStream("/files.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		String fileName;
		while((fileName=br.readLine())!=null) {
			String nr = fileName.replace(EMOJIPREFIX, "").replace(".png", "");
			list.add(nr);

			String[] excludes = { "0023", "002a", "0030", "0031", "0032", "0033", "0034", "0035", "0036", "0037",
					"0038", "0039" };

			for (String str : excludes) {
				list.remove(str);
			}
		}

		logger.info("Loaded {} entries", list.size());

		return list;
	}
}
