
package org.whispercomm.shout.serialization;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;

import org.joda.time.DateTime;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.util.Arrays;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.util.Log;

/**
 * Static utility class for serializing and deserializing Shouts, and generating
 * Shout hashes.
 * 
 * @author David Adrian
 */
public class SerializeUtility {
	private static final String TAG = SerializeUtility.class.getSimpleName();
	public static final int SINGLE_SHOUT_FLAG = 1;
	public static final int TIMESTAMP_SIZE = 8;
	public static final int USERNAME_LENGTH_SIZE = 1;
	public static final int MAX_USERNAME_SIZE = 40;
	public static final int PUBLIC_KEY_SIZE = 91;
	public static final int MESSAGE_LENGTH_SIZE = 1;
	public static final int MAX_MESSAGE_SIZE = 240;
	public static final int HASH_SIZE = 256 / 8;

	public static final int MAX_SHOUT_SIZE = SINGLE_SHOUT_FLAG + TIMESTAMP_SIZE
			+ USERNAME_LENGTH_SIZE
			+ MAX_USERNAME_SIZE + PUBLIC_KEY_SIZE + MESSAGE_LENGTH_SIZE
			+ MAX_MESSAGE_SIZE + HASH_SIZE;

	public static final int SIGNATURE_LENGTH_SIZE = 1;
	public static final int MAX_SIGNATURE_DATA_SIZE = 80;
	public static final int MAX_SIGNATURE_SIZE = SIGNATURE_LENGTH_SIZE + MAX_SIGNATURE_DATA_SIZE;

	public static final String HASH_ALGORITHM = "SHA-256";

	private static final int MASK = 0x00FF;
	private static final int HAS_PARENT_BITS = 1;

	/**
	 * Serialize the Shout data (not signature).
	 * 
	 * @param shout The Shout to serialize
	 * @return A serialized version of this Shout
	 */
	public static byte[] serializeShoutData(UnsignedShout shout) {
		ByteBuffer buffer = ByteBuffer.allocate(MAX_SHOUT_SIZE);
		int size = 0;
		try {
			// Check if parent
			Shout parent = shout.getParent();
			if (parent != null) {
				// Use the LSB as a has parent flag
				buffer.put((byte) 0x01);
			} else {
				buffer.put((byte) 0x00);
			}
			size += SINGLE_SHOUT_FLAG;
			// Put in the 8-byte timestamp
			long time = shout.getTimestamp().getMillis();
			buffer.putLong(time);
			size += TIMESTAMP_SIZE;

			// Get the username as UTF-8 encoded bytes
			byte[] username = shout.getSender().getUsername().getBytes(Shout.CHARSET_NAME);

			// Get the length of the username
			byte usernameLength = (byte) (username.length);

			// Put the username length in the buffer
			buffer.put(usernameLength);
			size += 1;

			// Put the username in the buffer
			buffer.put(username);
			size += username.length;

			// Put in the sender public key
			byte[] keyBytes = shout.getSender().getPublicKey().getEncoded();
			buffer.put(keyBytes);
			size += PUBLIC_KEY_SIZE;

			// Serialize the message
			String message = shout.getMessage();
			if (message != null) {
				// Get the message as UTF-8 encoded bytes
				byte[] messageBytes = message.getBytes(Shout.CHARSET_NAME);
				int messageLength = messageBytes.length;
				// Hack to get length as unsigned two bytes
				byte lengthByte = (byte) (messageLength);
				buffer.put(lengthByte);
				size += MESSAGE_LENGTH_SIZE;
				buffer.put(messageBytes);
				size += messageLength;
			} else {
				// No message, put in 0x0000 as length
				byte zero = 0;
				buffer.put(zero);
				size += MESSAGE_LENGTH_SIZE;
			}
			// Handle the parent
			if (parent != null) {
				// Put in the parent signature hash
				byte[] parentHash = parent.getHash();
				buffer.put(parentHash);
				size += HASH_SIZE;
			}
			return Arrays.copyOfRange(buffer.array(), 0, size);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		}
		// Should never happen
		return null;
	}

