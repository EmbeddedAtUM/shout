
package org.whispercomm.shout.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1OctetString;
import org.spongycastle.asn1.pkcs.PrivateKeyInfo;
import org.spongycastle.asn1.sec.ECPrivateKeyStructure;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.asn1.x9.X962Parameters;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9ECPoint;
import org.spongycastle.asn1.x9.X9ObjectIdentifiers;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.util.PrivateKeyFactory;
import org.spongycastle.crypto.util.PublicKeyFactory;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;

/**
 * Generator for Shout key pairs
 * 
 * @author David R. Bild
 */
public class KeyGenerator {

	private final ECDomainParameters params;

	private final ECKeyPairGenerator generator;

	public KeyGenerator() {
		this(CryptoParams.DOMAIN_PARAMS);
	}

	private KeyGenerator(ECDomainParameters params) {
		this.params = params;
		this.generator = new ECKeyPairGenerator();
		generator.init(new ECKeyGenerationParameters(
				params,
				new SecureRandom()));
	}

	public ECKeyPair generateKeyPair() {
		AsymmetricCipherKeyPair pair = generator.generateKeyPair();
		return new ECKeyPair(pair.getPublic(), pair.getPrivate());
	}

	public ECPublicKey generatePublic(BigInteger x, BigInteger y) {
		ECCurve curve = params.getCurve();
		ECPoint point = curve.createPoint(x, y, false);
		return new ECPublicKey(new ECPublicKeyParameters(point, params));
	}

	public static ECPublicKey generatePublic(byte[] encoded)
			throws InvalidKeySpecException {
		try {
			return new ECPublicKey((ECPublicKeyParameters) PublicKeyFactory.createKey(encoded));
		} catch (Exception e) {
			/*
			 * Catching Exception may seem too general, but these encodings are
			 * untrusted and we don't want a bad encoding to crash the system.
			 */
			throw new InvalidKeySpecException(e);
		}
	}

	/**
	 * Returns the DER-encoding of the X.509 SubjectPublicKeyInfo representation
	 * of the specified key.
	 * <p>
	 * N.B. This method encodes domain parameters in full; curve names are not
	 * used. The returned encoding should not be used when size is a concern.
	 * 
	 * @param publicKey the key to encode
	 * @return the DER-encoding of the X.509 SubjectPublicKeyInfo representation
	 *         of the key
	 */
	public static byte[] encodePublic(ECPublicKey publicKey) {
		/*
		 * Code based on org.bouncycastle.jce.provider.JCEECPublicKey.java
		 */
		ECPublicKeyParameters ecParams = publicKey.getECPublicKeyParameters();
		ECDomainParameters dParams = ecParams.getParameters();

		X9ECParameters ecP = new X9ECParameters(dParams.getCurve(), dParams.getG(), dParams.getN(),
				dParams.getH(), dParams.getSeed());
		ASN1Encodable params = new X962Parameters(ecP);

		ASN1OctetString p = (ASN1OctetString)
				(new X9ECPoint(ecParams.getQ())).toASN1Object();

		SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(
				X9ObjectIdentifiers.id_ecPublicKey, params), p.getOctets());

		return info.getDEREncoded();
	}

	public static ECPrivateKey generatePrivate(byte[] encoded)
			throws InvalidKeySpecException {
		try {
			return new ECPrivateKey((ECPrivateKeyParameters) PrivateKeyFactory.createKey(encoded));
		} catch (Exception e) {
			/*
			 * Catching Exception may seem too general, but these encodings are
			 * untrusted and we don't want a bad encoding to crash the system.
			 */
			throw new InvalidKeySpecException(e);
		}
	}

	/**
	 * Returns the DER-encoding of the PKCS8 representation of the specified
	 * key.
	 * <p>
	 * N.B. This method encodes domain parameters in full; curve names are not
	 * used. The returned encoding should not be used when size is a concern.
	 * 
	 * @param privateKey the key to encode
	 * @return the DER-encoding of the PKCS8 representation of the key
	 */
	public static byte[] encodePrivate(ECPrivateKey privateKey) {
		/*
		 * Code based on org.bouncycastle.jce.provider.JCEECPrivateKey.java
		 */
		ECPrivateKeyParameters ecParams = privateKey.getECPrivateKeyParameters();
		ECDomainParameters dParams = ecParams.getParameters();

		X9ECParameters ecP = new X9ECParameters(dParams.getCurve(), dParams.getG(), dParams.getN(),
				dParams.getH(), dParams.getSeed());
		ASN1Encodable params = new X962Parameters(ecP);

		ECPrivateKeyStructure keyStructure = new ECPrivateKeyStructure(ecParams.getD(), params);
		PrivateKeyInfo info = new PrivateKeyInfo(new AlgorithmIdentifier(
				X9ObjectIdentifiers.id_ecPublicKey, params.toASN1Object()),
				keyStructure.toASN1Object());

		return info.getDEREncoded();
	}

}
