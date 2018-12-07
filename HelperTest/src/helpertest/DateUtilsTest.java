package helpertest;

import static org.junit.Assert.*;

import helper.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.junit.Test;

public class DateUtilsTest {

	@Test
	public void testDateDifferEqual() throws ParseException {
		Calendar cal1 = getDate("2015-10-21");
		cal1.set(Calendar.HOUR, 10);
		Calendar cal2 = getDate("2015-10-21");
		assertFalse(DateUtils.dateDiffer(cal1, cal2));
	}
	
	@Test
	public void testDateDifferYearDiffer() throws ParseException{
		Calendar cal1 = getDate("2015-10-21");
		Calendar cal2 = getDate("2016-10-21");
		assertTrue(DateUtils.dateDiffer(cal1, cal2));
	}
	
	@Test
	public void testDateDifferMonthDiffer()throws ParseException {
		Calendar cal1 = getDate("2015-10-21");
		Calendar cal2 = getDate("2015-09-21");
		assertTrue(DateUtils.dateDiffer(cal1, cal2));
	}
	
	@Test
	public void testDateDifferDayDiffer()throws ParseException {
		Calendar cal1 = getDate("2015-10-21");
		Calendar cal2 = getDate("2015-10-22");
		assertTrue(DateUtils.dateDiffer(cal1, cal2));
	}
	
	@Test
	public void testFormatDateString() throws ParseException{
		assertEquals("Sonntag, den 18. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-18")));
		assertEquals("Montag, den 19. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-19")));
		assertEquals("Dienstag, den 20. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-20")));
		assertEquals("Mittwoch, den 21. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-21")));
		assertEquals("Donnerstag, den 22. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-22")));
		assertEquals("Freitag, den 23. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-23")));
		assertEquals("Samstag, den 24. Oktober 2015", DateUtils.formatDateString(getDate("2015-10-24")));
	}
	
	@Test
	public void testFormatTimeString() throws ParseException{
		assertEquals("15:26", DateUtils.formatTimeString(getTime("15:26")));
		assertEquals("09:01", DateUtils.formatTimeString(getTime("09:01")));
	}
	
	private Calendar getDate(String str) throws ParseException{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		cal.setTime(sdf.parse(str));
		
		return cal;
	}
	
	private Calendar getTime(String str) throws ParseException{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
		cal.setTime(sdf.parse(str));
		
		return cal;
	}
}
