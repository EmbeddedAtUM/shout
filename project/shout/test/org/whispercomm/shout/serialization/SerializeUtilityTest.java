
package org.whispercomm.shout.serialization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;

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

	private static final String VERSION_EXCEPTION_FAIL = "BadShoutVersionException thrown";
	private static final String PACKET_EXCEPTION_FAIL = "ShoutPacketException thrown";
	
	private static final String MESSAGE = "My shout message!";
	private static final DateTime TIMESTAMP = new DateTime(8675309L);
	private static final byte[] SIGNATURE = TestFactory
			.genByteArray(SerializeUtility.MAX_SIGNATURE_DATA_SIZE);

	private TestUser sender;
	private TestUser reshouter;
	private TestShout shout;
	private TestShout reshout;
	private byte[] shoutData;
	private byte[] reshoutData;

	@Before
	public void setUp() {
		// Make the shout
		this.sender = new TestUser("dadrian");
		this.shout = new TestShout(sender, null, MESSAGE, TIMESTAMP,
				SIGNATURE, null);
		this.shoutData = SerializeUtility.serializeShoutData(shout);
		shout.hash = SerializeUtility.generateHash(shoutData, shout.signature);

		// Make the reshout
		this.reshouter = new TestUser("DRBild");
		this.reshout = new TestShout(reshouter, shout, null, TIMESTAMP,
				TestFactory.genByteArray(SerializeUtility.MAX_SIGNATURE_DATA_SIZE), null);
		this.reshoutData = SerializeUtility.serializeShoutData(reshout);
		reshout.hash = SerializeUtility.generateHash(reshoutData, reshout.signature);
	}

	@After
	public void takeDown() {
		this.sender = null;
		this.shout = null;
	}

	@Test
	public void testSerializeShoutNoParent() {
		ByteBuffer buffer = ByteBuffer.allocate(shoutData.length + 1 + shout.signature.length);
		buffer.put(shoutData);
		buffer.put((byte) shout.signature.length);
		buffer.put(shout.signature);
		Shout fromBytes;
		try {
			fromBytes = SerializeUtility.deserializeShout(1, buffer.array());
		} catch (BadShoutVersionException e) {
			fail(VERSION_EXCEPTION_FAIL);
			return;
		} catch (ShoutPacketException e) {
			fail(PACKET_EXCEPTION_FAIL);
			return;
		}
		assertNotNull(fromBytes);
		TestUtility.testEqualShoutFields(shout, fromBytes);
	}

	@Test
	public void testSerializeReshout() {
		ByteBuffer buffer = ByteBuffer.allocate(shoutData.length + 1 + shout.signature.length
				+ reshoutData.length + 1 + reshout.signature.length);
		buffer.put(reshoutData);
		buffer.put((byte) reshout.signature.length);
		buffer.put(reshout.signature);
		buffer.put(shoutData);
		buffer.put((byte) shout.signature.length);
		buffer.put(shout.signature);
		Shout fromBytes;
		try {
			fromBytes = SerializeUtility.deserializeShout(2, buffer.array());
		} catch (BadShoutVersionException e) {
			fail(VERSION_EXCEPTION_FAIL);
			return;
		} catch (ShoutPacketException e) {
			fail(PACKET_EXCEPTION_FAIL);
			return;
		}
		assertNotNull(fromBytes);
		TestUtility.testEqualShoutFields(reshout, fromBytes);
	}
}
