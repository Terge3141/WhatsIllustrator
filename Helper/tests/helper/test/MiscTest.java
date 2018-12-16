package helper.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

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
		String[] arr = { "asd", "b", "c" };
		assertTrue(Misc.arrayContains(arr, "asd"));
		assertTrue(Misc.arrayContains(arr, "b"));
		assertTrue(Misc.arrayContains(arr, "c"));
		assertFalse(Misc.arrayContains(arr, "d"));
		assertFalse(Misc.arrayContains(arr, "ase"));
	}

	@Test
	public void testListContains() {
		List<String> list = Arrays.asList("asd", "b", "c");
		assertTrue(Misc.listContains(list, "asd"));
		assertTrue(Misc.listContains(list, "b"));
		assertTrue(Misc.listContains(list, "c"));
		assertFalse(Misc.listContains(list, "d"));
		assertFalse(Misc.listContains(list, "ase"));
	}
}
