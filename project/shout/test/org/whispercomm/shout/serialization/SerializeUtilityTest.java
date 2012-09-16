
package org.whispercomm.shout.serialization;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.network.UnsupportedVersionException;
import org.whispercomm.shout.network.shout.InvalidShoutSignatureException;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestUtility;

/**
 * Tests for serialization and deserialization of shouts
 * 
 * @author David R. Bild
 */
@RunWith(ShoutTestRunner.class)
public class SerializeUtilityTest {

	@Before
	public void setUp() {
	}

	@After
	public void takeDown() {
	}

	private void testShoutSerDes(Shout shout) throws UnsupportedVersionException,
			ShoutPacketException,
			InvalidShoutSignatureException {
		ByteBuffer serialized = ByteBuffer.allocate(3
				* SerializeUtility.SHOUT_SIGNED_SIZE_MAX);

		Shout cur = shout;
		int cnt = 0;
		while (cur != null) {
			SerializeUtility
					.serializeShout(serialized,
							cur);
			cur = cur.getParent();
			++cnt;
		}

		serialized.flip();
		Shout deserialized = SerializeUtility.deserializeSequenceOfShouts(cnt, serialized);

		TestUtility.testEqualShoutFields(shout, deserialized);
	}

	@Test
	public void testSerDesRoot() throws UnsupportedVersionException, ShoutPacketException,
			InvalidShoutSignatureException {
		testShoutSerDes(TestFactory.ROOT_SHOUT);
	}

	@Test
	public void testSerDesReshout() throws UnsupportedVersionException, ShoutPacketException,
			InvalidShoutSignatureException {
		testShoutSerDes(TestFactory.RESHOUT_SHOUT);
	}

	@Test
	public void testSerDesComment() throws UnsupportedVersionException, ShoutPacketException,
			InvalidShoutSignatureException {
		testShoutSerDes(TestFactory.COMMENT_SHOUT);
	}

	@Test
	public void testSerDesRecomment() throws UnsupportedVersionException, ShoutPacketException,
			InvalidShoutSignatureException {
		testShoutSerDes(TestFactory.RECOMMENT_SHOUT);
	}

	// TODO: Test hash function
	// TODO: Test SerDes of a chain of shouts
}
