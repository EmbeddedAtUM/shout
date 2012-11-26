
package org.whispercomm.shout.serialization;

import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.network.ObjectProtocol;
import org.whispercomm.shout.network.ObjectType;
import org.whispercomm.shout.network.PacketProtocol;
import org.whispercomm.shout.network.UnsupportedVersionException;
import org.whispercomm.shout.network.shout.InvalidShoutSignatureException;
import org.whispercomm.shout.network.shout.ShoutChainTooLongException;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestUtility;

@RunWith(ShoutTestRunner.class)
public class ShoutPacketTest implements ObjectProtocol {

	private PacketProtocol packetProtocol;

	private ManesInterface manesInterface;

	private Shout received;

	@Before
	public void setup() {
		manesInterface = null; // not used
		packetProtocol = new PacketProtocol(manesInterface);
		packetProtocol.register(ObjectType.Shout, this);
	}

	@Override
	public void receive(ByteBuffer data) {
		// TODO Auto-generated method stub

	}

	private void testSerDesPacket(Shout shout) throws ShoutChainTooLongException,
			UnsupportedVersionException, ShoutPacketException, InvalidShoutSignatureException {

		ByteBuffer buffer = PacketProtocol.createPacket();

		while (shout != null) {
			SerializeUtility.serializeShout(buffer, shout);
			shout = shout.getParent();
		}

		buffer.flip();
		packetProtocol.receive(buffer);

		TestUtility.testEqualShoutFields(shout, received);
	}

	@Test
	public void testSerDesRecomment() throws ShoutChainTooLongException,
			UnsupportedVersionException,
			ShoutPacketException, InvalidShoutSignatureException {
		testSerDesPacket(TestFactory.RECOMMENT_SHOUT);
	}

	@Test
	public void testSerDesComment() throws ShoutChainTooLongException, UnsupportedVersionException,
			ShoutPacketException, InvalidShoutSignatureException {
		testSerDesPacket(TestFactory.COMMENT_SHOUT);
	}

	@Test
	public void testSerDesShout() throws ShoutChainTooLongException, ShoutPacketException,
			UnsupportedVersionException, InvalidShoutSignatureException {
		testSerDesPacket(TestFactory.ROOT_SHOUT);
	}

	@Test
	public void testSerDesReshout() throws ShoutChainTooLongException, ShoutPacketException,
			UnsupportedVersionException, InvalidShoutSignatureException {
		testSerDesPacket(TestFactory.RESHOUT_SHOUT);
	}

}
