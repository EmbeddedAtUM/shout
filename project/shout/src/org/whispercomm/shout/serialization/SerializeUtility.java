
package org.whispercomm.shout.serialization;

import static org.whispercomm.shout.util.ByteBufferUtils.flipToMark;
import static org.whispercomm.shout.util.ByteBufferUtils.getArray;
import static org.whispercomm.shout.util.ByteBufferUtils.getBigInteger;
import static org.whispercomm.shout.util.ByteBufferUtils.getVArray;
import static org.whispercomm.shout.util.ByteBufferUtils.putBigInteger;
import static org.whispercomm.shout.util.ByteBufferUtils.putVArray;
import static org.whispercomm.shout.util.CharUtils.decodeUtf8Safe;
import static org.whispercomm.shout.util.CharUtils.encodeUtf8;

import java.math.BigInteger;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;

import org.joda.time.DateTime;
import org.spongycastle.math.ec.ECPoint;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.DsaSignature;
import org.whispercomm.shout.crypto.ECPublicKey;
import org.whispercomm.shout.crypto.EcdsaWithSha256;
import org.whispercomm.shout.crypto.KeyGenerator;
import org.whispercomm.shout.network.UnsupportedVersionException;
import org.whispercomm.shout.network.shout.InvalidShoutSignatureException;
import org.whispercomm.shout.util.Arrays;
import org.whispercomm.shout.util.ByteBufferUtils.InvalidLengthException;
import org.whispercomm.shout.util.ByteBufferUtils.LengthType;
import org.whispercomm.shout.util.HashUtils;
import org.whispercomm.shout.util.ShoutMessageUtility;

/**
 * Static utility class for serializing and deserializing Shouts, and generating
 * Shout hashes.
 * 
 * @author David Adrian
 */
public class SerializeUtility {
	@SuppressWarnings("unused")
	private static final String TAG = SerializeUtility.class.getSimpleName();

	public static final String HASH_ALGORITHM = "SHA-256";
	public static final int HASH_SIZE = 256 / 8;

	// Header fields sizes
	public static final int SHOUT_FLAG_SIZE = 1;
	public static final int TIMESTAMP_SIZE = 8;

	// User fields sizes
	public static final int PUBLIC_KEY_AFFINE_SIZE = 256 / 8;
	public static final int PUBLIC_KEY_SIZE = 2 * PUBLIC_KEY_AFFINE_SIZE;
	public static final int AVATAR_HASH_SIZE = HASH_SIZE;
	public static final int USERNAME_LENGTH_SIZE = 1;
	public static final int USERNAME_SIZE_MAX = 40;

	// Message field sizes
	public static final int MESSAGE_LENGTH_SIZE = 1;
	public static final int MESSAGE_SIZE_MAX = 240;
	public static final int LONGITUDE_SIZE = 8;
	public static final int LATITUDE_SIZE = 8;

	// Parent fields sizes
	public static final int PARENT_HASH_SIZE = HASH_SIZE;

	// Signature fields sizes
	public static final int SIGNATURE_R_SIZE = 256 / 8;
	public static final int SIGNATURE_S_SIZE = 256 / 8;

	// Max size computations
	public static final int SHOUT_HEADER_FIELDS_SIZE = SHOUT_FLAG_SIZE + TIMESTAMP_SIZE;
	public static final int SHOUT_USER_FIELDS_SIZE_MAX = PUBLIC_KEY_SIZE + AVATAR_HASH_SIZE
			+ USERNAME_LENGTH_SIZE + USERNAME_SIZE_MAX;
	public static final int SHOUT_MESSAGE_FIELDS_SIZE_MAX = MESSAGE_LENGTH_SIZE + MESSAGE_SIZE_MAX
			+ LONGITUDE_SIZE + LATITUDE_SIZE;
	public static final int SHOUT_PARENT_FIELDS_SIZE = PARENT_HASH_SIZE;
	public static final int SHOUT_SIGNATURE_FIELDS_SIZE_MAX = SIGNATURE_R_SIZE + SIGNATURE_S_SIZE;

	public static final int SHOUT_UNSIGNED_SIZE_MAX = SHOUT_HEADER_FIELDS_SIZE
			+ SHOUT_USER_FIELDS_SIZE_MAX + SHOUT_MESSAGE_FIELDS_SIZE_MAX + SHOUT_PARENT_FIELDS_SIZE;

