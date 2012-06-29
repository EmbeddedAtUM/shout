
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
import org.whispercomm.shout.test.util.TestUtility;

@RunWith(ShoutTestRunner.class)
public class SerializeUtilityTest {

	private static final String MESSAGE = "My shout message!";
	private static final String SENDER_NAME = "dadrian";
	private static final DateTime TIMESTAMP = new DateTime(8675309L);
	private static final ECPublicKey PUBLIC_KEY = TestFactory.genPublicKey();
	private static final byte[] HASH = TestFactory.genByteArray(SerializeUtility.HASH_SIZE);
	private static final byte[] SIGNATURE = TestFactory
			.genByteArray(SerializeUtility.MAX_SIGNATURE_DATA_SIZE);

	private TestUser sender;
	private TestShout shout;
	private TestShout signedParent;
	private byte[] shoutData;

	@Before
	public void setUp() {
		this.sender = new TestUser(SENDER_NAME, PUBLIC_KEY);
		this.shout = new TestShout(sender, null, MESSAGE, TIMESTAMP,
				SIGNATURE, HASH);
		this.shoutData = SerializeUtility.serializeShoutData(shout);
		shout.hash = SerializeUtility.generateHash(shoutData, shout.signature);
	}

	@After
	public void takeDown() {
		this.sender = null;
		this.shout = null;
		this.signedParent = null;
	}

	@Test
	public void testSerializeShoutNoParent() {
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

	@Test
	public void testSerializeReshout() {
		TestUser reshouter = new TestUser("not_dadrian");
		TestShout reshout = new TestShout(reshouter, shout, null, DateTime.now(),
				TestFactory.genByteArray(75), null);
		byte[] reshoutBytes = SerializeUtility.serializeShoutData(reshout);
		reshout.hash = SerializeUtility.generateHash(reshoutBytes, reshout.signature);
		ByteBuffer buffer = ByteBuffer.allocate(shoutData.length + 1 + shout.signature.length
				+ reshoutBytes.length + 1 + reshout.signature.length);
		buffer.put(reshoutBytes);
		buffer.put((byte) (reshout.signature.length & 0x00FF));
		buffer.put(reshout.signature);
		buffer.put(shoutData);
		buffer.put((byte) (shout.signature.length & 0x00FF));
		buffer.put(shout.signature);
		Shout fromBytes = SerializeUtility.deserializeShout(2, buffer.array());
		TestUtility.testEqualShoutFields(reshout, fromBytes);
	}
}
