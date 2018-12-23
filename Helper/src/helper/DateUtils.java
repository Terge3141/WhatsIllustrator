package helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

	private Locale locale;

	public DateUtils(Locale locale) {
		this.locale = locale;
	}

	public DateUtils(String locale) {
		this(new Locale(locale));
	}

	public static boolean dateDiffer(LocalDateTime date1, LocalDateTime date2) {
		return date1.getYear() != date2.getYear() || date1.getMonthValue() != date2.getMonthValue()
				|| date1.getDayOfMonth() != date2.getDayOfMonth();
	}

	public String formatDateString(LocalDate date) {
		if (this.locale.equals(Locale.GERMAN)) {
			return date.format(DateTimeFormatter.ofPattern("EEEE, 'der' d. MMMM yyyy", this.locale));
		} else if (this.locale.equals(Locale.FRENCH)) {
			return date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", this.locale));
		}
		// Fall back to English format with locale names
		else {
			return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", this.locale));
		}
	}

	public String formatDateString(LocalDateTime date) {
		return formatDateString(date.toLocalDate());
	}

	public String formatTimeString(LocalTime date) {
		if (this.locale.equals(Locale.ENGLISH)) {
			return date.format(DateTimeFormatter.ofPattern("hh:mm a"));
		} else {
			return date.format(DateTimeFormatter.ofPattern("HH:mm"));
		}
	}

	public String formatTimeString(LocalDateTime date) {
		return formatTimeString(date.toLocalTime());
	}
}