	public static final int SHOUT_SIGNED_SIZE_MAX = SHOUT_UNSIGNED_SIZE_MAX
			+ SHOUT_SIGNATURE_FIELDS_SIZE_MAX;

	// A recomment cannot have its own message
	public static final int SHOUT_CHAIN_MAX = 3 * SHOUT_SIGNED_SIZE_MAX - MESSAGE_SIZE_MAX;

	private static final int VERSION_MASK = 0x0F;
	private static final int LOCATION_BIT_MASK = 1 << 4;
	private static final int PARENT_BIT_MASK = 1 << 5;

	/**
	 * Version to use when serializing a shout
	 */
	private static final int VERSION = 0;

	/**
	 * KeyGenerator used to recover keys from X and Y coordinates
	 */
	private static final KeyGenerator KEY_GENERATOR = new KeyGenerator();

	/**
	 * Extracts the version from the flags byte
	 * 
	 * @param flags the flag byte
	 * @return the version number
	 */
	private static final int VERSION(byte flags) {
		return flags & VERSION_MASK;
	}

	/**
	 * Checks if the "has_location" bit is set in the flags byte.
	 * 
	 * @param flags the flag byte
	 * @return {@code true} if the "has_location" bit is set or {@code false}
	 *         otherwise.
	 */
	private static final boolean HAS_LOCATION(byte flags) {
		return (flags & LOCATION_BIT_MASK) != 0;
	}

	/**
	 * Checks if the "has_parent" bit is set in the flags byte.
	 * 
	 * @param flags the flag byte
	 * @return {@code true} if the "has_parent" bit is set or {@code false}
	 *         otherwise.
	 */
	private static final boolean HAS_PARENT(byte flags) {
		return (flags & PARENT_BIT_MASK) != 0;
	}

	/**
	 * Serializes the portion of the shout data over which the signature is
	 * computed. The buffer position is increased by the size of the shout data.
	 * If the buffer is too small, the position is increased by the amount of
	 * data that was added and a {@link BufferOverflowException} is thrown.
	 * 
	 * @param buffer the buffer into which to serialize the shout data
	 * @param shout the shout to serialize
	 * @return the provided buffer
	 * @throws BufferOverflowException if {@code buffer} does not have room for
	 *             the shout data
	 */
	public static ByteBuffer serializeShoutData(ByteBuffer buffer, UnsignedShout shout)
			throws BufferOverflowException {
		// Compute flags byte
		byte flags = VERSION;

		Shout parent = shout.getParent();
		if (parent != null) {
			flags |= PARENT_BIT_MASK;
		}

		boolean hasLocation = false; // TODO: check Shout object for location
		if (hasLocation) {
			flags |= LOCATION_BIT_MASK;
		}

		// Build the packet
		buffer.order(ByteOrder.BIG_ENDIAN); // network byte order;

		// Add Header Fields
		buffer.put(flags);
		buffer.putLong(shout.getTimestamp().getMillis());

		// Add User Fields
		putPublicKey(buffer, shout.getSender().getPublicKey());
		buffer.put(new byte[AVATAR_HASH_SIZE]); // TODO: Get real hash from
												// User object
		putVArray(buffer, encodeUtf8(shout.getSender().getUsername()),
				LengthType.BYTE);

		// Add Message Fields
		String message = shout.getMessage();
		if (message != null) {
			putVArray(buffer, encodeUtf8(message), LengthType.BYTE);
		} else {
			buffer.put((byte) 0); // No message, put in 0 as length
		}
		if (hasLocation) {
			// TODO: get real location information
			buffer.putDouble(0);
			buffer.putDouble(0);
		}

		// Add Parent Reference Fields
		if (parent != null) {
			buffer.put(parent.getHash());
		}

		return buffer;
	}

