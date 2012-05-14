package org.whispercomm.shout.id;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.whispercomm.shout.SimpleUser;
import org.whispercomm.shout.User;
import org.whispercomm.shout.Utility;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.id.IdStorage;
import org.whispercomm.shout.network.NetworkShout;

import com.xtremelabs.robolectric.RobolectricTestRunner;

import android.app.Activity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Matchers.argThat;

//*** serialize and getshoutsignature will be tested in the NetworkShoutTest
@RunWith(RobolectricTestRunner.class)
public class SignatureUtilityTest {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	Activity myActivity = new Activity();
	IdStorage idStorage = mock(IdStorage.class);
	SignatureUtility signUtility = new SignatureUtility(idStorage);

	class KeyPairMatcher extends ArgumentMatcher<KeyPair> {
		@Override
		public boolean matches(Object argument) {
			return argument instanceof KeyPair;
		}
	}

	/**
	 * test genKeyPairs(), as well getPublicKey() and getPrivateKey()
	 */
	@Test
	public void testGenKeyPairs() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidAlgorithmParameterException,
			InvalidKeySpecException, UserNotInitiatedException {
		signUtility.genKeyPairs();
		verify(idStorage).updateKeyPair(argThat(new KeyPairMatcher()));
	}

	@Test
	public void testGetPublicKeyFromBytes() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidAlgorithmParameterException,
			InvalidKeySpecException {
		ECPublicKey pubKey = (ECPublicKey) Utility.genKeyPair().getPublic();
		ByteBuffer byteBuffer = ByteBuffer.allocate(NetworkShout.KEY_LENGTH);
		byteBuffer.put(pubKey.getEncoded());
		byteBuffer.flip();
		byte[] pubKeyBytes = new byte[NetworkShout.KEY_LENGTH];
		byteBuffer.get(pubKeyBytes, 0, NetworkShout.KEY_LENGTH);
		assertNotNull(SignatureUtility.getPublicKeyFromBytes(pubKey
				.getEncoded()));
	}

	@Test
	public void testUpdateUserNameWithNull() {

		String userName = null;
		try {
			signUtility.updateUserName(userName);
		} catch (Exception e) {
			assertTrue(e instanceof UserNameInvalidException);
		}
	}

	@Test
	public void testUpdateUserNameTooLong() {

		String userName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		try {
			signUtility.updateUserName(userName);
		} catch (Exception e) {
			assertTrue(e instanceof UserNameInvalidException);
		}
	}

	@Test
	public void testUpdateUserNameNoKey() throws Exception {
		String userName = "testuser";
		when(idStorage.getPublicKey()).thenReturn(null);
		when(idStorage.getPrivateKey()).thenReturn(null);
		signUtility.updateUserName(userName);
		verify(idStorage).updateUserName(userName);
		verify(idStorage).updateKeyPair(argThat(new KeyPairMatcher()));
	}

	@Test
	public void testUpdateUserNameWithExistingKey() throws Exception {
		KeyPair kpA = Utility.genKeyPair();
		String username = "username";
		when(idStorage.getPublicKey())
				.thenReturn((ECPublicKey) kpA.getPublic());
		when(idStorage.getPrivateKey()).thenReturn(
				(ECPrivateKey) kpA.getPrivate());
		signUtility.updateUserName(username);
		verify(idStorage).updateUserName(username);
		verify(idStorage, never()).updateKeyPair(argThat(new KeyPairMatcher()));
	}

	/**
	 * test genSignature() and verifySignature()
	 * 
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 */
	@Test
	public void testSignature() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidAlgorithmParameterException,
			InvalidKeyException, SignatureException {

		KeyPair kpA = Utility.genKeyPair();
		ECPublicKey pubKey = (ECPublicKey) kpA.getPublic();
		ECPrivateKey privKey = (ECPrivateKey) kpA.getPrivate();
		byte[] data = new String("test data").getBytes();
		byte[] signature = SignatureUtility.genSignature(data, privKey);
		assertTrue(SignatureUtility.verifySignature(signature, pubKey, data));
	}

