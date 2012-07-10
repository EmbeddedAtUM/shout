
package org.whispercomm.shout.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestShout;
import org.whispercomm.shout.test.util.TestUser;
import org.whispercomm.shout.test.util.TestUtility;

import android.app.Activity;
import android.content.Context;

@RunWith(ShoutTestRunner.class)
public class ShoutSearchContractTest {

	private static final String MESSAGE = "Could you repeat the part where you said all the stuff about the things";
	private static final String WORD = "repeat";

	private User sender;

	private Context context;

	private List<Shout> shouts;
	private TestShout unique;

	@Before
	public void setUp() {
		this.sender = new TestUser("dadrian");
		this.context = new Activity();
		shouts = new ArrayList<Shout>();
		for (int i = 0; i < 5; i++) {
			byte[] hash = TestFactory.genByteArray(32);
			byte[] sig = TestFactory.genByteArray(32);
			TestShout test = new TestShout(sender, null, MESSAGE, DateTime.now(), sig, hash);
			ShoutProviderContract.saveShout(context, test);
			shouts.add(test);
		}
		unique = new TestShout(sender, null, "Imma firin mah lazor!", DateTime.now(),
				TestFactory.genByteArray(18), TestFactory.genByteArray(8));
		ShoutProviderContract.saveShout(context, unique);
	}

	@After
	public void takeDown() {
		this.sender = null;
		this.context = null;
		this.shouts = null;
	}

	@Test
	public void testSearchNoResults() {
		List<Shout> result = ShoutSearchContract // Trololololo
				.searchShoutMessage(context,
						"someStringNotInDbBecauseItContainViciousInsultsToDavidBByDavidA");
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void testSearchOneResult() {
		ShoutProviderContract.saveShout(context, unique);
		List<Shout> result = ShoutSearchContract.searchShoutMessage(context, "lazor");
		assertNotNull(result);
		assertFalse(result.isEmpty());
		ListIterator<Shout> it = result.listIterator();
		assertTrue(it.hasNext());
		Shout next = it.next();
		TestUtility.testEqualShoutFields(unique, next);
	}

	@Test
	public void testSearchManyResults() {
		List<Shout> result = ShoutSearchContract.searchShoutMessage(context, WORD);
		assertNotNull(result);
		int resultSize = result.size();
		int expectSize = shouts.size();
		assertTrue(expectSize == resultSize);
		ListIterator<Shout> resultIter = result.listIterator();
		ListIterator<Shout> origIter = shouts.listIterator();
		for (int i = 0; i < resultSize; i++) {
			Shout fromSearch = resultIter.next();
			Shout original = origIter.next();
			TestUtility.testEqualShoutFields(original, fromSearch);
		}
	}

	@Test
	public void testSearchPrefix() {
		TestShout similar = new TestShout(sender, null,
				"I am firing my employees because they spend too much time on Reddit",
				new DateTime(), TestFactory.genByteArray(9), TestFactory.genByteArray(9));
		ShoutProviderContract.saveShout(context, similar);
		List<Shout> result = ShoutSearchContract.searchShoutMessage(context, "firin*");
		assertNotNull(result);
		int expectedSize = 2;
		int resultSize = result.size();
		assertEquals(expectedSize, resultSize);
	}
}
