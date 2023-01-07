package imagematcher;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

// currently only xml specific code is tested
class ImageMatcherTest {

	@Test
	void testFromXmlString() {
		String 	xml = ""
				+ TestHelper.createMatchEntryXmlString(2023, 1, 7, 16, 18)
				+ TestHelper.createMatchEntryXmlString(2023, 1, 7, 16, 19)
				+ TestHelper.createMatchEntryXmlString(2023, 1, 7, 16, 20);
		
		xml = TestHelper.xmlWrap("ArrayOfMatchEntry", xml);
		
		ImageMatcher imageMatcher = ImageMatcher.fromXmlString(xml);
		List<MatchEntry> mes = imageMatcher.getMatchList();
		assertEquals(3, mes.size());
		
		LocalDate date = LocalDate.of(2023, 1, 7);
		assertEquals(LocalDateTime.of(date, LocalTime.of(16, 18)), mes.get(0).getTimePoint());
		assertEquals(LocalDateTime.of(date, LocalTime.of(16, 19)), mes.get(1).getTimePoint());
		assertEquals(LocalDateTime.of(date, LocalTime.of(16, 20)), mes.get(2).getTimePoint());
	}

}
