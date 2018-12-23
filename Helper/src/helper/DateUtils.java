package helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateUtils {

	// TODO provide list externally
	public static final String[] dayNames = { "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag",
			"Sonntag" };
	// TODO provide list externally
	public static final String[] months = { "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August",
			"September", "Oktober", "November", "Dezember" };

	public static boolean dateDiffer(LocalDateTime date1, LocalDateTime date2) {
		return date1.getYear() != date2.getYear() || date1.getMonthValue() != date2.getMonthValue()
				|| date1.getDayOfMonth() != date2.getDayOfMonth();
	}

	public static String formatDateString(LocalDate date) {
		String dayName = dayNames[date.getDayOfWeek().getValue() - 1];
		String monthName = months[date.getMonthValue() - 1];
		return String.format("%s, der %s. %s %s", dayName, date.getDayOfMonth(), monthName, date.getYear());
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
