
package org.whispercomm.shout.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.network.UnsupportedVersionException;
import org.whispercomm.shout.network.shout.InvalidShoutSignatureException;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestUtility;

@RunWith(ShoutTestRunner.class)
public class SerializeStandardTest {

	@Before
	public void setUp() {

	}

	@After
	public void takeDown() {

	}

	// TODO: Remove when some real test are enabled. This just prevents warnings
	// from JUnit
	@Test
	public void dummyTest() {

	}

	// TODO: Fix manual sigs and hashes in TestFactory, update this test to use
	// them, and reenable.
	public void testSerializeSingleShoutNoSignature() {
		Shout grandparent = SerialShouts.GRANDPARENT;
		byte[] grandparentBytes = SerialShouts.GRANDPARENT_SERIALIZED;
		byte[] fromUtility = SerializeUtility.serializeShoutData(grandparent);
		assertArrayEquals(grandparentBytes, fromUtility);
	}

	// TODO: Fix manual sigs and hashes in TestFactory, update this test to use
	// them, and reenable.
	public void testDeserializeSingleShoutWithSignature() {
		try {
			Shout grandparent = SerialShouts.GRANDPARENT;
			byte[] signedGrandparent = SerialShouts.GRANDPARENT_SIGNED;
			Shout fromBytes = SerializeUtility.deserializeSequenceOfShouts(1, signedGrandparent);
			assertNotNull(fromBytes);
			TestUtility.testEqualShoutFields(grandparent, fromBytes);
		} catch (UnsupportedVersionException e) {
			fail("Shout version is not bad!");
		} catch (ShoutPacketException e) {
			fail("Shout packet is valid!");
		} catch (InvalidShoutSignatureException e) {
			fail();
		}
	}

	// TODO The rest of the Shouts
}