	/**
	 * Serializes an already-signed shout. The buffer position is increased by
	 * the size of the shout data. If the buffer is too small, the position is
	 * increased by the amount of data that was added and a
	 * {@link BufferOverflowException} is thrown.
	 * 
	 * @param buffer the buffer into which to serialize the shout
	 * @param shout the shout to serialize
	 * @return the provided buffer
	 * @throws BufferOverflowException if {@code buffer} does not have room for
	 *             the shout
	 */
	public static ByteBuffer serializeShout(ByteBuffer buffer, Shout shout) {
		serializeShoutData(buffer, shout);
		putDsaSignature(buffer, shout.getSignature());
		return buffer;
	}

	/**
	 * Serialize the Shout data (not signature).
	 * <p>
	 * If the returned data is to be copied into another array, prefer using
	 * {@link #serializeShout(ByteBuffer, Shout)} to do the serialization in
	 * place.
	 * 
	 * @param shout the shout to serialize
	 * @return A serialized version of the shout
	 */
	public static byte[] serializeShoutData(UnsignedShout shout) {
		ByteBuffer buffer = ByteBuffer.allocate(SHOUT_UNSIGNED_SIZE_MAX);
		serializeShoutData(buffer, shout);
		return Arrays.copyOfRange(buffer.array(), 0, buffer.position());
	}

	/**
	 * Deserializes a shout.
	 * 
	 * @param buffer the buffer holding the serialized shout.
	 * @return the deserialized shout
	 * @throws BadShoutVersionException if the serialized shout is an
	 *             unsupported version
	 * @throws ShoutPacketException if the serialized shout contains invalid
	 *             data
	 * @throws InvalidShoutSignatureException if the included signature is
	 *             invalid
	 */
	public static BuildableShout deserializeShout(ByteBuffer buffer)
			throws UnsupportedVersionException,
			ShoutPacketException, InvalidShoutSignatureException {
		try {
			byte flags = buffer.get(buffer.position());
			switch (VERSION(flags)) {
				case 0:
					return deserializeVersion0Shout(buffer);
				default:
					throw new UnsupportedVersionException(
							String.format(
									"Shout object version %d is unsupported. Only version %d and below are supported.",
									VERSION(flags), VERSION));
			}
		} catch (BufferUnderflowException e) {
			throw new ShoutPacketException("Shout packet is missing expected data.", e);
		}
	}

	/**
	 * Deserialize a version 0 Shout.
	 * 
	 * @param buffer the buffer holding the serialized shout.
	 * @return the deserialized shout
	 * @throws ShoutPacketException if the serialized shout contains invalid
	 *             data
	 * @throws InvalidShoutSignatureException if the included signature is
	 *             invalid
	 */
	private static BuildableShout deserializeVersion0Shout(ByteBuffer buffer)
			throws ShoutPacketException,
			InvalidShoutSignatureException {

		BuildableUser user = new BuildableUser();
		BuildableShout shout = new BuildableShout();
		shout.user = user;

		try {
			buffer.order(ByteOrder.BIG_ENDIAN); // Network-byte order
			/*
			 * Mark the start of this shout in the buffer, so that clones made
			 * for hash and signature operations can be reset back to the
			 * correct starting position.
			 */
			buffer.mark();

			// Header fields
			byte flags = buffer.get();
			shout.timestamp = new DateTime(buffer.getLong());

			// Public Key
			user.publicKey = getPublicKey(buffer);

			// Avatar hash
			user.avatarHash = getArray(buffer, AVATAR_HASH_SIZE);

			// Username
			try {
				user.username = decodeUtf8Safe(getVArray(buffer, LengthType.BYTE, 1,
						USERNAME_SIZE_MAX));
			} catch (CharacterCodingException e) {
				throw new ShoutPacketException("Invalid encoding in username field.", e);
			} catch (InvalidLengthException e) {
				throw new ShoutPacketException("Invalid length for username field.", e);
			}

			// Message
			try {
				shout.message = decodeUtf8Safe(getVArray(buffer, LengthType.BYTE, 0,
						MESSAGE_SIZE_MAX));
			} catch (CharacterCodingException e) {
				throw new ShoutPacketException("Invalid encoding in message field.", e);
			} catch (InvalidLengthException e) {
				throw new ShoutPacketException("Invalid length for message field.", e);
			}

			// Location
			if (HAS_LOCATION(flags)) {
				shout.longitude = buffer.getDouble();
				shout.latitude = buffer.getDouble();
			}

			// Parent reference
			if (HAS_PARENT(flags)) {
				shout.parentHash = getArray(buffer, PARENT_HASH_SIZE);
			}

			/*
			 * Create a view of the portion of the data over which the signature
			 * is computed, for later signature verification.
			 */
			ByteBuffer signedData = flipToMark(buffer.asReadOnlyBuffer());

			// Signature
			shout.signature = getDsaSignature(buffer);
			if (!EcdsaWithSha256.verify(shout.signature, signedData, user.publicKey)) {
				throw new InvalidShoutSignatureException();
			}

			// Compute hash
			ByteBuffer clone = flipToMark(buffer.asReadOnlyBuffer());
			shout.hash = HashUtils.sha256(clone);

			return shout;
		} catch (BufferUnderflowException e) {
			throw new ShoutPacketException("Shout packet missing bytes", e);
		}
	}

