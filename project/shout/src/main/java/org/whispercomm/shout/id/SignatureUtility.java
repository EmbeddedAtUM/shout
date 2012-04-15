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

import android.app.Activity;

public class SignatureUtility {

	static final String ECC_PARAMS = "secp256r1";
	static final String CRYPTO_ALGO = "ECDSA";
	static final String CRYPTO_PROVIDER = "SC";
	static final String SIGN_ALGO = "SHA1WITHECDSA";
	static final String HASH_ALGO = "SHA-256";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	IdStorage idStorage;

	public SignatureUtility(Activity callerActivity) {
		this.idStorage = new IdStorageSharedPrefs(callerActivity);
	}
	
	public SignatureUtility(IdStorage idStorage){
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
	public synchronized void updateUserName(String userName)
			throws Exception {
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
	 * @throws Exception 
	 */
	public byte[] genShoutSignature(DateTime timestamp, User sender,
			String content, Shout shoutOri)
			throws Exception {
		// Serialize the shout
		ByteBuffer byteBuffer = ByteBuffer.allocate(NetworkShout.MAX_LEN);
		serialize(byteBuffer, timestamp, sender, content, shoutOri);
		ECPrivateKey privKey = idStorage.getPrivateKey();
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