	/**
	 * also test verifyShoutSignature() in NetworkShout.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGenShoutSignature() throws Exception {

		KeyPair kpA = Utility.genKeyPair();
		ECPublicKey pubKey = (ECPublicKey) kpA.getPublic();
		ECPrivateKey privKey = (ECPrivateKey) kpA.getPrivate();
		when(idStorage.getPrivateKey()).thenReturn(privKey);

		DateTime timestamp = new DateTime();
		String username = "testuser";
		User user = new SimpleUser(username, pubKey);
		String msg = "test is boring!!!";
		byte[] signature = signUtility.genShoutSignature(timestamp, user, msg,
				null);

		byte[] shoutBody = SignatureUtility.serialize(timestamp, user, msg,
				null);
		ByteBuffer byteBuffer = ByteBuffer.wrap(shoutBody);

		assertNotNull(NetworkShout.verifyShoutSignature(signature, byteBuffer));
	}

	@Test
	public void testNetworkShoutToFromBytesOneShout() throws Exception {
		// setup
		KeyPair kpA = Utility.genKeyPair();
		ECPublicKey pubKey = (ECPublicKey) kpA.getPublic();
		ECPrivateKey privKey = (ECPrivateKey) kpA.getPrivate();
		when(idStorage.getPrivateKey()).thenReturn(privKey);

		// get a NetworkShout
		DateTime timestamp = new DateTime();
		String username = "testuser";
		User user = new SimpleUser(username, pubKey);
		String msg = "test is boring!!!";
		byte[] signature = signUtility.genShoutSignature(timestamp, user, msg,
				null);
		NetworkShout shout = new NetworkShout(timestamp, user, msg, signature,
				null);

		// get network bytes of the shout
		byte[] shoutBytes = shout.toNetworkBytes();

		NetworkShout shoutRecovered = new NetworkShout(shoutBytes);

		assertTrue(shoutRecovered.getTimestamp().equals(timestamp));
		assertTrue(new String(shoutRecovered.getSender().getPublicKey()
				.getEncoded()).compareToIgnoreCase(new String(pubKey
				.getEncoded())) == 0);
		assertTrue(shoutRecovered.getSender().getUsername().compareTo(username) == 0);
		assertTrue(shoutRecovered.getMessage().compareTo(msg) == 0);
		assertTrue(shoutRecovered.getParent() == null);
	}

	@Test
	public void testNetworkShoutToFromBytesThreeShouts() throws Exception {
		KeyPair kpA = Utility.genKeyPair();
		ECPublicKey pubKey = (ECPublicKey) kpA.getPublic();
		ECPrivateKey privKey = (ECPrivateKey) kpA.getPrivate();
		when(idStorage.getPrivateKey()).thenReturn(privKey);

		// get a NetworkShout
		DateTime timestamp = new DateTime();
		String username = "testuser";
		User user = new SimpleUser(username, pubKey);
		String msg = "test is boring!!!";
		byte[] signature = signUtility.genShoutSignature(timestamp, user, msg,
				null);
		NetworkShout shout = new NetworkShout(timestamp, user, msg, signature,
				null);
		byte[] signature2 = signUtility.genShoutSignature(timestamp, user, msg,
				shout);
		NetworkShout shout2 = new NetworkShout(timestamp, user, msg,
				signature2, shout);
		byte[] signature3 = signUtility.genShoutSignature(timestamp, user, msg,
				shout2);
		NetworkShout shout3 = new NetworkShout(timestamp, user, msg,
				signature3, shout2);

		// get network bytes of the shout
		byte[] shoutBytes = shout3.toNetworkBytes();

		NetworkShout shoutRecovered = new NetworkShout(shoutBytes);

		while (shoutRecovered != null) {
			assertTrue(shoutRecovered.getTimestamp().equals(timestamp));
			assertTrue(new String(shoutRecovered.getSender().getPublicKey()
					.getEncoded()).compareToIgnoreCase(new String(pubKey
					.getEncoded())) == 0);
			assertTrue(shoutRecovered.getSender().getUsername()
					.compareTo(username) == 0);
			assertTrue(shoutRecovered.getMessage().compareTo(msg) == 0);
			shoutRecovered = (NetworkShout) shoutRecovered.getParent();
		}
	}
}
