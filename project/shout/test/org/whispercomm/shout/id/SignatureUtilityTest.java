
package org.whispercomm.shout.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestUnsignedShout;

@RunWith(ShoutTestRunner.class)
public class SignatureUtilityTest {

	private Me me;
	private ECPublicKey publicKey;
	private ECPrivateKey privateKey;

	@Before
	public void setUp() {
		me = TestFactory.TEST_ME_1;
		publicKey = me.getPublicKey();
		privateKey = (ECPrivateKey) me.getKeyPair().getPrivate();
	}

	@After
	public void takeDown() {
		me = null;
		publicKey = null;
		privateKey = null;
	}

	@Test
	public void testGetPublicKeyFromBytes() throws InvalidKeySpecException {
		byte[] keyBytes = publicKey.getEncoded();
		ECPublicKey fromBytes = SignatureUtility.generatePublic(keyBytes);
		assertEquals(publicKey, fromBytes);
	}

	@Test
	public void testGetPrivateKeyFromBytes() throws InvalidKeySpecException {
		byte[] keyBytes = privateKey.getEncoded();
		ECPrivateKey fromBytes = SignatureUtility.getPrivateKeyFromBytes(keyBytes);
		assertEquals(privateKey, fromBytes);
	}

	@Test
	public void testInvalidSignatureIsInvalid() {
		byte[] dataBytes = TestFactory.genByteArray(1000);
		byte[] signature = TestFactory.genByteArray(71);
		assertFalse(SignatureUtility.verifySignature(dataBytes, signature, me.getPublicKey()));
	}

	@Test
	public void testValidSignatureFromShoutIsValid() {
		UnsignedShout shout = new TestUnsignedShout(me, null, "message content", DateTime.now());
		byte[] signature = SignatureUtility.generateSignature(shout, me);
		boolean valid = SignatureUtility.verifySignature(
				SerializeUtility.serializeShoutData(shout), signature, me.getPublicKey());
		assertTrue(valid);
	}

}
