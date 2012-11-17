package org.whispercomm.shout.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.test.ShoutTestRunner;

@RunWith(ShoutTestRunner.class)
public class ValidatorsTest {

	private static final String STRING_WITH_NEWLINE = "dadri\nan";
	private static final String STRING_WITHOUT_NEWLINE = "dadrian";
	
	private static final String STRING_WITH_TRAILING_WHITESPACE = "Hope for the best!  \t\t\n\n    \t\n";
	private static final String STRING_WITHOUT_TRAILING_WHITESPACE = "Hope for the best!";
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void takeDown() {
		
	}
	
	@Test
	public void testRemoveTrailingSpaces() {
		String output = Validators.removeTrailingSpaces(STRING_WITH_TRAILING_WHITESPACE);
		assertEquals(STRING_WITHOUT_TRAILING_WHITESPACE, output);
	}
	
	@Test
	public void testNewlineInvalidatesUsername() {
		boolean isValid = Validators.validateUsername(STRING_WITH_NEWLINE);
		assertFalse(isValid);
		isValid = Validators.validateUsername(STRING_WITHOUT_NEWLINE);
		assertTrue(isValid);
	}
	
}
