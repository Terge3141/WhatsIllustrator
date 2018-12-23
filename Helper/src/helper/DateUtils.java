package helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

	public static boolean dateDiffer(LocalDateTime date1, LocalDateTime date2) {
		return date1.getYear() != date2.getYear() || date1.getMonthValue() != date2.getMonthValue()
				|| date1.getDayOfMonth() != date2.getDayOfMonth();
	}

	public static String formatDateString(LocalDate date) {
		return date.format(DateTimeFormatter.ofPattern("EEEE, 'der' d. MMMM yyyy", Locale.GERMANY));
	}

	public static String formatDateString(LocalDateTime date) {
		return formatDateString(date.toLocalDate());
	}

	public static String formatTimeString(LocalTime date) {
		return String.format("%02d:%02d", date.getHour(), date.getMinute());
	}

	public static String formatTimeString(LocalDateTime date) {
		return formatTimeString(date.toLocalTime());
	}
}
