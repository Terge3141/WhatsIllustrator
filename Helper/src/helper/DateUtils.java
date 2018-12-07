package helper;

import java.util.Calendar;

public class DateUtils {

	// TODO provide list externally
	public static final String[] dayNames = { "Sonntag", "Montag", "Dienstag",
			"Mittwoch", "Donnerstag", "Freitag", "Samstag" };
	// TODO provide list externally
	public static final String[] months = { "Januar", "Februar", "M\\\"arz",
			"April", "Mai", "Juni", "Juli", "August", "September", "Oktober",
			"November", "Dezember" };

	public static boolean dateDiffer(Calendar cal1, Calendar cal2) {
		return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)
				|| cal1.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)
				|| cal1.get(Calendar.DAY_OF_MONTH) != cal2
						.get(Calendar.DAY_OF_MONTH);
	}

	public static String formatDateString(Calendar cal) {
		String dayName = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1];
		String monthName = months[cal.get(Calendar.MONTH)];
		return String.format("%s, den %s. %s %s", dayName,
				cal.get(Calendar.DAY_OF_MONTH), monthName,
				cal.get(Calendar.YEAR));
	}

	public static String formatTimeString(Calendar cal) {
		return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE));
	}

}
