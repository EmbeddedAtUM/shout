
package org.whispercomm.shout.id;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;
import org.spongycastle.jce.spec.ECNamedCurveSpec;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.serialization.SerializeUtility;

import android.util.Log;

public class SignatureUtility {
	private static final String TAG = SignatureUtility.class.getSimpleName();

	/*
	 * Use the BouncyCastle provider;
	 */
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * Java does not provide a way to construct an ECPublicKey from a known
	 * point on a named curve. Instead, some classes from Bouncycastle must be
	 * used directly.
	 * <p>
	 * This method converts from the Bouncycastle parameter spec class to the
	 * Java parameter spec class, minimizing the amount of Bouncastle-specific
	 * code used elsewhere.
	 * 
	 * @param spec
	 * @return
	 */
	private static ECParameterSpec convert(ECNamedCurveParameterSpec spec) {
		return new ECNamedCurveSpec(spec.getName(), spec.getCurve(), spec.getG(), spec.getN(),
				spec.getH(), spec.getSeed());
	}

	/**
	 * The security provider to use
	 */
	private static final String CRYPTO_PROVIDER = "SC";

	/**
	 * The name of the elliptic curve used
	 */
	private static final String EC_PARAM_NAME = "secp256r1";

	/**
	 * The EC parameters for the named curved
	 */
	private static final ECParameterSpec EC_PARAMS =
			convert(ECNamedCurveTable.getParameterSpec(EC_PARAM_NAME));

	/**
	 * The crypto algorithm to use
	 */
	private static final String CRYPTO_ALGORITHM = "ECDSA";

	/**
	 * The signing algorithm to use
	 */
	private static final String SIGNING_ALGORITHM = "SHA256withECDSA";

	private SignatureUtility() {
		throw new IllegalStateException("Cannot instantiate SignatureUtility.");
	}

