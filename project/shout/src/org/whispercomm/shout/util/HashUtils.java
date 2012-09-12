
package org.whispercomm.shout.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper methods for hashing
 * 
 * @author David R. Bild
 */
public class HashUtils {

	/**
	 * Generates the SHA-256 hash of the specified array of bytes, starting at
	 * the specified offset.
	 * 
	 * @param input the array of bytes
	 * @param offset the offset to start from in the array of bytes
	 * @param len the number of bytes to use, start at {@code offset}.
	 * @returns the array of bytes for the resulting hash value
	 */
	public static byte[] sha256(byte[] input, int offset, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(input, offset, len);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			// Should never happen. Testing should catch such errors.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates the SHA-256 hash of the specified {@code ByteBuffer}. The hash
	 * is computed using {@code input.remaining()} bytes starting at
	 * input.position(). Upon return, the buffer's position will be equal to its
	 * limit; its limit will not have changed.
	 * 
	 * @param input the {@code ByteBuffer}
	 * @returns the array of bytes for the resulting hash value
	 */
	public static byte[] sha256(ByteBuffer input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(input);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			// Should never happen. Testing should catch such errors.
			throw new RuntimeException(e);
		}
	}
}
