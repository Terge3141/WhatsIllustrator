package helpertest;

import static org.junit.Assert.*;

import helper.DateUtils;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class DateUtilsTest {

	@Test
	public void testDateDifferEqual() throws ParseException {
		LocalDateTime date1 = getDate("2015-10-21").atTime(0, 0);
		LocalDateTime date2 = getDate("2015-10-21").atTime(10, 0);
		assertFalse(DateUtils.dateDiffer(date1, date2));
	}

	@Test
	public void testDateDifferYearDiffer() throws ParseException {
		LocalDateTime date1 = getDate("2015-10-21").atTime(0,0);
		LocalDateTime date2 = getDate("2016-10-21").atTime(0,0);
		assertTrue(DateUtils.dateDiffer(date1, date2));
	}

	@Test
	public void testDateDifferMonthDiffer() throws ParseException {
		LocalDateTime date1 = getDate("2015-10-21").atTime(0,0);
		LocalDateTime date2 = getDate("2015-09-21").atTime(0,0);
		assertTrue(DateUtils.dateDiffer(date1, date2));
	}

	@Test
	public void testDateDifferDayDiffer() throws ParseException {
		LocalDateTime date1 = getDate("2015-10-21").atTime(0,0);
		LocalDateTime date2 = getDate("2015-10-22").atTime(0,0);
		assertTrue(DateUtils.dateDiffer(date1, date2));
	}

	@Test
	public void testFormatDateString() throws ParseException {
		assertEquals("Sonntag, der 18. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-18")));
		assertEquals("Montag, der 19. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-19")));
		assertEquals("Dienstag, der 20. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-20")));
		assertEquals("Mittwoch, der 21. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-21")));
		assertEquals("Donnerstag, der 22. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-22")));
		assertEquals("Freitag, der 23. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-23")));
		assertEquals("Samstag, der 24. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-24")));
	}

	@Test
	public void testFormatTimeString() throws ParseException {
		assertEquals("15:26", DateUtils.formatTimeString(getTime("15:26")));
		assertEquals("09:01", DateUtils.formatTimeString(getTime("09:01")));
	}

	private LocalDate getDate(String str) throws ParseException {
		DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return LocalDate.parse(str, formatter);
	}

	private LocalTime getTime(String str) throws ParseException {
		DateTimeFormatter formatter=DateTimeFormatter.ofPattern("HH:mm");
		return LocalTime.parse(str, formatter);
	}
}
