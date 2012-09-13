
package org.whispercomm.shout.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Utility methods for working with ByteBuffers
 * 
 * @author David R. Bild
 */
public class ByteBufferUtils {

	/**
	 * Flips the buffer relative to the mark by setting the limit to the current
	 * position and then resetting the position to the mark.
	 * 
	 * @param buffer the buffer to flip
	 * @return the specified buffer
	 */
	public static ByteBuffer flipToMark(ByteBuffer buffer) {
		buffer.limit(buffer.position()).reset();
		return buffer;
	}

	/**
	 * Enum specifying the number of bytes to use to encode the length of a
	 * variable length array.
	 */
	public enum LengthType {
		BYTE, SHORT, INT, LONG
	};

	/**
	 * Writes the length of the given byte array, followed by the bytes in said
	 * array, to the current position and increases the position by the number
	 * of bytes in the length plus the number of bytes written. The number of
	 * bytes used for the length is specified by the type argument.
	 * 
	 * @param buffer the buffer into which to write the length and array
	 *            contents
	 * @param src the array whose contents to write to the buffer
	 * @param type the type to use when writing the array length to the buffer
	 * @return the buffer
	 * @throws IllegalArgumentException if the length of the array does not fit
	 *             in an unsigned interpretation of the specified type
	 */
	public static ByteBuffer putVArray(ByteBuffer buffer, byte[] src, LengthType type) {
		return putVArray(buffer, ByteBuffer.wrap(src), type);
	}

	/**
	 * Writes the remaining bytes in the given source buffer, preceded by the
	 * number of remaining bytes, to the current position of the given buffer
	 * and increases the position by the number of bytes in the length plus the
	 * number of bytes written. The number of bytes used for the length is
	 * specified by the type argument.
	 * 
	 * @param buffer the buffer into which to write the length and buffer
	 *            contents
	 * @param src the buffer whose contents to write to the buffer
	 * @param type the type to use when writing the array length to the buffer
	 * @return the buffer
	 * @throws IllegalArgumentException if the length of the array does not fit
	 *             in an unsigned interpretation of the specified type
	 */
	public static ByteBuffer putVArray(ByteBuffer buffer, ByteBuffer src, LengthType type)
			throws IllegalArgumentException {
		String EXCEPTION_MSG = "Max supported length is %d. Got array of length %d.";
		int len = src.remaining();
		switch (type) {
			case BYTE:
				if (len >= (1 << 8)) {
					throw new IllegalArgumentException(String.format(EXCEPTION_MSG, 1 << 8, len));
				}
				buffer.put((byte) len);
				break;
			case SHORT:
				if (len >= (1 << 16)) {
					throw new IllegalArgumentException(String.format(EXCEPTION_MSG, 1 << 16, len));
				}
				buffer.putShort((short) len);
				break;
			case INT:
				// Array can't be longer than fits in 32 bits
				buffer.putInt(len);
				break;
			case LONG:
			default:
				// Array can't be longer than fits in 64 bits
				buffer.putLong(len);
				break;
		}
		return buffer.put(src);
	}

	/**
	 * Reads an array of the specified length from the buffer and advances the
	 * position by the number of bytes read.
	 * 
	 * @param buffer the buffer from which to read
	 * @param len the number of bytes to read
	 * @return the read array
	 */
	public static byte[] getArray(ByteBuffer buffer, int len) {
		byte[] array = new byte[len];
		buffer.get(array);
		return array;
	}

