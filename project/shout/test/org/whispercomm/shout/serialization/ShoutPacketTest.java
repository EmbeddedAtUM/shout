
package org.whispercomm.shout.serialization;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.serialization.ShoutPacket.PacketBuilder;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestUtility;

@RunWith(ShoutTestRunner.class)
public class ShoutPacketTest {

	private void testSerDesPacket(Shout shout) throws ShoutChainTooLongException,
			BadShoutVersionException, ShoutPacketException, InvalidShoutSignatureException {
		PacketBuilder builder = new ShoutPacket.PacketBuilder();
		builder.addShout(shout);

		byte[] body = builder.build().getPacketBytes();
		assertNotNull(body);

		Shout deserialized = ShoutPacket.wrap(body).decodeShout();
		assertNotNull(deserialized);

		TestUtility.testEqualShoutFields(shout, deserialized);
	}

	@Test
	public void testSerDesRecomment() throws ShoutChainTooLongException, BadShoutVersionException,
			ShoutPacketException, InvalidShoutSignatureException {
		testSerDesPacket(TestFactory.RECOMMENT_SHOUT);
	}

	@Test
	public void testSerDesComment() throws ShoutChainTooLongException, BadShoutVersionException,
			ShoutPacketException, InvalidShoutSignatureException {
		testSerDesPacket(TestFactory.COMMENT_SHOUT);
	}

	@Test
	public void testSerDesShout() throws ShoutChainTooLongException, ShoutPacketException,
			BadShoutVersionException, InvalidShoutSignatureException {
		testSerDesPacket(TestFactory.ROOT_SHOUT);
	}

	@Test
	public void testSerDesReshout() throws ShoutChainTooLongException, ShoutPacketException,
			BadShoutVersionException, InvalidShoutSignatureException {
		testSerDesPacket(TestFactory.RESHOUT_SHOUT);
	}
}
