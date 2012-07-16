
package org.whispercomm.shout.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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

	private static final String VERSION_EXCEPTION_FAIL = "Shout version is not bad";
	private static final String PACKET_EXCEPTION_FAIL = "ShoutPacketException thrown";
	private static final String SHOUT_CHAIN_FAIL = "ShoutChainTooLongException thrown";

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
		try {
			PacketBuilder builder = new ShoutPacket.PacketBuilder();
			builder.addShout(recomment);
			ShoutPacket packet = builder.build();
			assertNotNull(packet);
			int count = packet.getShoutCount();
			assertEquals(3, count);
			byte[] body = packet.getBodyBytes();
			assertNotNull(body);
			Shout fromBytes = packet.decodeShout();
			assertNotNull(fromBytes);
			TestUtility.testEqualShoutFields(recomment, fromBytes);
		} catch (BadShoutVersionException e) {
			fail(VERSION_EXCEPTION_FAIL);
			return;
		} catch (ShoutPacketException e) {
			fail(PACKET_EXCEPTION_FAIL);
			return;
		} catch (ShoutChainTooLongException e) {
			e.printStackTrace();
			fail(SHOUT_CHAIN_FAIL);
		} catch (InvalidShoutSignatureException e) {
			fail();
		}

	}

	@Test
	public void testBuildComment() {
		try {
			PacketBuilder builder = new ShoutPacket.PacketBuilder();
			builder.addShout(comment);
			ShoutPacket packet = builder.build();
			assertNotNull(packet);
			int count = packet.getShoutCount();
			assertEquals(2, count);
			byte[] body = packet.getBodyBytes();
			assertNotNull(body);
			Shout fromBytes = packet.decodeShout();
			TestUtility.testEqualShoutFields(comment, fromBytes);
		} catch (BadShoutVersionException e) {
			e.printStackTrace();
			fail(VERSION_EXCEPTION_FAIL);
		} catch (ShoutPacketException e) {
			e.printStackTrace();
			fail(PACKET_EXCEPTION_FAIL);
		} catch (ShoutChainTooLongException e) {
			e.printStackTrace();
			fail(SHOUT_CHAIN_FAIL);
		} catch (InvalidShoutSignatureException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testWrapShout() {
		try {
			PacketBuilder builder = new ShoutPacket.PacketBuilder();
			builder.addShout(shout);
			ShoutPacket packet = builder.build();
			assertNotNull(packet);
			byte[] packetBytes = packet.getPacketBytes();
			assertNotNull(packetBytes);
			ShoutPacket wrapper = ShoutPacket.wrap(packetBytes);
			assertEquals(1, wrapper.getShoutCount());
			Shout decoded = wrapper.decodeShout();
			assertNotNull(decoded);
			TestUtility.testEqualShoutFields(shout, decoded);
		} catch (ShoutChainTooLongException e) {
			e.printStackTrace();
			fail(SHOUT_CHAIN_FAIL);
		} catch (BadShoutVersionException e) {
			e.printStackTrace();
			fail(VERSION_EXCEPTION_FAIL);
		} catch (ShoutPacketException e) {
			e.printStackTrace();
			fail(PACKET_EXCEPTION_FAIL);
		} catch (InvalidShoutSignatureException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testWrapComment() {
		try {
			PacketBuilder builder = new ShoutPacket.PacketBuilder();
			builder.addShout(comment);
			ShoutPacket packet = builder.build();
			assertNotNull(packet);
			byte[] packetBytes = packet.getPacketBytes();
			assertNotNull(packetBytes);
			ShoutPacket wrapper = ShoutPacket.wrap(packetBytes);
			assertEquals(2, wrapper.getShoutCount());
			Shout decoded = wrapper.decodeShout();
			assertNotNull(decoded);
			TestUtility.testEqualShoutFields(comment, decoded);
		} catch (BadShoutVersionException e) {
			e.printStackTrace();
			fail(VERSION_EXCEPTION_FAIL);
		} catch (ShoutChainTooLongException e) {
			e.printStackTrace();
			fail(SHOUT_CHAIN_FAIL);
		} catch (ShoutPacketException e) {
			e.printStackTrace();
			fail(PACKET_EXCEPTION_FAIL);
		} catch (InvalidShoutSignatureException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testWrapRecomment() {
		try {
			PacketBuilder builder = new ShoutPacket.PacketBuilder();
			builder.addShout(recomment);
			ShoutPacket packet = builder.build();
			assertNotNull(packet);
			byte[] packetBytes = packet.getPacketBytes();
			ShoutPacket wrapper = ShoutPacket.wrap(packetBytes);
			assertEquals(3, wrapper.getShoutCount());
			Shout decoded = wrapper.decodeShout();
			assertNotNull(decoded);
			TestUtility.testEqualShoutFields(recomment, decoded);
		} catch (ShoutChainTooLongException e) {
			e.printStackTrace();
			fail(SHOUT_CHAIN_FAIL);
		} catch (ShoutPacketException e) {
			e.printStackTrace();
			fail(PACKET_EXCEPTION_FAIL);
		} catch (BadShoutVersionException e) {
			e.printStackTrace();
			fail(VERSION_EXCEPTION_FAIL);
		} catch (InvalidShoutSignatureException e) {
			e.printStackTrace();
			fail();
		}
	}
}
