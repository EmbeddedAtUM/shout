package org.whispercomm.shout.id;

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
import java.security.spec.X509EncodedKeySpec;

import org.joda.time.DateTime;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.network.NetworkShout;
import org.whispercomm.shout.util.Arrays;

import android.app.Activity;

public class SignatureUtility {

	public static final String ECC_PARAMS = "secp256r1";
	public static final String CRYPTO_ALGO = "ECDSA";
	public static final String CRYPTO_PROVIDER = "SC";
	public static final String SIGN_ALGO = "SHA1WITHECDSA";
	public static final String HASH_ALGO = "SHA-256";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	IdStorage idStorage;

	public SignatureUtility(Activity callerActivity) {
		this.idStorage = new IdStorageSharedPrefs(callerActivity);
	}

	public SignatureUtility(IdStorage idStorage) {
		this.idStorage = idStorage;
	}

	/**
	 * Generate the public/private key pair. This is called when the application
	 * is first launched.
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 */
	protected void genKeyPairs() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidAlgorithmParameterException {
		// generate key pairs
		ECGenParameterSpec ecParamSpec = new ECGenParameterSpec(ECC_PARAMS);
		KeyPairGenerator kpg = KeyPairGenerator.getInstance(CRYPTO_ALGO,
				CRYPTO_PROVIDER);
		kpg.initialize(ecParamSpec);

		KeyPair kpA = kpg.generateKeyPair();

		idStorage.updateKeyPair(kpA);
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

		KeyFactory kf = KeyFactory.getInstance(CRYPTO_ALGO, CRYPTO_PROVIDER);
		X509EncodedKeySpec x509ks = new X509EncodedKeySpec(keyBytes);
		ECPublicKey pubKey = (ECPublicKey) kf.generatePublic(x509ks);

		return pubKey;
	}

	/**
	 * Allow UI to create or update user name.
	 * 
	 * @param userName
	 * @throws Exception
	 */
	public synchronized void updateUserName(String userName) throws Exception {
		if (userName == null
				|| userName.length() > NetworkShout.MAX_USER_NAME_LEN)
			throw new UserNameInvalidException();
		idStorage.updateUserName(userName);
		// create key pair if not exist
		if (idStorage.getPublicKey() == null
				|| idStorage.getPrivateKey() == null) {
			genKeyPairs();
		}
	}

	/**
	 * @return information of current user in form of User object.
	 * @throws Exception
	 */
	public User getUser() throws Exception {
		return idStorage.getUser();
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
		byte[] shoutBody = serialize(timestamp, sender, content, shoutOri);

		MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
		md.update(shoutBody);
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
	 * @throws Exception
	 */
	public byte[] genShoutSignature(DateTime timestamp, User sender,
			String content, Shout shoutOri) throws Exception {
		// Serialize the shout
		byte[] dataBytes = serialize(timestamp, sender, content, shoutOri);
		ECPrivateKey privKey = idStorage.getPrivateKey();
		byte[] signature = genSignature(dataBytes, privKey);
		return signature;
	}

	/**
	 * Serialize a shout message (not including signature) into the destination
	 * buffer
	 * @param timestamp
	 *            send time of the shout
	 * @param sender
	 *            sender of the shout
	 * @param content
	 *            content of the shout
	 * @param shoutOri
	 *            the original shout of the shout
	 * 
	 * @return TODO
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] serialize(DateTime timestamp, User sender,
			String content, Shout shoutOri)
			throws UnsupportedEncodingException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(NetworkShout.MAX_LEN);
		int size = 0;
		while (timestamp != null) {
			// time
			byteBuffer.putLong(timestamp.getMillis());
			size += NetworkShout.TIME_STAMP_SIZE;
			// senderNameLen and senderName
			byte[] usernameBytes = sender.getUsername().getBytes(Shout.CHARSET_NAME);
			int senderNameLen = usernameBytes.length;
			byteBuffer.put((byte) senderNameLen);
			size += NetworkShout.SENDER_NAME_LEN_SIZE;
			byteBuffer.put(usernameBytes);
			size += usernameBytes.length;
			// senderPubKey
			byte[] pubKeyBytes = sender.getPublicKey().getEncoded();
			if (pubKeyBytes == null)
				throw new UnsupportedEncodingException(
						"ECPublicKey does not support encoding.");
			byteBuffer.put(pubKeyBytes);
			size += NetworkShout.KEY_LENGTH;
			// contentLen and content
			byte[] contentBytes = content.getBytes(Shout.CHARSET_NAME);
			int contentLen = contentBytes.length;
			byteBuffer.putChar((char) contentLen);
			size += NetworkShout.CONTENT_LEN_SIZE;
			byteBuffer.put(contentBytes);
			size += contentBytes.length;
			// hasReshout
			byteBuffer.put((byte) (shoutOri == null ? 0 : 1));
			size += NetworkShout.HAS_RESHOUT_SIZE;
			// update
			if (shoutOri != null) {
				timestamp = shoutOri.getTimestamp();
				sender = shoutOri.getSender();
				content = shoutOri.getContent();
				shoutOri = shoutOri.getOriginalShout();
			} else
				timestamp = null;
		}
		return Arrays.copyOfRange(byteBuffer.array(), 0, size);
	}

}
