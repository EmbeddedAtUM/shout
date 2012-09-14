
package org.whispercomm.shout.crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.signers.ECDSASigner;

public class EcdsaWithSha256 {

	private final ECDSASigner signer;

	private final SHA256Digest digest;

	public static DsaSignature sign(ByteBuffer buffer, ECPrivateKey privateKey) {
		if (buffer.hasArray()) {
			return sign(buffer.array(), buffer.arrayOffset() + buffer.position(),
					buffer.remaining(), privateKey);
		} else {
			byte[] msg = new byte[buffer.remaining()];
			buffer.get(msg);
			return sign(msg, privateKey);
		}
	}

	public static DsaSignature sign(byte[] data, ECPrivateKey privateKey) {
		return sign(data, 0, data.length, privateKey);
	}

	public static DsaSignature sign(byte[] data, int offset, int len, ECPrivateKey privateKey) {
		EcdsaWithSha256 signer = new EcdsaWithSha256();
		signer.initSign(privateKey);
		signer.update(data, offset, len);
		return signer.sign();
	}

	public static boolean verify(DsaSignature sig, ByteBuffer buffer, ECPublicKey publicKey) {
		if (buffer.hasArray()) {
			return verify(sig, buffer.array(), buffer.arrayOffset() + buffer.position(),
					buffer.remaining(), publicKey);
		} else {
			byte[] msg = new byte[buffer.remaining()];
			buffer.get(msg);
			return verify(sig, msg, publicKey);
		}
	}

	public static boolean verify(DsaSignature sig, byte[] data, ECPublicKey publicKey) {
		return verify(sig, data, 0, data.length, publicKey);
	}

	public static boolean verify(DsaSignature sig, byte[] data, int offset, int len,
			ECPublicKey publicKey) {
		EcdsaWithSha256 signer = new EcdsaWithSha256();
		signer.initVerify(publicKey);
		signer.update(data, offset, len);
		return signer.verify(sig);
	}

	public EcdsaWithSha256() {
		signer = new ECDSASigner();
		digest = new SHA256Digest();
	}

	public void initVerify(ECPublicKey publicKey) {
		signer.init(false, publicKey.getECPublicKeyParameters());
		digest.reset();
	}

	public void initSign(ECPrivateKey privateKey) {
		signer.init(true, privateKey.getECPrivateKeyParameters());
		digest.reset();
	}

	public void update(byte b) {
		digest.update(b);
	}

	public void update(byte[] data) {
		digest.update(data, 0, data.length);
	}

	public void update(byte[] data, int offset, int len) {
		digest.update(data, offset, len);
	}

	public void update(ByteBuffer buffer) {
		if (buffer.hasArray()) {
			digest.update(buffer.array(), buffer.arrayOffset() + buffer.position(),
					buffer.remaining());
		} else {
			byte[] data = new byte[buffer.remaining()];
			buffer.get(data);
			digest.update(data, 0, data.length);
		}
	}

	public DsaSignature sign() {
		byte[] hash = new byte[digest.getDigestSize()];
		digest.doFinal(hash, 0);
		BigInteger[] sig = signer.generateSignature(hash);
		return new DsaSignature(sig[0], sig[1]);
	}

	public boolean verify(DsaSignature sig) {
		byte[] hash = new byte[digest.getDigestSize()];
		digest.doFinal(hash, 0);
		return signer.verifySignature(hash, sig.getR(), sig.getS());
	}
}