	/**
	 * Reads a variable length array from the buffer and advances the position
	 * to the end of the array. The array must have been written in the format
	 * used by {@link #putVArray(ByteBuffer, byte[], LengthType)}. The type used
	 * for the length must be specified.
	 * 
	 * @param buffer the buffer from which to read
	 * @param type the used when writing the array length to the buffer
	 * @return the read array
	 */
	public static byte[] getVArray(ByteBuffer buffer, LengthType type) {
		try {
			return getVArray(buffer, type, null, null);
		} catch (InvalidLengthException e) {
			// Cannot occur when no length-bounds are specified
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Reads a variable length array from the buffer and advances the position
	 * to the end of the array. The array must have been written in the format
	 * used by {@link #putVArray(ByteBuffer, byte[], LengthType)}. The type used
	 * for the length must be specified. If the read length is outside of the
	 * specified bounds, [minLength,maxLength], an exception is thrown.
	 * 
	 * @param buffer the buffer from which to read
	 * @param type the used when writing the array length to the buffer
	 * @param minLength the minimum length of the array
	 * @param maxLength the maximum length of the array
	 * @return the read array
	 * @throws InvalidLengthException if the read length is outside of the bound
	 *             [minLength, maxLength]
	 */
	public static byte[] getVArray(ByteBuffer buffer, LengthType type, Integer minLength,
			Integer maxLength)
			throws InvalidLengthException {
		int len;
		switch (type) {
			case BYTE:
				len = 0xFF & buffer.get();
				break;
			case SHORT:
				len = 0xFFFF & buffer.getShort();
				break;
			case INT:
				len = 0xFFFFFFFF & buffer.getInt();
				break;
			case LONG:
			default:
				len = (int) buffer.getLong();
				break;
		}
		if ((minLength != null && len < minLength) || (maxLength != null && len > maxLength)) {
			throw new InvalidLengthException(minLength, maxLength, len);
		}
		return getArray(buffer, len);
	}

	/**
	 * Serializes a {@link BigInteger} into the given buffer, padding with sign
	 * extension bits up to the specified length. If the value is unsigned,
	 * extraneous leading zero byte are removed, up to the required padding.
	 * 
	 * @param buffer the buffer to which to serialize the integer
	 * @param value the integer to serialize
	 * @param len the number of bytes to fill
	 * @param unsigned {@code true} if {@code value} should be treated as
	 *            unsigned
	 * @return the provided buffer
	 */
	public static ByteBuffer putBigInteger(ByteBuffer buffer, BigInteger value,
			int len, boolean unsigned) {
		// Get the value in big endian 2's complement.
		byte[] data = value.toByteArray();

		// If unsigned, don't store the extra 0 byte that contains the sign-bit,
		// if it exists.
		int startIdx = 0;
		if (unsigned && data[0] == 0) {
			startIdx = 1;
		}

		// Ensure the requested length is large enough
		int dataLen = data.length - startIdx;
		if (dataLen > len) {
			throw new IllegalArgumentException(String.format(
					"Argument len too small. Got: %d.  Must be at least: %d.", len, dataLen));
		}

		// Insert padding first, for big endian
		int padLen = len - dataLen;
		byte padValue = (byte) (data[0] >> 7);
		for (int i = 0; i < padLen; ++i) {
			buffer.put(padValue);
		}

		return buffer.put(data, startIdx, dataLen);
	}

	/**
	 * Deserializes a {@link BigInteger} from the given buffer.
	 * 
	 * @param buffer the buffer from which to deserialize the integer
	 * @param len the number of bytes that hold the serialized integer
	 * @param unsigned {@code true} if {@code value} should be treated as
	 *            unsigned
	 * @return the integer
	 */
	public static BigInteger getBigInteger(ByteBuffer buffer, int len, boolean unsigned) {
		byte[] data = unsigned ? new byte[len + 1] : new byte[len];

		int startIdx = unsigned ? 1 : 0;
		buffer.get(data, startIdx, len);

		return new BigInteger(data);
	}

	public static class InvalidLengthException extends Exception {
		private static final long serialVersionUID = -4898459278333707998L;

		private static String MSG = "Invalid length: %d. Must be in range [%d, %d]";

		public InvalidLengthException(int minLength, int maxLength, int length) {
			super(String.format(MSG, length, minLength, maxLength));
		}
	}

}
