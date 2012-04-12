package org.whispercomm.shout;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.joda.time.DateTime;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.whispercomm.shout.network.NetworkShout;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Base64;

public class SignatureUtility {

	public static final String SHARED_PREFS = "shout_user_keys";
	static final String USER_NAME = "user_name";
	static final String USER_PUB_KEY = "user_pub_key";
	static final String USER_PRIV_KEY = "user_priv_key";

	static final String ECC_PARAMS = "secp256r1";
	static final String SIGN_ALGO = "SHA1WITHECDSA";
	static final String HASH_ALGO = "SHA-256";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	SharedPreferences sharedPrefs;

	public SignatureUtility(Activity callerActivity) {
		this.sharedPrefs = callerActivity.getSharedPreferences(SHARED_PREFS, 0);
	}

	/**
	 * Generate the public/private key pair. This is called when the application
	 * is first launched.
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 */
	public void genKeyPairs() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidAlgorithmParameterException {
		// generate key pairs
		ECGenParameterSpec ecParamSpec = new ECGenParameterSpec(ECC_PARAMS);
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDSA", "SC");
		kpg.initialize(ecParamSpec);

		KeyPair kpA = kpg.generateKeyPair();

		byte[] pubKeyBytes = kpA.getPublic().getEncoded();
		String pubStr = Base64.encodeToString(pubKeyBytes, 0,
				pubKeyBytes.length, Base64.DEFAULT);
		byte[] privKeyBytes = kpA.getPrivate().getEncoded();
		String privStr = Base64.encodeToString(privKeyBytes, 0,
				privKeyBytes.length, Base64.DEFAULT);
		// store key pair in sharedPrefs
		SharedPreferences.Editor prefsEditor = sharedPrefs.edit();

		prefsEditor.putString(USER_PUB_KEY, pubStr);
		prefsEditor.putString(USER_PRIV_KEY, privStr);
		prefsEditor.commit();
	}

	/**
	 * @return the current user's public key from the sharedPrefs
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws UserNotInitiatedException
	 */
	public ECPublicKey getPublicKey() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException,
			UserNotInitiatedException {
		String pubKeyString = sharedPrefs.getString(USER_PUB_KEY, null);
		if (pubKeyString == null)
			throw new UserNotInitiatedException();
		byte[] pubKeyBytes = Base64.decode(pubKeyString, Base64.DEFAULT);

		KeyFactory kf = KeyFactory.getInstance("ECDSA", "SC");
		X509EncodedKeySpec x509ks = new X509EncodedKeySpec(pubKeyBytes);
		ECPublicKey pubKey = (ECPublicKey) kf.generatePublic(x509ks);

		return pubKey;
	}

	/**
	 * Get ECPublicKey from X.509 encoded bytes.
	 * 
	 * @param keyBytes
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeySpecException
	 */
	static public ECPublicKey getPublicKeyFromBytes(byte[] keyBytes)
			throws NoSuchAlgorithmException, NoSuchProviderException,
			InvalidKeySpecException {

		KeyFactory kf = KeyFactory.getInstance("ECDSA", "SC");
		X509EncodedKeySpec x509ks = new X509EncodedKeySpec(keyBytes);
		ECPublicKey pubKey = (ECPublicKey) kf.generatePublic(x509ks);

		return pubKey;
	}

	/**
	 * @return the current user's private key from the sharedPrefs
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws UserNotInitiatedException
	 */
	private ECPrivateKey getPrivateKey() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException,
			UserNotInitiatedException {
		String privKeyString = sharedPrefs.getString(USER_PRIV_KEY, null);
		if (privKeyString == null)
			throw new UserNotInitiatedException();
		byte[] privKeyBytes = Base64.decode(privKeyString, Base64.DEFAULT);

		KeyFactory kf = KeyFactory.getInstance("ECDSA", "SC");
		PKCS8EncodedKeySpec p8ks = new PKCS8EncodedKeySpec(privKeyBytes);
		ECPrivateKey privKey = (ECPrivateKey) kf.generatePrivate(p8ks);

		return privKey;
	}

