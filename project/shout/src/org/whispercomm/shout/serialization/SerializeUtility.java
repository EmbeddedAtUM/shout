
package org.whispercomm.shout.serialization;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.util.Arrays;

import android.util.Log;

public class SerializeUtility {

	private static final String TAG = SerializeUtility.class.getSimpleName();
	private static final int MAX_SHOUT_LENGTH = 415;
	public static final int MAX_USERNAME_LENGTH = 40;
	public static final int MAX_MESSAGE_LENGTH = 240;
	private static final int TIMESTAMP_LENGTH = 8;
	public static final int PUBLIC_KEY_LENGTH = 91;
	private static final int SIGNED_PARENT_HASH_LENGTH = 32;

	public static final String HASH_ALGORITHM = "SHA-256";

	/**
	 * Serialize the Shout data (not signature).
	 * 
	 * @param shout
	 * @return A serialized version of this Shout
	 */
	public static byte[] serializeShoutData(UnsignedShout shout) {
		ByteBuffer buffer = ByteBuffer.allocate(MAX_SHOUT_LENGTH);
		int size = 0;
		try {
			// Put in the 8-byte timestamp
			long time = shout.getTimestamp().getMillis();
			buffer.putLong(time);
			size += TIMESTAMP_LENGTH;

			// Get the username as UTF-8 encoded bytes
			byte[] username = shout.getSender().getUsername().getBytes(Shout.CHARSET_NAME);

			// Get the length of the username
			byte usernameLength = (byte) (username.length & 0x000F);

			// Put the username length in the buffer
			buffer.put(usernameLength);
			size += 1;

			// Put the username in the buffer
			buffer.put(username);
			size += username.length;

			// Put in the sender public key
			byte[] keyBytes = shout.getSender().getPublicKey().getEncoded();
			buffer.put(keyBytes);
			size += PUBLIC_KEY_LENGTH;

			// Serialize the message
			String message = shout.getMessage();
			if (message != null) {
				// Get the message as UTF-8 encoded bytes
				byte[] messageBytes = message.getBytes(Shout.CHARSET_NAME);
				int messageLength = messageBytes.length;
				// Hack to get length as unsigned two bytes
				char twoByteLength = (char) messageLength;
				buffer.putChar(twoByteLength);
				buffer.put(messageBytes);
				size += 2 + messageLength;
			} else {
				// No message, put in 0x0000 as length
				char zero = '\u0000';
				buffer.putChar(zero);
				size += 2;
			}
			// Handle the parent
			Shout parent = shout.getParent();
			if (parent != null) {
				// has_parent = 0x1;
				buffer.put((byte) 0x0001);
				// Put in the parent signature hash
				byte[] parentHash = parent.getHash();
				buffer.put(parentHash);
				size += 1 + SIGNED_PARENT_HASH_LENGTH;
			} else {
				// has_parent = 0x0
				buffer.put((byte) 0x0000);
				size += 1;
			}
			return Arrays.copyOfRange(buffer.array(), 0, size);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		}
		// Should never happen
		return null;
	}

	/**
	 * Generate a hash over the serialized version of the data in this Shout.
	 * 
	 * @param shout
	 * @return A hash that is not over the signature
	 */
	public static byte[] generateHash(Shout shout) {
		byte[] dataBytes = serializeShoutData(shout);
		byte[] signatureBytes = shout.getSignature();
		return generateHash(dataBytes, signatureBytes);
	}

	/**
	 * Generate a hash over the serialized data bytes.
	 * 
	 * @param shoutData The serialized data bytes.
	 * @return A hash that is not over the signature
	 */
	public static byte[] generateHash(byte[] data, byte[] signature) {
		byte lengthByte = (byte) (signature.length & 0x000F);
		ByteBuffer buffer = ByteBuffer.allocate(data.length + 1 + signature.length);
		buffer.put(data);
		buffer.put(lengthByte);
		buffer.put(signature);
		try {
			MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
			md.update(buffer.array());
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		}
		// Should never happen
		return null;
	}

}
