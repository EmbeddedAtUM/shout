
package org.whispercomm.shout.crypto;

import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

public class ECKeyPair {

	private final ECPublicKey publicKey;

	private final ECPrivateKey privateKey;

	public ECKeyPair(ECPublicKey publicKey, ECPrivateKey privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public ECKeyPair(ECPublicKeyParameters publicParams, ECPrivateKeyParameters privateParams) {
		this(new ECPublicKey(publicParams), new ECPrivateKey(privateParams));
	}

	public ECKeyPair(CipherParameters publicParams, CipherParameters privateParams) {
		this((ECPublicKeyParameters) publicParams, (ECPrivateKeyParameters) privateParams);
	}

	public ECPublicKey getPublicKey() {
		return this.publicKey;
	}

	public ECPrivateKey getPrivateKey() {
		return this.privateKey;
	}

}
