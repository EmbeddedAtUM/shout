
package org.whispercomm.shout.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestMe;

@RunWith(ShoutTestRunner.class)
public class SignatureUtilityTest {

	private TestMe me;
	private ECPrivateKey privateKey;
	private ECPublicKey publicKey;

	@Before
	public void setUp() {
		me = new TestMe("skatherine");
		this.privateKey = (ECPrivateKey) me.getKeyPair().getPrivate();
		this.publicKey = me.getPublicKey();
	}

	@After
	public void takeDown() {

	}

	@Test
	public void testGetPublicKeyFromBytes() {
		byte[] keyBytes = publicKey.getEncoded();
		ECPublicKey fromBytes = SignatureUtility.getPublicKeyFromBytes(keyBytes);
		assertEquals(publicKey, fromBytes);
	}

	@Test
	public void testGetPrivateKeyFromBytes() {
		byte[] keyBytes = privateKey.getEncoded();
		ECPrivateKey fromBytes = SignatureUtility.getPrivateKeyFromBytes(keyBytes);
		assertEquals(privateKey, fromBytes);
	}

	@Test
	public void testSignatureParametersDoNotThrowExceptions() {
		byte[] dataBytes = TestFactory.genByteArray(1000);
		byte[] signature = SignatureUtility.generateSignature(dataBytes, me);
		assertNotNull(signature);
	}

	@Test
	public void testValidSignatureIsValid() {
		byte[] dataBytes = TestFactory.genByteArray(1000);
		byte[] signature = SignatureUtility.generateSignature(dataBytes, me);
		assertTrue(SignatureUtility.verifySignature(dataBytes, signature, me.getPublicKey()));
	}
	
	@Test
	public void testInvalidSignatureIsInvalid() {
		byte[] dataBytes = TestFactory.genByteArray(1000);
		byte[] signature = TestFactory.genByteArray(71);
		assertFalse(SignatureUtility.verifySignature(dataBytes, signature, me.getPublicKey()));
	}

}