	/*
	 * Key Generation methods
	 */
	/**
	 * Generates a new random {@link KeyPair} from the default curve.
	 * 
	 * @return the new key pair
	 */
	public static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(CRYPTO_ALGORITHM,
					CRYPTO_PROVIDER);
			kpg.initialize(EC_PARAMS);
			return kpg.generateKeyPair();
		} catch (InvalidAlgorithmParameterException e) {
			// Should not happen. Testing will catch invalid parameter specs.
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			// Should not happen. Testing will catch a missing algorithm.
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			// Should not happen. Testing will catch a missing provider.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates an {@link ECPublicKey} for the default curve from the provided
	 * affine coordinates.
	 * 
	 * @param x the affine x coordinate
	 * @param y the affine y coordinate
	 * @return the genereated key
	 * @throws InvalidKeySpecException if and {@code x} and {@code y} do not
	 *             define a valid point on the curve
	 */
	public static ECPublicKey generatePublic(BigInteger x, BigInteger y)
			throws InvalidKeySpecException {
		try {
			ECPoint w = new ECPoint(x, y);
			ECPublicKeySpec spec = new ECPublicKeySpec(w, EC_PARAMS);
			KeyFactory kf = KeyFactory
					.getInstance(CRYPTO_ALGORITHM, CRYPTO_PROVIDER);
			return (ECPublicKey) kf.generatePublic(spec);
		} catch (NoSuchAlgorithmException e) {
			// Should not happen. Testing will catch a missing algorithm
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			// Should not happen. Testing will catch a missing provider
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates an {@link ECPublicKey} from the provided X.509-encoded bytes.
	 * 
	 * @param encoded the X.509 encoding of the public key
	 * @return the public key
	 * @throws InvalidKeySpecException if the bytes are not an X.509 encoding of
	 *             a valid public key
	 */
	public static ECPublicKey generatePublic(byte[] encoded)
			throws InvalidKeySpecException {
		try {
			KeyFactory kf = KeyFactory
					.getInstance(CRYPTO_ALGORITHM, CRYPTO_PROVIDER);
			return (ECPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
		} catch (NoSuchAlgorithmException e) {
			// Should not occur. Testing will catch invalid algorithms.
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			// Should not occur. Testing will catch missing providers.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates an {@link ECPrivateKey} from the provdied PKCS8-encoded bytes.
	 * 
	 * @param encoded the PKCS8 encoding of the private key
	 * @return the private key
	 * @throws InvalidKeySpecException if the bytes are not a PKCS8 encoding of
	 *             a valid private key
	 */
	public static ECPrivateKey getPrivateKeyFromBytes(byte[] encoded)
			throws InvalidKeySpecException {
		try {
			KeyFactory kf = KeyFactory
					.getInstance(CRYPTO_ALGORITHM, CRYPTO_PROVIDER);
			return (ECPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(encoded));
		} catch (NoSuchAlgorithmException e) {
			// Should not occur. Testing will catch invalid algorithms.
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			// Should not occur. Testing will catch missing providers.
			throw new RuntimeException(e);
		}
	}

	public static boolean verifySignature(ByteBuffer data, byte[] signature, ECPublicKey publicKey) {
		try {
			Signature verifier = Signature.getInstance(SIGNING_ALGORITHM, CRYPTO_PROVIDER);
			verifier.initVerify(publicKey);
			verifier.update(data);
			return verifier.verify(signature);
		} catch (SignatureException e) {
			// Can occur if the signature data is not a valid encoded signature.
			// TODO: Store signature numbers in packets, not some ASN.1
			// encoding.
			Log.i(TAG, "SignatureException when verifying shout signature.", e);
			return false;
		} catch (InvalidKeyException e) {
			// Should not occur. Testing will catch key/algorithm mismatches.
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			// Should not occur. Testing will catch missing algorithms.
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			// Should not occur. Testing will catch missing providers.
			throw new RuntimeException(e);
		}
	}

	/*
	 * Signature methods
	 */
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
	@Deprecated
	public static boolean verifySignature(byte[] data, byte[] dataSignature,
			ECPublicKey pubKey) {
		try {
			Signature signature = Signature.getInstance(SIGNING_ALGORITHM,
					CRYPTO_PROVIDER);
			signature.initVerify(pubKey);
			signature.update(data);
			return signature.verify(dataSignature);
		} catch (SignatureException e) {
			// Can occur if the signature data is not a valid encoded signature.
			// TODO: Store signature numbers in packets, not some ASN.1
			// encoding.
			Log.i(TAG, "SignatureException when verifying shout signature.", e);
			return false;
		} catch (NoSuchAlgorithmException e) {
			// Should not happen. Testing will catch missing algorithms.
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			// Should not happen. Testing will catch missing providers.
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			// Should not happen. A non-default-curve key to be passed
			// to this method indicates a bug in the key-deserializing code.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates the signature for the given array of bytes, using the private
	 * key of the given {@code Me}.
	 * 
	 * @param data the data to sign
	 * @param me the Me whose key with which to sign
	 * @return the signature
	 */
	private static byte[] generateSignature(byte[] data, Me me) {
		try {
			ECPrivateKey privateKey = (ECPrivateKey) me.getKeyPair().getPrivate();
			Signature signature = Signature.getInstance(SIGNING_ALGORITHM,
					CRYPTO_PROVIDER);
			signature.initSign(privateKey);
			signature.update(data);
			return signature.sign();
		} catch (NoSuchAlgorithmException e) {
			// Should not happen. Testing will catch missing algorithms.
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			// Should not happen. Testing will catch missing providers.
			throw new RuntimeException(e);
		} catch (SignatureException e) {
			// Should not happen. Testing will catch if the Signature instance
			// is
			// not configured properly.
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			// Should not happen. A non-default-curve key to be passed
			// to this method indicates a bug in the key-deserializing code.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates the signature for the given shout, using the private key of the
	 * given {@code Me}.
	 * 
	 * @param shout the shout to sign
	 * @param me the Me whose key with which to sign
	 * @return the signature
	 */
	public static byte[] generateSignature(UnsignedShout shout, Me me) {
		byte[] dataBytes = SerializeUtility.serializeShoutData(shout);
		return generateSignature(dataBytes, me);
	}

}
