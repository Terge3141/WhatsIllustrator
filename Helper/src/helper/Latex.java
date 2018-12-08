package helper;

public class Latex {

	public static String encodeLatex(String str) {
		if (str == null)
        {
            return "";
        }

        str = str.replace("\\", "\\\\");
        str = str.replace("_", "\\_");
        str = str.replace("{", "\\{");
        str = str.replace("}", "\\}");
        str = str.replace("\"", "''");

        str = str.replace("\n", "\\\\");
        str = str.replace("&", "\\&");
        str = str.replace("#", "\\#");
        str = str.replace("%", "\\%");
        str = str.replace("$", "\\$");
        str = str.replace("€", "\\euro ");
        str = str.replace("„", "\\glqq ");
        str = str.replace("“", "\\grqq ");
        str = str.replace("[", "");
        str = str.replace("]", "");
        str = str.replace("^", "$\\hat{}$");
        str = str.replace("<", "$<$");
        str = str.replace(">", "$>$");
        str = str.replace("’", "'");
        str = str.replace("…", "...");
        str = str.replace("°", "${}^\\circ$");

        str = str.replace("ß", "\\ss{}");
        str = str.replace("Ä", "\\\"A");
        str = str.replace("Ö", "\\\"O");
        str = str.replace("Ü", "\\\"U");
        str = str.replace("ä", "\\\"a");
        str = str.replace("ö", "\\\"o");
        str = str.replace("ü", "\\\"u");

        // àáâ
        str = str.replace("à", "\\`{a}");
        str = str.replace("á", "\\'{a}");
        str = str.replace("â", "\\^{a}");
        str = str.replace("À", "\\`{A}");
        str = str.replace("Á", "\\'{A}");
        str = str.replace("Â", "\\^{A}");

        // èéê
        str = str.replace("è", "\\`{e}");
        str = str.replace("é", "\\'{e}");
        str = str.replace("ê", "\\^{e}");
        str = str.replace("È", "\\`{E}");
        str = str.replace("É", "\\'{E}");
        str = str.replace("Ê", "\\^{E}");

        // ìíî
        str = str.replace("ì", "\\`{i}");
        str = str.replace("í", "\\'{i}");
        str = str.replace("î", "\\^{i}");
        str = str.replace("Ì", "\\`{I}");
        str = str.replace("Í", "\\'{I}");
        str = str.replace("Î", "\\^{I}");

        // òóô
        str = str.replace("ò", "\\`{o}");
        str = str.replace("ó", "\\'{o}");
        str = str.replace("ô", "\\^{o}");
        str = str.replace("Ò", "\\`{O}");
        str = str.replace("Ó", "\\'{O}");
        str = str.replace("Ô", "\\^{O}");

        // ùúû
        str = str.replace("ù", "\\`{u}");
        str = str.replace("ú", "\\'{u}");
        str = str.replace("û", "\\^{u}");
        str = str.replace("Ù", "\\`{U}");
        str = str.replace("Ú", "\\'{U}");
        str = str.replace("Û", "\\^{U}");



        /*// causes strange character 0xC2 0xA0 --> " "
		str = replaceHelper.replaceString (str, new byte[]{ 0xC2, 0xA0 }, " "); 

		// 0xC2 0x84 --> ,,
		str = replaceHelper.replaceString (str, new byte[]{ 0xC2, 0x84 }, "\\glqq ");

		// 0xC2 0x93 --> ''
		str = replaceHelper.replaceString (str, new byte[]{ 0xC2, 0x93 }, "\\grqq ");

		// 0xC2 0x92 --> '
		str = replaceHelper.replaceString (str, new byte[]{ 0xC2, 0x92 }, "'");

		// 0xC2 0x85 --> nothing
		str = replaceHelper.replaceString (str, new byte[]{ 0xC2, 0x85 }, "");*/

        return str;
	}

	public static String replaceURL(String str) {
		boolean found = true;
        int startIndex = 0;
        while (found)
        {
        	SearchResult result = FirstString(str, new String[] { "http://", "https://" }, startIndex);

            if (result.index != -1)
            {
                int whiteIndex = FirstString(str, new String[]{ " ", "\n", "\\\\" }, result.index).index;

                String left = str.substring(0, result.index);
                String httpString;
                String right;
                if (whiteIndex == -1)
                {
                    httpString = str.substring(result.index);
                    right = "";
                }
                else
                {
                    httpString = str.substring(result.index, whiteIndex);
                    right = str.substring(whiteIndex);
                }

                String replaceStr=String.format("\\url{%s}", httpString);
                str = left + replaceStr + right;
                startIndex = result.index + replaceStr.length();
            }
            else
            {
                found = false;
            }
        }

        return str;
	}
	
	private static SearchResult FirstString(String str, String[] needles, int startIndex)
    {
        String needle = null;
        int index = -1;
        for (String x : needles)
        {
            int i = str.indexOf(x, startIndex);
            if (i != -1 && (i < index || needle == null))
            {
                index = i;
                needle = x;
            }
        }

        return new SearchResult(index, needle);
    }
	
	public static class SearchResult{
		
		private int index;
		private String needle;
		
		public SearchResult(int index,String needle){
			this.index=index;
			this.needle=needle;
		}
	}

}
