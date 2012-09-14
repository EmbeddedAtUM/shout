
package org.whispercomm.shout.crypto;

import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.params.ECDomainParameters;

public class CryptoParams {

	public static final String CURVE_NAME = "secp256r1";

	protected static final ECDomainParameters DOMAIN_PARAMS = fromName(CURVE_NAME);

	private static ECDomainParameters fromName(String curveName) {
		X9ECParameters ecP = SECNamedCurves.getByName(curveName);
		return new ECDomainParameters(ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH());
	}

	private CryptoParams() {
		throw new IllegalStateException("CryptoParams should not be instantiated");
	}
}
