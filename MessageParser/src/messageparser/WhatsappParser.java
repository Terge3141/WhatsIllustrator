package messageparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import imagematcher.*;

public class WhatsappParser {

	private List<String> lines;
	private ImageMatcher imageMatcher;
	private int index;
	private LastCnt lastCnt;

	// TODO extend pattern with Nickname and Message
	private static final String DATEPATTERN = "[0-3][0-9]/[0-1][0-9]/[0-9]{4},\\ [0-2][0-9]:[0-5][0-9]";
	private static final String PATTERN = DATEPATTERN + "\\ -\\ ";
	private static final String FILE_ATTACHED = "(file attached)";
	private static final String MEDIA_OMITTED = "<Media omitted>";

	public WhatsappParser(String messagePath, ImageMatcher imageMatcher) throws IOException {
		this.lines = Files.readAllLines(Paths.get(messagePath));
		index = 0;

		this.imageMatcher = imageMatcher;
		this.lastCnt = new LastCnt();
	}

	public IMessage NextMessage() {
		if (this.index == this.lines.size()) {
			return null;
		}

		String line = this.lines.get(this.index);
		if (!IsHeader(line)) {
			throw new IllegalArgumentException(String.format("Invalid header line: '%s'", line));
		}

		this.index++;

		// TODO check for size
		Pattern p = Pattern.compile("^" + DATEPATTERN);
		Matcher m = p.matcher(line);
		if (!m.find()) {
			throw new IllegalArgumentException(String.format("Invalid date format in line: '%s'", line));
		}
		String dateStr = line.substring(m.start(), m.end());

		/*
		 * Calendar date = Calendar.getInstance(); SimpleDateFormat sdf = new
		 * SimpleDateFormat("dd/MM/yyyy, HH:mm"); try {
		 * date.setTime(sdf.parse(dateStr)); } catch (ParseException pe) { throw new
		 * IllegalArgumentException(String.format( "Invalid date format in line: '%s'",
		 * line)); }
		 */
		LocalDateTime date = null;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
			date = LocalDateTime.parse(dateStr, formatter);
		} catch (DateTimeParseException dtpe) {
			throw new IllegalArgumentException(String.format("Invalid date format in line: '%s'", line));
		}

		// TODO use regex
		int senderEnd = line.indexOf(":", dateStr.length());

		// special message, e.g. encryption information
		if (senderEnd == -1) {
			System.out.format("No sender found, skipping line '%s'\n", line);
			return NextMessage();
		}

		String sender = line.substring(dateStr.length() + 3, senderEnd);
		String contentStr = line.substring(senderEnd + 2);

		if (contentStr.endsWith(FILE_ATTACHED)) {
			String fileName = contentStr.substring(0, contentStr.length() - FILE_ATTACHED.length() - 1);
			String extension = fileName.substring(fileName.length() - 3);

			switch (extension) {
			case "jpg":
				String subscription = ParseNextLines().trim();
				return new ImageMessage(date, sender, fileName, subscription);
			default:
				subscription = ParseNextLines().trim();
				return new MediaMessage(date, sender, fileName, subscription);
			}
		} else if (contentStr.equals(MEDIA_OMITTED)) {
			MatchEntry entry = this.imageMatcher.pick(date, GetCnt(date));
			if (entry.isImageType() && entry.getFileMatches().size() > 0) {
				List<String> relpaths = entry.getFileMatches().stream().map(x -> x.getRelPath()).distinct()
						.collect(Collectors.toList());
				return new MediaOmittedMessage(entry.getTimePoint(), sender, relpaths);
			} else {
				return NextMessage();
			}
		} else {
			contentStr = contentStr + ParseNextLines();
			contentStr = contentStr.trim();
			return new TextMessage(date, sender, contentStr);
		}
	}

	private boolean IsHeader(String str) {
		return str.matches("^" + PATTERN + ".*");
	}

	private String ParseNextLines() {
		StringBuilder sb = new StringBuilder();
		while (this.index < this.lines.size() && !IsHeader(this.lines.get(this.index))) {
			sb.append("\n");
			sb.append(this.lines.get(this.index));
			this.index++;
		}

		return sb.toString();
	}

	private int GetCnt(LocalDateTime tp) {
		if (this.lastCnt.cnt == -1) {
			this.lastCnt = new LastCnt(tp, 0);
		} else {
			if (this.lastCnt.date.equals(tp)) {
				this.lastCnt = new LastCnt(this.lastCnt.date, this.lastCnt.cnt + 1);
			} else {
				this.lastCnt = new LastCnt(tp, 0);
			}
		}

		return this.lastCnt.cnt;
	}

	public class LastCnt {
		private LocalDateTime date;
		private int cnt;

		public LastCnt() {
			this.date = LocalDateTime.MIN;
			this.cnt = 0;
		}

		public LastCnt(LocalDateTime date, int cnt) {
			this.date = date;
			this.cnt = cnt;
		}
	}
}
