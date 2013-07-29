
package org.whispercomm.shout;

import java.util.Arrays;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.util.encoders.Hex;
import org.whispercomm.shout.errors.InvalidEncodingException;
import org.whispercomm.shout.util.Encoders;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Simple class that encapsulates a byte[] representation of a SHA-256 hash,
 * providing type safety and an {@code equals()} implementation.
 * <p>
 * The {@link #equals(Object)} method is not constant time, so this class should
 * not be used for secret hashes (e.g., MACs).
 * 
 * @author David R. Bild
 */
public final class Hash implements Parcelable {

	/**
	 * Hash with a value of zero (all bits zero).
	 */
	public static final Hash ZERO = new Hash(new byte[32]);

	/**
	 * Length of byte array representation of hash.
	 */
	public static final int LENGTH = 32;

	public static Hash hashData(byte[] data) {
		return hashData(data, 0, data.length);
	}

	public static Hash hashData(byte[] data, int offset, int len) {
		SHA256Digest digest = new SHA256Digest();
		digest.update(data, offset, len);

		byte[] hash = new byte[32];
		digest.doFinal(hash, 0);

		return new Hash(hash);
	}

	private final byte[] hash;

	public Hash(byte[] hash) throws InvalidEncodingException {
		if (hash.length != 32)
			throw new InvalidEncodingException("Hash must be 32 bytes long.");

		this.hash = hash;
	}

	public Hash(String hash) throws InvalidEncodingException {
		this(fromString(hash));
	}

	private static byte[] fromString(String hash) {
		try {
			return Hex.decode(hash);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new InvalidEncodingException(
					"Hash encoding may only contain hexadecimal characters.", e);
		}
	}

	public byte[] toByteArray() {
		return this.hash;
	}

	@Override
	public String toString() {
		return Encoders.toHexString(hash);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(hash);
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
		Hash other = (Hash) obj;
		if (!Arrays.equals(hash, other.hash))
			return false;
		return true;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByteArray(hash);
	}

	public static final Parcelable.Creator<Hash> CREATOR = new Parcelable.Creator<Hash>() {

		public Hash createFromParcel(Parcel in) {
			return new Hash(in);
		}

		public Hash[] newArray(int size) {
			return new Hash[size];
		}
	};

	private Hash(Parcel in) {
		this(in.createByteArray());
	}
}
