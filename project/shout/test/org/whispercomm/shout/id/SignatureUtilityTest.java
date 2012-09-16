
package org.whispercomm.shout.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.crypto.DsaSignature;
import org.whispercomm.shout.crypto.ECPrivateKey;
import org.whispercomm.shout.crypto.ECPublicKey;
import org.whispercomm.shout.crypto.EcdsaWithSha256;
import org.whispercomm.shout.crypto.KeyGenerator;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestShout;
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
		privateKey = me.getPrivateKey();
	}

	@After
	public void takeDown() {
		me = null;
		publicKey = null;
		privateKey = null;
	}

	@Test
	public void testGetPublicKeyFromBytes() throws InvalidKeySpecException {
		byte[] encoded = KeyGenerator.encodePublic(publicKey);
		ECPublicKey decoded = KeyGenerator.generatePublic(encoded);
		assertEquals(publicKey, decoded);
	}

	@Test
	public void testGetPrivateKeyFromBytes() throws InvalidKeySpecException {
		byte[] encoded = KeyGenerator.encodePrivate(privateKey);
		ECPrivateKey decoded = KeyGenerator.generatePrivate(encoded);
		assertEquals(privateKey, decoded);
	}

	@Test
	public void testInvalidSignatureIsInvalid() {
		byte[] dataBytes = TestFactory.genByteArray(1000);
		DsaSignature signature = new DsaSignature(BigInteger.valueOf(238423234),
				BigInteger.valueOf(3483489234L));
		assertFalse(EcdsaWithSha256.verify(signature, dataBytes, me.getPublicKey()));
	}

	@Test
	public void testValidSignatureFromShoutIsValid() {
		UnsignedShout unsigned = new TestUnsignedShout(me, null, "message content", null,
				DateTime.now());
		DsaSignature signature = SignatureUtility.signShout(unsigned, me);
		Shout shout = new TestShout(unsigned.getSender(), null, unsigned.getMessage(),
				unsigned.getTimestamp(), signature, null);
		boolean valid = SignatureUtility.verifyShout(shout);
		assertTrue(valid);
	}

}
