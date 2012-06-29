
package org.whispercomm.shout.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.ByteBuffer;
import java.security.interfaces.ECPublicKey;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestShout;
import org.whispercomm.shout.test.util.TestUser;

@RunWith(ShoutTestRunner.class)
public class SerializeUtilityTest {

	private static final String MESSAGE = "My shout message!";
	private static final String SENDER_NAME = "dadrian";
	private static final DateTime TIMESTAMP = new DateTime(8675309L);
	private static final ECPublicKey PUBLIC_KEY = TestFactory.genPublicKey();
	private static final byte[] HASH = TestFactory.genByteArray(SerializeUtility.HASH_SIZE);
	private static final byte[] SIGNATURE = TestFactory.genByteArray(SerializeUtility.MAX_SIGNATURE_DATA_SIZE);

	private TestUser sender;
	private TestShout shout;
	private TestShout signedParent;

	@Before
	public void setUp() {
		this.sender = new TestUser(SENDER_NAME, PUBLIC_KEY);
		this.shout = new TestShout(sender, null, MESSAGE, TIMESTAMP,
				SIGNATURE, HASH);
	}

	@After
	public void takeDown() {
		this.sender = null;
		this.shout = null;
		this.signedParent = null;
	}
	
	@Test
	public void testSerializeShoutNoParent() {
		byte[] shoutData = SerializeUtility.serializeShoutData(shout);
		ByteBuffer buffer = ByteBuffer.allocate(shoutData.length + 1 + shout.signature.length);
		buffer.put(shoutData);
		buffer.put((byte) (shout.signature.length & 0x00FF));
		buffer.put(shout.signature);
		Shout fromBytes = SerializeUtility.deserializeShout(1, buffer.array());
		assertNotNull(fromBytes);
		assertEquals(shout.getMessage(), fromBytes.getMessage());
		assertArrayEquals(shout.getSignature(), fromBytes.getSignature());
		assertEquals(shout.getSender().getUsername(), fromBytes.getSender().getUsername());
		assertEquals(shout.getSender().getPublicKey(), fromBytes.getSender().getPublicKey());
		assertNull(fromBytes.getParent());
		assertEquals(shout.getTimestamp(), fromBytes.getTimestamp());
	}
}
