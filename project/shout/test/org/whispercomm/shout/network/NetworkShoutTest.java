package org.whispercomm.shout.network;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.SimpleUser;
import org.whispercomm.shout.User;
import org.whispercomm.shout.Utility;
import org.whispercomm.shout.id.SignatureUtility;

public class NetworkShoutTest {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	String username;
	String msg;
	ECPublicKey pubKey;
	ECPrivateKey privateKey;

	@Before
	public void setup() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidAlgorithmParameterException {
		username = "testuser";
		msg = "test is so boring!!!";
		KeyPair kpa = Utility.genKeyPair();
		pubKey = (ECPublicKey) kpa.getPublic();
		privateKey = (ECPrivateKey) kpa.getPrivate();
	}

	/**
	 * this also test serialize() in SignatureUtility.
	 */
	@Test
	public void testGetShoutBody() throws UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchProviderException,
			InvalidKeySpecException {
		DateTime timestamp = new DateTime();
		User user = new SimpleUser(username, pubKey);
		Shout shoutOri = null;
		byte[] shoutBody = SignatureUtility.serialize(timestamp, user, msg, shoutOri);
		ByteBuffer byteBuffer = ByteBuffer.wrap(shoutBody);
		Shout shoutRecovered = NetworkShout.getShoutBody(byteBuffer);
		assertTrue(shoutRecovered.getTimestamp().equals(timestamp));
		assertTrue(new String(shoutRecovered.getSender().getPublicKey()
				.getEncoded()).compareToIgnoreCase(new String(pubKey
				.getEncoded())) == 0);
		assertTrue(shoutRecovered.getSender().getUsername().compareTo(username) == 0);
		assertTrue(shoutRecovered.getMessage().compareTo(msg) == 0);
		assertTrue(shoutRecovered.getParent() == null);
	}
	
}
