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

import android.content.Context;
import android.util.Log;

public class SignatureUtility {

	public static final String ECC_PARAMS = "secp256r1";
	public static final String CRYPTO_ALGO = "ECDSA";
	public static final String CRYPTO_PROVIDER = "SC";
	public static final String SIGN_ALGO = "SHA1WITHECDSA";
	public static final String HASH_ALGO = "SHA-256";
	private static final String TAG = SignatureUtility.class.getSimpleName();

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	IdStorage idStorage;

	public SignatureUtility(Context context) {
		this.idStorage = new IdStorageSharedPrefs(context);
	}

	public SignatureUtility(IdStorage idStorage) {
		this.idStorage = idStorage;
	}

	/**
	 * Generate the public/private key pair. This is called when the application
	 * is first launched.
	 * 
	 * @return TODO
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 */
	protected boolean genKeyPairs() {
		try {
			// generate key pairs
			ECGenParameterSpec ecParamSpec = new ECGenParameterSpec(ECC_PARAMS);
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(CRYPTO_ALGO,
					CRYPTO_PROVIDER);
			kpg.initialize(ecParamSpec);
			KeyPair kpA = kpg.generateKeyPair();
			idStorage.updateKeyPair(kpA);
			return true;
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			Log.e(TAG, e.getMessage());
		}
		return false;
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
	static public ECPublicKey getPublicKeyFromBytes(byte[] keyBytes) {
		try {
			KeyFactory kf = KeyFactory
					.getInstance(CRYPTO_ALGO, CRYPTO_PROVIDER);

			X509EncodedKeySpec x509ks = new X509EncodedKeySpec(keyBytes);
			ECPublicKey pubKey = (ECPublicKey) kf.generatePublic(x509ks);
			return pubKey;
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage());
		}
		// TODO
		return null;
	}

	/**
	 * Allow UI to create or update user name.
	 * 
	 * @param userName
	 * @throws UserNameInvalidException
	 * @throws Exception
	 */
	public synchronized void updateUserName(String userName)
			throws UserNameInvalidException {
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
	public User getUser() {
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
			ECPublicKey pubKey, byte[] data) {
		Signature signature;
		try {
			signature = Signature.getInstance(SIGN_ALGO);
			signature.initVerify(pubKey);
			signature.update(data);
			return signature.verify(DataSignature);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeyException e) {
			Log.e(TAG, e.getMessage());
		} catch (SignatureException e) {
			Log.e(TAG, e.getMessage());
		}
		return false;
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
	public static byte[] genSignature(byte[] data, ECPrivateKey privKey) {
		Signature signature;
		try {
			signature = Signature.getInstance(SIGN_ALGO);
			signature.initSign(privKey);
			signature.update(data);
			byte[] dataSignature = signature.sign();
			return dataSignature;
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeyException e) {
			Log.e(TAG, e.getMessage());
		} catch (SignatureException e) {
			Log.e(TAG, e.getMessage());
		}
		return null;
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
			String content, Shout shoutOri) {
		byte[] shoutBody;
		try {
			// Serialize the shout
			shoutBody = serialize(timestamp, sender, content, shoutOri);

			// Generate the hash
			MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
			md.update(shoutBody);
			return md.digest();
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		}
		// Should never happen
		return null;

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
	 * @throws UnsupportedEncodingException
	 */
	public byte[] genShoutSignature(DateTime timestamp, User sender,
			String content, Shout shoutOri) throws UnsupportedEncodingException {
		// Serialize the shout
		byte[] dataBytes = serialize(timestamp, sender, content, shoutOri);
		ECPrivateKey privKey = idStorage.getPrivateKey();
		byte[] signature = genSignature(dataBytes, privKey);
		return signature;
	}

	/**
	 * Serialize a shout message (not including signature) into the destination
	 * buffer
	 * 
	 * @param timestamp
	 *            send time of the shout
	 * @param sender
	 *            sender of the shout
	 * @param content
	 *            content of the shout
	 * @param shoutOri
	 *            the original shout of the shout
	 * 
	 * @return a Shout serialized into a network packet
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] serialize(DateTime timestamp, User sender,
			String content, Shout shoutOri) throws UnsupportedEncodingException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(NetworkShout.MAX_LEN);
		int size = 0;
		while (timestamp != null) {
			// time
			byteBuffer.putLong(timestamp.getMillis());
			size += NetworkShout.TIME_STAMP_SIZE;
			// senderNameLen and senderName
			byte[] usernameBytes = sender.getUsername().getBytes(
					Shout.CHARSET_NAME);
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
				content = shoutOri.getMessage();
				shoutOri = shoutOri.getParent();
			} else
				timestamp = null;
		}
		return Arrays.copyOfRange(byteBuffer.array(), 0, size);
	}

}
