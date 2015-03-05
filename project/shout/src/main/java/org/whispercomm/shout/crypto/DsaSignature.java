
package org.whispercomm.shout.crypto;

import java.math.BigInteger;

public class DsaSignature {

	private final BigInteger r;
	private final BigInteger s;

	public DsaSignature(BigInteger r, BigInteger s) {
		this.r = r;
		this.s = s;
	}

	public BigInteger getR() {
		return r;
	}

	public BigInteger getS() {
		return s;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((r == null) ? 0 : r.hashCode());
		result = prime * result + ((s == null) ? 0 : s.hashCode());
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
		DsaSignature other = (DsaSignature) obj;
		if (r == null) {
			if (other.r != null)
				return false;
		} else if (!r.equals(other.r))
			return false;
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (!s.equals(other.s))
			return false;
		return true;
	}

	/**
	 * Encodes the signature according to a simple format:
	 * 
	 * <pre>
	 * <code>
	 * byte 0             - length of r in bytes, |r|
	 * byte 1 ... 1+|r|   - r
	 * byte 2+|r| ... end - s
	 * </code>
	 * </pre>
	 */
	public static byte[] encode(DsaSignature sig) {
		byte[] r = sig.getR().toByteArray();
		byte[] s = sig.getS().toByteArray();
		byte[] encoded = new byte[1 + r.length + s.length];

		assert r.length <= 255;
		encoded[0] = (byte) r.length;
		System.arraycopy(r, 0, encoded, 1, r.length);
		System.arraycopy(s, 0, encoded, 1 + r.length, s.length);

		return encoded;
	}

	/**
	 * Decodes a signature from the encoded form created by
	 * {@link #encode(DsaSignature)}.
	 * 
	 * @param encoded the encoded form
	 * @return the signature
	 * @throws IllegalArgumentException if the provided bytes are not a valid
	 *             encoding
	 */
	public static DsaSignature decode(byte[] encoded) throws IllegalArgumentException {
		int len = 0xFF & encoded[0];
		byte[] r = new byte[len];
		byte[] s = new byte[encoded.length - 1 - len];
		System.arraycopy(encoded, 1, r, 0, r.length);
		System.arraycopy(encoded, 1 + r.length, s, 0, s.length);
		return new DsaSignature(new BigInteger(r), new BigInteger(s));
	}
}