	/**
	 * @return information of current user in form of User object.
	 * 
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws UserNotInitiatedException
	 */
	public User getUser() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException,
			UserNotInitiatedException {
		String userName = sharedPrefs.getString(USER_NAME, null);
		if (userName == null)
			throw new UserNotInitiatedException();
		ECPublicKey pubKey = getPublicKey();
		User sender = new SimpleUser(userName, pubKey);
		return sender;
	}

	/**
	 * Verify the signature of data using pubKey.
	 * 
	 * @param signature
	 * @param pubKey
	 * @param data
	 *            in ByteBuffer
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static boolean verifySignature(byte[] DataSignature,
			ECPublicKey pubKey, byte[] data) throws NoSuchAlgorithmException,
			InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance(SIGN_ALGO);
		signature.initVerify(pubKey);
		signature.update(data);
		return signature.verify(DataSignature);
	}

	/**
	 * Sign data using privKey.
	 * 
	 * @param data
	 * @param privKey
	 * @return signature
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static byte[] genSignature(byte[] data, ECPrivateKey privKey)
			throws NoSuchAlgorithmException, InvalidKeyException,
			SignatureException {
		Signature signature = Signature.getInstance(SIGN_ALGO);
		signature.initSign(privKey);
		signature.update(data);
		byte[] dataSignature = signature.sign();
		return dataSignature;
	}

	/**
	 * Generate the hash-code of the given shout.
	 * 
	 * @param timestamp
	 * @param sender
	 * @param content
	 * @param shoutOri
	 * @return hash-code of the shout
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] genShoutHash(DateTime timestamp, User sender,
			String content, Shout shoutOri)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		// Serialize the shout
		ByteBuffer byteBuffer = ByteBuffer.allocate(NetworkShout.MAX_LEN);
		serialize(byteBuffer, timestamp, sender, content, shoutOri);

		MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
		md.update(byteBuffer.array());
		return md.digest();
	}

	/**
	 * Generate the signature of a given shout message.
	 * 
	 * @param timestamp
	 * @param content
	 * @param shoutOri
	 * @param sender
	 * 
	 * @return signature
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws UserNotInitiatedException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 */
	public byte[] genShoutSignature(DateTime timestamp, User sender,
			String content, Shout shoutOri)
			throws UnsupportedEncodingException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException,
			UserNotInitiatedException, InvalidKeyException, SignatureException {
		// Serialize the shout
		ByteBuffer byteBuffer = ByteBuffer.allocate(NetworkShout.MAX_LEN);
		serialize(byteBuffer, timestamp, sender, content, shoutOri);
		ECPrivateKey privKey = getPrivateKey();
		byte[] signature = genSignature(byteBuffer.array(), privKey);
		return signature;
	}

	/**
	 * Serialize a shout message (not including signature) into the destination
	 * buffer
	 * 
	 * @param byteBuffer
	 *            the destination ByteButter
	 * @param timestamp
	 *            send time of the shout
	 * @param sender
	 *            sender of the shout
	 * @param content
	 *            content of the shout
	 * @param shoutOri
	 *            the original shout of the shout
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static void serialize(ByteBuffer byteBuffer, DateTime timestamp,
			User sender, String content, Shout shoutOri)
			throws UnsupportedEncodingException {
		while (timestamp != null) {
			// time
			byteBuffer.putLong(timestamp.getMillis());
			// senderNameLen and senderName
			int senderNameLen = sender.getUsername().length();
			byteBuffer.putInt(senderNameLen);
			byteBuffer.put(sender.getUsername().getBytes(Shout.CHARSET_NAME));
			// senderPubKey
			byte[] pubKeyBytes = sender.getPublicKey().getEncoded();
			if (pubKeyBytes == null)
				throw new UnsupportedEncodingException(
						"ECPublicKey does not support encoding.");
			byteBuffer.put(pubKeyBytes);
			// contentLen and content
			int contentLen = content.length();
			byteBuffer.putInt(contentLen);
			byteBuffer.put(content.getBytes(Shout.CHARSET_NAME));
			// hasReshout
			byteBuffer.putChar((char) (shoutOri == null ? 0 : 1));
			// update
			timestamp = shoutOri.getTimestamp();
			sender = shoutOri.getSender();
			content = shoutOri.getContent();
			shoutOri = shoutOri.getOriginalShout();
		}
	}

}
