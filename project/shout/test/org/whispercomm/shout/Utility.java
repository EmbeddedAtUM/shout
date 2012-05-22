package org.whispercomm.shout;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

import org.whispercomm.shout.id.SignatureUtility;

import static org.junit.Assert.*;

public class Utility {
	// generate key pairs
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
	
	public static byte[] genByteArray(int size) {
		byte[] arr = new byte[size];
		SecureRandom rand = new SecureRandom();
		rand.nextBytes(arr);
		return arr;
	}
}
