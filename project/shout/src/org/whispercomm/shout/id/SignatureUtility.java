
package org.whispercomm.shout.id;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.joda.time.DateTime;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.network.NetworkShout;
import org.whispercomm.shout.util.Arrays;

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
	
	/*
	 * Make this class uninstantiable
	 */
	private SignatureUtility() {
		throw new IllegalStateException("Cannot instantiate SignatureUtility!");
	}

	/**
	 * Get ECPublicKey from X.509 encoded bytes.
	 * 
	 * @param publicKeyBytes
	 * @return {@code null} on failure
	 */
	public static ECPublicKey getPublicKeyFromBytes(byte[] publicKeyBytes) {
		try {
			KeyFactory kf = KeyFactory.getInstance(CRYPTO_ALGO, CRYPTO_PROVIDER);
			X509EncodedKeySpec x509ks = new X509EncodedKeySpec(publicKeyBytes);
			ECPublicKey pubKey = (ECPublicKey) kf.generatePublic(x509ks);
			return pubKey;
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage());
		}
		return null;
	}

	/**
	 * Get private key from PKCS8 encoded bytes
	 * 
	 * @param privateKeyBytes
	 * @return {@code null} on failure
	 */
	public static ECPrivateKey getPrivateKeyFromBytes(byte[] privateKeyBytes) {
		try {
			KeyFactory kf = KeyFactory.getInstance(CRYPTO_ALGO, CRYPTO_PROVIDER);
			PKCS8EncodedKeySpec p8ks = new PKCS8EncodedKeySpec(privateKeyBytes);
			ECPrivateKey privateKey = (ECPrivateKey) kf.generatePrivate(p8ks);
			return privateKey;
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage());
		}
		return null;
	}

	/**
	 * Verify the signature of data using pubKey.
	 * 
	 * @param signature
	 * @param pubKey
	 * @param data in ByteBuffer
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static boolean verifySignature(byte[] DataSignature,
			ECPublicKey pubKey, byte[] data) {
		Signature signature;
		try {
			signature = Signature.getInstance(SIGN_ALGO, CRYPTO_PROVIDER);
			signature.initVerify(pubKey);
			signature.update(data);
			return signature.verify(DataSignature);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeyException e) {
			Log.e(TAG, e.getMessage());
		} catch (SignatureException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage());
		}
		return false;
	}

	/**
	 * Generate the hash-code of the given shout.
	 * 
	 * @param timestamp
	 * @param sender
	 * @param message
	 * @param shoutOri
	 * @return hash-code of the shout
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] genShoutHash(UnsignedShout shout) throws UnsupportedEncodingException {
		byte[] shoutBody;
		try {
			// Serialize the shout
			shoutBody = serialize(shout);

			// Generate the hash
			MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
			md.update(shoutBody);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		}
		// Should never happen
		return null;

	}

	public static byte[] genShoutSignature(UnsignedShout shout, Me me)
			throws UnsupportedEncodingException {
		byte[] dataBytes;
		dataBytes = serialize(shout);
		ECPrivateKey privateKey = (ECPrivateKey) me.getKeyPair().getPrivate();
		Signature signature;
		try {
			signature = Signature.getInstance(SIGN_ALGO, CRYPTO_PROVIDER);
			signature.initSign(privateKey);
			signature.update(dataBytes);
			byte[] dataSignature = signature.sign();
			return dataSignature;
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeyException e) {
			Log.e(TAG, e.getMessage());
		} catch (SignatureException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage());
		}
		// Should never happen
		return null;
	}

	/**
	 * Serialize a shout message (not including signature) into the destination
	 * buffer
	 * 
	 * @param timestamp send time of the shout
	 * @param sender sender of the shout
	 * @param message content of the shout
	 * @param shoutOri the original shout of the shout
	 * @return a Shout serialized into a network packet
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] serialize(UnsignedShout unsignedShout) throws UnsupportedEncodingException {
		DateTime timestamp = unsignedShout.getTimestamp();
		User sender = unsignedShout.getSender();
		String content = unsignedShout.getMessage();
		Shout parent = unsignedShout.getParent();
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
			byte[] contentBytes = content == null ? new byte[0]
					: content.getBytes(Shout.CHARSET_NAME);
			int contentLen = contentBytes.length;
			byteBuffer.putChar((char) contentLen);
			size += NetworkShout.CONTENT_LEN_SIZE;
			byteBuffer.put(contentBytes);
			size += contentBytes.length;
			// hasReshout
			byteBuffer.put((byte) (parent == null ? 0 : 1));
			size += NetworkShout.HAS_RESHOUT_SIZE;
			// update
			if (parent != null) {
				timestamp = parent.getTimestamp();
				sender = parent.getSender();
				content = parent.getMessage();
				parent = parent.getParent();
			} else
				timestamp = null;
		}
		return Arrays.copyOfRange(byteBuffer.array(), 0, size);
	}

}
