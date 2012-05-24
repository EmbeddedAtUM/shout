
package org.whispercomm.shout.test.util;

import static org.junit.Assert.fail;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.whispercomm.shout.id.SignatureUtility;

/**
 * Factory class to be used when generating objects for testing
 * 
 * @author David Adrian
 * @author Yue Liu
 */
public class TestFactory {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	/**
	 * Generate a valid EC key pair
	 */
	public static KeyPair genKeyPair() {
		ECGenParameterSpec ecParamSpec = new ECGenParameterSpec(
				SignatureUtility.ECC_PARAMS);
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance(
					SignatureUtility.CRYPTO_ALGO, SignatureUtility.CRYPTO_PROVIDER);
			kpg.initialize(ecParamSpec);
			KeyPair kpA = kpg.generateKeyPair();
			return kpA;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		fail("Test writer is a failure at generating Key Pairs");
		return null;
	}

	/**
	 * Generate an ECPublicKey by generating a valid key pair, then dropping the
	 * private key and returning the public key
	 * 
	 * @return Valid ECPublicKey
	 */
	public static ECPublicKey genPublicKey() {
		KeyPair keyPair = genKeyPair();
		ECPublicKey pubKey = (ECPublicKey) keyPair.getPublic();
		return pubKey;
	}

	/**
	 * Generate a unique, random byte array of a given size
	 * 
	 * @param size
	 */
	public static byte[] genByteArray(int size) {
		byte[] arr = new byte[size];
		SecureRandom rand = new SecureRandom();
		rand.nextBytes(arr);
		return arr;
	}
	
	public static int[] genArrayWithSingleValue(int size, int value) {
		int[] arr = new int[size];
		for (int i = 0; i < size; i++) {
			arr[i] = value;
		}
		return arr;
	}
}
