
package org.whispercomm.shout.id;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
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

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.serialization.SerializeUtility;

import android.util.Log;

public class SignatureUtility {

	public static final String ECC_PARAMS = "secp256r1";
	public static final String CRYPTO_ALGO = "ECDSA";
	public static final String CRYPTO_PROVIDER = "SC";
	public static final String SIGN_ALGORITHM = "SHA256withECDSA";
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
	 * @param data in ByteBuffer
	 * @param pubKey
	 * @param signature
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static boolean verifySignature(byte[] data,
			byte[] dataSignature, ECPublicKey pubKey) {
		try {
			Signature signature = Signature.getInstance(SIGN_ALGORITHM, CRYPTO_PROVIDER);
			signature.initVerify(pubKey);
			signature.update(data);
			return signature.verify(dataSignature);
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
		return false;
	}

	public static byte[] generateSignature(byte[] dataBytes, Me me) {
		ECPrivateKey privateKey = (ECPrivateKey) me.getKeyPair().getPrivate();
		try {
			Signature signature = Signature.getInstance(SIGN_ALGORITHM, CRYPTO_PROVIDER);
			signature.initSign(privateKey);
			signature.update(dataBytes);
			byte[] dataSignature = signature.sign();
			return dataSignature;
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeyException e) {
			Log.e(TAG, e.getMessage());
		} catch (SignatureException e) {
			Log.e(TAG, e.getMessage());
		}
		// Should never happen
		return null;
	}

	public static byte[] generateSignature(UnsignedShout shout, Me me)
			throws UnsupportedEncodingException {
		byte[] dataBytes = SerializeUtility.serializeShoutData(shout);
		return generateSignature(dataBytes, me);
	}

}
