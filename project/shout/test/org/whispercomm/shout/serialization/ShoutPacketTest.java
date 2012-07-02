
package org.whispercomm.shout.serialization;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.serialization.ShoutPacket.PacketBuilder;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestShout;
import org.whispercomm.shout.test.util.TestUser;
import org.whispercomm.shout.test.util.TestUtility;

@RunWith(ShoutTestRunner.class)
public class ShoutPacketTest {

	private static final int SIGNATURE_SIZE = 71;
	private TestShout shout;
	private TestShout recomment;
	private TestShout comment;

	private TestUser shouter;
	private TestUser reshouter;
	private TestUser commenter;

	@Before
	public void setUp() {
		shouter = new TestUser("dadrian");
		reshouter = new TestUser("duiu");
		commenter = new TestUser("DRBild");
		shout = new TestShout(shouter, null, "I shout cool things!", DateTime.now(),
				TestFactory.genByteArray(SIGNATURE_SIZE), null);
		comment = new TestShout(commenter, shout, "No you don't", DateTime.now(),
				TestFactory.genByteArray(SIGNATURE_SIZE), null);
		recomment = new TestShout(reshouter, comment, null, DateTime.now(),
				TestFactory.genByteArray(SIGNATURE_SIZE), null);
		shout.hash = SerializeUtility.generateHash(shout);
		comment.hash = SerializeUtility.generateHash(comment);
		recomment.hash = SerializeUtility.generateHash(recomment);
	}

	@After
	public void takeDown() {
		this.shout = null;
		this.recomment = null;
		this.comment = null;
		this.shouter = null;
		this.reshouter = null;
		this.commenter = null;
	}
	
	@Test
	public void testBuildRecomment() {
		PacketBuilder builder = new ShoutPacket.PacketBuilder();
		try {
			builder.addShout(recomment);
		} catch (ShoutChainTooLongException e) {
			fail("Shout chain is only 3!");
		}
		ShoutPacket packet = builder.build();
		assertNotNull(packet);
		int count = packet.getShoutCount();
		assertEquals(3, count);
		byte[] body = packet.getBodyBytes();
		assertNotNull(body);
		Shout fromBytes;
		try {
			fromBytes = packet.decodeShout();
		} catch (BadShoutVersionException e) {
			fail("BadShoutVersionException thrown");
			return;
		}
		assertNotNull(fromBytes);
		TestUtility.testEqualShoutFields(recomment, fromBytes);
	}
	
	@Test
	public void testBuildComment() {
		PacketBuilder builder = new ShoutPacket.PacketBuilder();
		try {
			builder.addShout(comment);
		} catch (ShoutChainTooLongException e) {
			fail("Shout chain is only 2!");
		}
		ShoutPacket packet = builder.build();
		assertNotNull(packet);
		int count = packet.getShoutCount();
		assertEquals(2, count);
		byte[] body = packet.getBodyBytes();
		assertNotNull(body);
		Shout fromBytes;
		try {
			fromBytes = packet.decodeShout();
		} catch (BadShoutVersionException e) {
			fail("BadShoutVersionException thrown");
			return;
		}
		TestUtility.testEqualShoutFields(comment, fromBytes);
	}
}
