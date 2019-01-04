package creator.plugins.fop;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "FOPToken")
public class FOPToken implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement(name = "emoji")
	private String emoji;
	
	@XmlElement(name = "normal")
	private String normal;

	public FOPToken() {
	}

	public static FOPToken ofEmoji(String str, boolean isEmoji) {
		FOPToken token = new FOPToken();
		if (isEmoji) {
			token.emoji = str;
			token.normal = null;
		} else {
			token.emoji = null;
			token.normal = str;
		}

		return token;
	}
}
