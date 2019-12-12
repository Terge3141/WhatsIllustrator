package helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility class to compare and format java.time.* date/time objects.
 * @author Michael Elvers
 *
 */
public class DateUtils {

	private Locale locale;

	/**
	 * Constructor
	 * 
	 * @param locale Locale for date/time conversion
	 */
	public DateUtils(Locale locale) {
		this.locale = locale;
	}

	/**
	 * Constructor
	 * 
	 * @param locale language code used for date/time conversion
	 */
	public DateUtils(String locale) {
		this(new Locale(locale));
	}

	/**
	 * Compares the dates (year, month, day) of two LocalDateTime object
	 * 
	 * @param date1 first date
	 * @param date2 second date
	 * @return true if they differ, false otherwise
	 */
	public static boolean dateDiffer(LocalDateTime date1, LocalDateTime date2) {
		return date1.getYear() != date2.getYear() || date1.getMonthValue() != date2.getMonthValue()
				|| date1.getDayOfMonth() != date2.getDayOfMonth();
	}

	/**
	 * Formats a date string based according to the locale setting.
	 * Fallback is English format with locale names
	 * 
	 * @param date the date to formatted
	 * @return the formatted date string
	 */
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

	/**
	 * Formats a date string based according to the locale setting.
	 * Fallback is English format with locale names
	 * 
	 * @param date the date to formatted
	 * @return the formatted date string
	 */
	public String formatDateString(LocalDateTime date) {
		return formatDateString(date.toLocalDate());
	}

	/**
	 * Formats a time string based according to the locale setting.
	 * Default is 24h format
	 * 
	 * @param time the time to formatted
	 * @return the formatted time string
	 */
	public String formatTimeString(LocalTime date) {
		if (this.locale.equals(Locale.ENGLISH)) {
			return date.format(DateTimeFormatter.ofPattern("hh:mm a"));
		} else {
			return date.format(DateTimeFormatter.ofPattern("HH:mm"));
		}
	}

	/**
	 * Formats a time string based according to the locale setting.
	 * Default is 24h format
	 * 
	 * @param time the time to formatted
	 * @return the formatted time string
	 */
	public String formatTimeString(LocalDateTime date) {
		return formatTimeString(date.toLocalTime());
	}
}
