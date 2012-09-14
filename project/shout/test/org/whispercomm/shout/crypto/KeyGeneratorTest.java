
package org.whispercomm.shout.crypto;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class KeyGeneratorTest {

	{
		Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
	}

	private KeyGenerator generator;

	private ECPublicKey publicKey;

	private ECPrivateKey privateKey;

	@Before
	public void setup() {
		generator = new KeyGenerator();
		ECKeyPair pair = generator.generateKeyPair();
		publicKey = pair.getPublicKey();
		privateKey = pair.getPrivateKey();
	}

	@Test
	public void testPublicKeyEncoding() throws NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchProviderException {
		byte[] encoded = KeyGenerator.encodePublic(publicKey);
		KeyFactory kf = KeyFactory.getInstance("ECDSA", "SC");
		PublicKey key = kf.generatePublic(new X509EncodedKeySpec(encoded));
		assertThat(key, is(not(nullValue())));
	}

	@Test
	public void testPrivateKeyEncoding() throws NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchProviderException {
		byte[] encoded = KeyGenerator.encodePrivate(privateKey);
		KeyFactory kf = KeyFactory.getInstance("ECDSA", "SC");
		PrivateKey key = kf.generatePrivate(new PKCS8EncodedKeySpec(encoded));
		assertThat(key, is(not(nullValue())));

	}
}
