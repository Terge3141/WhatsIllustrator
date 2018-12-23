package helper.test;

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
		LocalDateTime date1 = getDate("2015-10-21").atTime(0, 0);
		LocalDateTime date2 = getDate("2016-10-21").atTime(0, 0);
		assertTrue(DateUtils.dateDiffer(date1, date2));
	}

	@Test
	public void testDateDifferMonthDiffer() throws ParseException {
		LocalDateTime date1 = getDate("2015-10-21").atTime(0, 0);
		LocalDateTime date2 = getDate("2015-09-21").atTime(0, 0);
		assertTrue(DateUtils.dateDiffer(date1, date2));
	}

	@Test
	public void testDateDifferDayDiffer() throws ParseException {
		LocalDateTime date1 = getDate("2015-10-21").atTime(0, 0);
		LocalDateTime date2 = getDate("2015-10-22").atTime(0, 0);
		assertTrue(DateUtils.dateDiffer(date1, date2));
	}

	@Test
	public void testFormatDateString_DE() throws ParseException {
		DateUtils dateUtils = new DateUtils("de");
		assertEquals("Sonntag, der 18. Oktober 2015", dateUtils.formatDateString(getDate("2015-10-18")));
		assertEquals("Montag, der 19. Oktober 2015", dateUtils.formatDateString(getDate("2015-10-19")));
		assertEquals("Dienstag, der 20. Oktober 2015", dateUtils.formatDateString(getDate("2015-10-20")));
		assertEquals("Mittwoch, der 21. Oktober 2015", dateUtils.formatDateString(getDate("2015-10-21")));
		assertEquals("Donnerstag, der 22. Oktober 2015", dateUtils.formatDateString(getDate("2015-10-22")));
		assertEquals("Freitag, der 23. Oktober 2015", dateUtils.formatDateString(getDate("2015-10-23")));
		assertEquals("Samstag, der 24. Oktober 2015", dateUtils.formatDateString(getDate("2015-10-24")));
	}

	@Test
	public void testFormatDateString_EN() throws ParseException {
		DateUtils dateUtils = new DateUtils("en");
		assertEquals("18 October 2015", dateUtils.formatDateString(getDate("2015-10-18")));
		assertEquals("19 October 2015", dateUtils.formatDateString(getDate("2015-10-19")));
		assertEquals("20 October 2015", dateUtils.formatDateString(getDate("2015-10-20")));
		assertEquals("21 October 2015", dateUtils.formatDateString(getDate("2015-10-21")));
		assertEquals("22 October 2015", dateUtils.formatDateString(getDate("2015-10-22")));
		assertEquals("23 October 2015", dateUtils.formatDateString(getDate("2015-10-23")));
		assertEquals("24 October 2015", dateUtils.formatDateString(getDate("2015-10-24")));
	}

	@Test
	public void testFormatDateString_FR() throws ParseException {
		DateUtils dateUtils = new DateUtils("fr");
		assertEquals("dimanche 18 octobre 2015", dateUtils.formatDateString(getDate("2015-10-18")));
		assertEquals("lundi 19 octobre 2015", dateUtils.formatDateString(getDate("2015-10-19")));
		assertEquals("mardi 20 octobre 2015", dateUtils.formatDateString(getDate("2015-10-20")));
		assertEquals("mercredi 21 octobre 2015", dateUtils.formatDateString(getDate("2015-10-21")));
		assertEquals("jeudi 22 octobre 2015", dateUtils.formatDateString(getDate("2015-10-22")));
		assertEquals("vendredi 23 octobre 2015", dateUtils.formatDateString(getDate("2015-10-23")));
		assertEquals("samedi 24 octobre 2015", dateUtils.formatDateString(getDate("2015-10-24")));
	}

	@Test
	public void testFormatTimeString_Default() throws ParseException {
		DateUtils dateUtils = new DateUtils("de");
		assertEquals("09:01", dateUtils.formatTimeString(getTime("09:01")));
		assertEquals("15:26", dateUtils.formatTimeString(getTime("15:26")));
	}
	
	@Test
	public void testFormatTimeString_EN() throws ParseException {
		DateUtils dateUtils = new DateUtils("en");
		assertEquals("09:01 AM", dateUtils.formatTimeString(getTime("09:01")));
		assertEquals("03:26 PM", dateUtils.formatTimeString(getTime("15:26")));
	}

	private LocalDate getDate(String str) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return LocalDate.parse(str, formatter);
	}

	private LocalTime getTime(String str) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		return LocalTime.parse(str, formatter);
	}
}
