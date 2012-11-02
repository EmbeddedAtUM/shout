
package org.whispercomm.shout;

/**
 * Interface for hash-based references to objects.
 * <p>
 * Many objects in Shout are referenced from other objects by hash. For example,
 * shouts reference avatars by hash and parent shouts by hash. In some cases,
 * the referenced object may not be available. For example, avatars are shared
 * asynchronously and without any guarantee. Consequently, the Java classes
 * representing such objects cannot directly contain the referenced object.
 * <p>
 * {@code HashReference} addresses this problem. An object with a hash-based
 * reference to another should use a HashReference implementation to provide
 * safe access to both the hash and, when available, the referent.
 * <p>
 * Implementations should override {@link #hashCode()} and
 * {@link #equals(Object)} such that for two HashReference objects {@code A} and
 * {@code B}
 * <ul>
 * <li> {@code A.hashCode() == A.getHash().hashCode()}, and</li>
 * <li> {@code A.equals(B)} returns {@code true} if and only if
 * {@code A.getHash().equals(B.getHash())} returns {@code true}.</li>
 * </ul>
 * 
 * @author David R. Bild
 * @param <T> the type of the object referenced by the hash
 */
public interface HashReference<T> {

	/**
	 * Returns the hash reference
	 * 
	 * @return the hash
	 */
	public Hash getHash();

	/**
	 * Tells whether the referenced object is available and can be retrieved by
	 * the {@link #get()} method.
	 * 
	 * @return {@code true} if the referent is available and can be retrieved
	 */
	public boolean isAvailable();

	/**
	 * Returns the referenced object, if available.
	 * 
	 * @return the references object or {@code null} if it is not available
	 */
	public T get();

}
