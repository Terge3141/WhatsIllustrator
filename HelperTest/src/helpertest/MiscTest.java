package helpertest;

import static org.junit.Assert.*;
import helper.Misc;

import org.junit.Test;

public class MiscTest {

	@Test
	public void testIsNullOrEmpty() {
		assertTrue(Misc.isNullOrEmpty(null));
		assertTrue(Misc.isNullOrEmpty(""));
		assertFalse(Misc.isNullOrEmpty(" "));
		assertFalse(Misc.isNullOrEmpty("asd dfgd"));
	}

	@Test
	public void testIsNullOrWhiteSpace() {
		assertTrue(Misc.isNullOrWhiteSpace(null));
		assertTrue(Misc.isNullOrWhiteSpace(""));
		assertTrue(Misc.isNullOrWhiteSpace(" "));
		assertTrue(Misc.isNullOrWhiteSpace("   "));
		assertFalse(Misc.isNullOrWhiteSpace("asd dfgd"));
	}

	@Test
	public void testArrayContains() {
		String[] arr = {"asd","b","c"};
		assertTrue(Misc.arrayContains(arr, "asd"));
		assertTrue(Misc.arrayContains(arr, "b"));
		assertTrue(Misc.arrayContains(arr, "c"));
		assertFalse(Misc.arrayContains(arr, "d"));
		assertFalse(Misc.arrayContains(arr, "ase"));
	}
}
