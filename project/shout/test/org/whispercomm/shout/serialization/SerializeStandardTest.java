
package org.whispercomm.shout.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.SerialShouts;
import org.whispercomm.shout.test.util.TestUtility;

@RunWith(ShoutTestRunner.class)
public class SerializeStandardTest {

	@Before
	public void setUp() {

	}

	@After
	public void takeDown() {

	}

	@Test
	public void testSerializeSingleShoutNoSignature() {
		Shout grandparent = SerialShouts.GRANDPARENT;
		byte[] grandparentBytes = SerialShouts.GRANDPARENT_SERIALIZED;
		byte[] fromUtility = SerializeUtility.serializeShoutData(grandparent);
		assertArrayEquals(grandparentBytes, fromUtility);
	}

	@Test
	public void testDeserializeSingleShoutWithSignature() {
		try {
			Shout grandparent = SerialShouts.GRANDPARENT;
			byte[] signedGrandparent = SerialShouts.GRANDPARENT_SIGNED;
			Shout fromBytes = SerializeUtility.deserializeShout(1, signedGrandparent);
			assertNotNull(fromBytes);
			TestUtility.testEqualShoutFields(grandparent, fromBytes);
		} catch (BadShoutVersionException e) {
			fail("Shout version is not bad!");
		} catch (ShoutPacketException e) {
			fail("Shout packet is valid!");
		}
	}
	
	// TODO The rest of the Shouts
}