	/**
	 * Serializes the x and y affine coordinates of public key to the provided
	 * buffer.
	 * 
	 * @param buffer the buffer to which to serialize the key
	 * @param key the key to serialize
	 * @return the provided buffer
	 */
	public static ByteBuffer putPublicKey(ByteBuffer buffer, ECPublicKey key) {
		ECPoint q = key.getECPublicKeyParameters().getQ();
		putBigInteger(buffer, q.getX().toBigInteger(), PUBLIC_KEY_AFFINE_SIZE, true);
		putBigInteger(buffer, q.getY().toBigInteger(), PUBLIC_KEY_AFFINE_SIZE, true);
		return buffer;
	}

	/**
	 * Deserializes a public key from the provided buffer, assuming the default
	 * named curve.
	 * 
	 * @param buffer the buffer from which to read the key
	 * @return the deserialized public key
	 */
	public static ECPublicKey getPublicKey(ByteBuffer buffer) {
		BigInteger x = getBigInteger(buffer, PUBLIC_KEY_AFFINE_SIZE, true);
		BigInteger y = getBigInteger(buffer, PUBLIC_KEY_AFFINE_SIZE, true);
		return KEY_GENERATOR.generatePublic(x, y);
	}

	/**
	 * Serializes the r and s values of the DSA signature to the provided
	 * buffer.
	 * 
	 * @param buffer the buffer to which to serialize the signature
	 * @param sig the signature to serialize
	 * @return the provided buffer
	 */
	public static ByteBuffer putDsaSignature(ByteBuffer buffer, DsaSignature sig) {
		putBigInteger(buffer, sig.getR(), SIGNATURE_R_SIZE, true);
		putBigInteger(buffer, sig.getS(), SIGNATURE_S_SIZE, true);
		return buffer;
	}

	/**
	 * Deserializes a DSA signature from the provided buffer.
	 * 
	 * @param buffer the buffer from which to read the signature
	 * @return the deserialized signature
	 */
	public static DsaSignature getDsaSignature(ByteBuffer buffer) {
		BigInteger r = getBigInteger(buffer, SIGNATURE_R_SIZE, true);
		BigInteger s = getBigInteger(buffer, SIGNATURE_S_SIZE, true);
		return new DsaSignature(r, s);
	}

	/**
	 * Generates the hash of the provided signed shout.
	 * 
	 * @param shout the shout to hash
	 * @return the hash of the provied shout
	 */
	public static byte[] generateHash(Shout shout) {
		ByteBuffer serialized = serializeShout(ByteBuffer.allocate(SHOUT_SIGNED_SIZE_MAX),
				shout);
		serialized.flip();
		return HashUtils.sha256(serialized);
	}

	private static class BuildableUser implements User {

		String username;
		ECPublicKey publicKey;
		@SuppressWarnings("unused")
		byte[] avatarHash;

		@Override
		public String getUsername() {
			return username;
		}

		@Override
		public ECPublicKey getPublicKey() {
			return publicKey;
		}

	}

	public static class BuildableShout implements Shout {

		public DateTime timestamp = null;
		public User user = null;

		public String message = null;
		public Double longitude = null;
		public Double latitude = null;

		public BuildableShout parent = null;
		public byte[] parentHash = null;

		public DsaSignature signature = null;
		public byte[] hash = null;

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
		public DsaSignature getSignature() {
			return signature;
		}

		@Override
		public byte[] getHash() {
			return hash;
		}

	}

}
