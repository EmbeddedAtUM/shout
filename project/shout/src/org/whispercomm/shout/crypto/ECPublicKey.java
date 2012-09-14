
package org.whispercomm.shout.crypto;

import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

public class ECPublicKey {

	private final ECPublicKeyParameters params;

	public ECPublicKey(ECPublicKeyParameters params) {
		this.params = params;
	}

	public ECPublicKeyParameters getECPublicKeyParameters() {
		return params;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((params == null) ? 0 : ECUtil.hashCode(params));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ECPublicKey other = (ECPublicKey) obj;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!ECUtil.equals(params, other.params))
			return false;
		return true;
	}

	@Override
	public String toString() {
		ECDomainParameters dParams = params.getParameters();
		return String.format("DParams[%s]", dParams);
	}
}
