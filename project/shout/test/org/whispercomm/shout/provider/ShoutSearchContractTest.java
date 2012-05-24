
package org.whispercomm.shout.provider;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
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

import android.app.Activity;
import android.content.Context;

@RunWith(ShoutTestRunner.class)
public class ShoutSearchContractTest {

	private static final String MESSAGE = "Could you repeat the part where you said all the stuff about the things";
	private static final String WORD = "repeat";

	private User sender;

	private Context context;

	private List<Shout> shouts;
	private HashMap<Integer, Shout> idMap;

	@Before
	public void setUp() {
		this.sender = new TestUser("drbeagle"); // And that is what happens when
												// you Google people you work
												// with
		this.context = new Activity();
		shouts = new ArrayList<Shout>();
		idMap = new HashMap<Integer, Shout>();
		for (int i = 0; i < 5; i++) {
			byte[] hash = TestFactory.genByteArray(32);
			byte[] sig = TestFactory.genByteArray(32);
			TestShout test = new TestShout(sender, null, MESSAGE, new DateTime(), sig, hash);
			shouts.add(test);
			int id = ShoutProviderContract.storeShout(context, test);
			idMap.put(id, test);
		}
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
				.searchShoutMessage("someStringNotInDbBecauseItContainViciousInsultsToDavidBByDavidA");
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void testSearchOneResult() {
		TestShout unique = new TestShout(sender, null, "Imma firin mah lazor!", new DateTime(),
				TestFactory.genByteArray(18), TestFactory.genByteArray(8));
		ShoutProviderContract.storeShout(context, unique);
		List<Shout> result = ShoutSearchContract.searchShoutMessage("lazor");
		assertNotNull(result);
		assertFalse(result.isEmpty());
		ListIterator<Shout> it = result.listIterator();
		assertTrue(it.hasNext());
		Shout next = it.next();
		assertEquals(unique.getMessage(), next.getMessage());
		assertEquals(unique.getTimestamp(), next.getTimestamp());
		assertEquals(unique.getSender().getPublicKey(), next.getSender().getPublicKey());
		assertArrayEquals(unique.hash, next.getHash());
		assertArrayEquals(unique.signature, next.getSignature());
	}

	@Test
	public void testSearchManyResults() {
		List<Shout> result = ShoutSearchContract.searchShoutMessage(WORD);
		int resultSize = result.size();
		int expectSize = shouts.size();
		assertTrue(expectSize == resultSize);
		ListIterator<Shout> resultIter = result.listIterator();
		ListIterator<Shout> origIter = shouts.listIterator();
		for (int i = 0; i < resultSize; i++) {
			Shout from = resultIter.next();
			Shout orig = origIter.next();
			assertEquals(orig.getMessage(), from.getMessage());
			assertEquals(orig.getTimestamp(), from.getTimestamp());
			assertEquals(orig.getSender().getPublicKey(), from.getSender().getPublicKey());
			assertArrayEquals(orig.getHash(), from.getHash());
			assertArrayEquals(orig.getSignature(), from.getSignature());
		}
	}

}
