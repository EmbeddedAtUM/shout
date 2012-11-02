
package org.whispercomm.shout;

/**
 * Simple implementation of {@link HashReference}. Subclasses may use the
 * {@link #set(Object)} method to set the referent after object construction.
 * This class correctly implements {@link #hashCode()} and
 * {@link #equals(Object)}.
 * 
 * @author David R. Bild
 * @param <T>
 */
public class SimpleHashReference<T> implements HashReference<T> {

	private final Hash hash;

	private T referent;

	public SimpleHashReference(Hash hash) {
		this.hash = hash;
		this.referent = null;
	}

	public SimpleHashReference(Hash hash, T referent) {
		this(hash);
		this.referent = referent;
	}

	@Override
	public Hash getHash() {
		return hash;
	}

	@Override
	public boolean isAvailable() {
		return (referent != null);
	}

	@Override
	public T get() {
		return referent;
	}

	protected void set(T referent) {
		this.referent = referent;
	}

	@Override
	public int hashCode() {
		return hash.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof HashReference))
			return false;
		HashReference<?> other = (HashReference<?>) obj;
		if (hash == null) {
			if (other.getHash() != null)
				return false;
		} else if (!hash.equals(other.getHash()))
			return false;
		return true;
	}

}
