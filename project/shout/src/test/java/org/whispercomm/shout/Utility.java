package org.whispercomm.shout;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.ECGenParameterSpec;

import org.whispercomm.shout.id.SignatureUtility;

public class Utility {
	// generate key pairs
	public static KeyPair genKeyPair() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidAlgorithmParameterException {
		ECGenParameterSpec ecParamSpec = new ECGenParameterSpec(
				SignatureUtility.ECC_PARAMS);
		KeyPairGenerator kpg = KeyPairGenerator.getInstance(
				SignatureUtility.CRYPTO_ALGO, SignatureUtility.CRYPTO_PROVIDER);
		kpg.initialize(ecParamSpec);

		KeyPair kpA = kpg.generateKeyPair();
		return kpA;
	}
}