	/**
	 * Deserialize the body of a Shout packet. Most likely you want to pass this
	 * function the result of {@link ShoutPacket#getBodyBytes()}.
	 * 
	 * @param count How long the Shout chain is.
	 * @param body The serialized Shout chain.
	 * @return The Java object representation of this serialized Shout chain.
	 * @throws BadShoutVersionException 
	 */
	public static Shout deserializeShout(int count, byte[] body) throws BadShoutVersionException {
		/*
		 * TODO Make everything about this function not be awful
		 */
		boolean hasNext = count > 0;
		ByteBuffer buffer = ByteBuffer.wrap(body);
		BuildableShout shout = new BuildableShout();
		BuildableShout child = shout;
		int start = 0;
		while (hasNext) {
			int size = 0;
			byte versionFlag = buffer.get();
			size += SINGLE_SHOUT_FLAG;
			// Handle version
			int version = (int) (versionFlag >>> HAS_PARENT_BITS);
			if (version != 0) {
				throw new BadShoutVersionException();
			}
			// Get the has_parent flag
			int parentFlag = versionFlag & 0x01;
			// Get the timestamp
			long time = buffer.getLong();
			size += TIMESTAMP_SIZE;
			byte nameLengthByte = buffer.get();
			size += USERNAME_LENGTH_SIZE;
			int nameLength = (((int) nameLengthByte) & MASK);
			byte[] nameBytes = new byte[nameLength];
			buffer.get(nameBytes);
			size += nameLength;
			byte[] publicKeyBytes = new byte[PUBLIC_KEY_SIZE];
			buffer.get(publicKeyBytes);
			size += PUBLIC_KEY_SIZE;
			String message = null;
			byte messageLengthByte = buffer.get();
			size += MESSAGE_LENGTH_SIZE;
			int messageLength = (((int) messageLengthByte) & MASK);
			if (messageLength > 0) {
				byte[] messageBytes = new byte[messageLength];
				buffer.get(messageBytes);
				size += messageLength;
				try {
					message = new String(messageBytes, Shout.CHARSET_NAME);
				} catch (UnsupportedEncodingException e) {
					// Should never happen
					Log.e(TAG, e.getMessage());
					return null;
				}
			}
			byte[] parentHash;
			if (parentFlag == 1) {
				parentHash = new byte[HASH_SIZE];
				buffer.get(parentHash);
				size += HASH_SIZE;
				hasNext = true;
			} else {
				hasNext = false;
			}
			byte[] shoutData = Arrays.copyOfRange(buffer.array(), start, start + size);
			byte signatureLengthByte = buffer.get();
			size += SIGNATURE_LENGTH_SIZE;
			int signatureLength = (((int) signatureLengthByte) & MASK);
			byte[] signatureBytes = new byte[signatureLength];
			buffer.get(signatureBytes);
			size += signatureLength;
			start += size;
			BuildableUser user = new BuildableUser();
			try {
				user.username = new String(nameBytes, Shout.CHARSET_NAME);
				user.publicKey = SignatureUtility.getPublicKeyFromBytes(publicKeyBytes);
			} catch (UnsupportedEncodingException e) {
				// Should never happen
				Log.e(TAG, e.getMessage());
				return null;
			}
			shout.timestamp = new DateTime(time);
			shout.user = user;
			shout.message = message;
			shout.signature = signatureBytes;
			shout.hash = SerializeUtility.generateHash(shoutData, signatureBytes);
			if (hasNext) {
				shout.parent = new BuildableShout();
				shout = shout.parent;
			}
		}
		return child;
	}

	/**
	 * Convenience method to serialize this Shout, and then use the serialized
	 * data bytes and signature to generate the hash, using SHA-256.
	 * 
	 * @param shout The shout to hash
	 * @return A unique hash over the Shout and its signature
	 */
	public static byte[] generateHash(Shout shout) {
		byte[] dataBytes = serializeShoutData(shout);
		byte[] signatureBytes = shout.getSignature();
		return generateHash(dataBytes, signatureBytes);
	}

	/**
	 * Generate a hash over the serialized data bytes and the signature bytes
	 * using SHA-256. The hash is over the concatenation of
	 * {@code data + ((byte) signature.length) + signature}.
	 * 
	 * @param data The serialized data bytes.
	 * @param signature The serialized signature bytes
	 * @return A unique hash over data and signature.
	 */
	public static byte[] generateHash(byte[] data, byte[] signature) {
		byte lengthByte = (byte) (signature.length);
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

	private static class BuildableUser implements User {

		String username;
		ECPublicKey publicKey;

		@Override
		public String getUsername() {
			return username;
		}

		@Override
		public ECPublicKey getPublicKey() {
			return publicKey;
		}

	}

	private static class BuildableShout implements Shout {

		User user = null;
		String message = null;
		DateTime timestamp = null;
		BuildableShout parent = null;
		byte[] signature = null;
		byte[] hash = null;

		@Override
		public User getSender() {
			return user;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public DateTime getTimestamp() {
			return timestamp;
		}

		@Override
		public Shout getParent() {
			return parent;
		}

		@Override
		public ShoutType getType() {
			return ShoutMessageUtility.getShoutType(this);
		}

		@Override
		public byte[] getSignature() {
			return signature;
		}

		@Override
		public byte[] getHash() {
			return hash;
		}

	}

}
