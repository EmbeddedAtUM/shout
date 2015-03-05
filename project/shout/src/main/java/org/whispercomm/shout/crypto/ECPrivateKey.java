
package org.whispercomm.shout.crypto;

import org.spongycastle.crypto.params.ECPrivateKeyParameters;

public class ECPrivateKey {

	private final ECPrivateKeyParameters params;

	public ECPrivateKey(ECPrivateKeyParameters params) {
		this.params = params;
	}

	public ECPrivateKeyParameters getECPrivateKeyParameters() {
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
		ECPrivateKey other = (ECPrivateKey) obj;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!ECUtil.equals(params, other.params))
			return false;
		return true;
	}

}
