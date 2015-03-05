
package org.whispercomm.shout.crypto;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;

public class EcdsaWithSha256Test {

	private EcdsaWithSha256 signer;

	@Before
	public void setup() {
		signer = new EcdsaWithSha256();
	}

	@Test
	public void testVerifying() {
		byte[] message = "This is only a test message. It is 48 bytes long".getBytes();

		ECDomainParameters domain = CryptoParams.DOMAIN_PARAMS;
		BigInteger x = new BigInteger(
				Hex.decode("008101ece47464a6ead70cf69a6e2bd3d88691a3262d22cba4f7635eaff26680a8"));
		BigInteger y = new BigInteger(
				Hex.decode("00d8a12ba61d599235f67d9cb4d58f1783d3ca43e78f0a5abaa624079936c0c3a9"));
		ECPoint point = domain.getCurve().createPoint(x, y, false);
		ECPublicKey key = new ECPublicKey(new ECPublicKeyParameters(point,
				domain));

		BigInteger r = new BigInteger(1,
				Hex.decode("7214bc9647160bbd39ff2f80533f5dc6ddd70ddf86bb815661e805d5d4e6f27c"));
		BigInteger s = new BigInteger(1,
				Hex.decode("7d1ff961980f961bdaa3233b6209f4013317d3e3f9e1493592dbeaa1af2bc367"));
		DsaSignature sig = new DsaSignature(r, s);

		signer.initVerify(key);
		signer.update(message);
		assertTrue(signer.verify(sig));
	}

	@Test
	public void testSigning() {
		byte[] message = "This is only a test message. It is 48 bytes long".getBytes();

		ECDomainParameters domain = CryptoParams.DOMAIN_PARAMS;
		BigInteger d = new BigInteger(1,
				Hex.decode("70a12c2db16845ed56ff68cfc21a472b3f04d7d6851bf6349f2d7d5b3452b38a"));
		ECPrivateKey privateKey = new ECPrivateKey(new ECPrivateKeyParameters(d, domain));

		signer.initSign(privateKey);
		signer.update(message);
		DsaSignature sig = signer.sign();

		assertThat(sig, is(not(nullValue())));

		/*
		 * Signature includes a random component, so we can't verify the result
		 * directly. Instead, check that the signature verifies. verify() is
		 * determinstically tested.
		 */
		BigInteger x = new BigInteger(
				Hex.decode("008101ece47464a6ead70cf69a6e2bd3d88691a3262d22cba4f7635eaff26680a8"));
		BigInteger y = new BigInteger(
				Hex.decode("00d8a12ba61d599235f67d9cb4d58f1783d3ca43e78f0a5abaa624079936c0c3a9"));
		ECPoint point = domain.getCurve().createPoint(x, y, false);
		ECPublicKey publicKey = new ECPublicKey(new ECPublicKeyParameters(point,
				domain));

		assertTrue(EcdsaWithSha256.verify(sig, message, publicKey));
	}
}
