
package org.whispercomm.shout.util;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

/**
 * Utilities for working with strings and character encodings.
 * 
 * @author David R. Bild
 */
public class CharUtils {

	public static final Charset UTF_8 = Charset.forName("UTF-8");

	/**
	 * Gets a new instance of the decoder for the specified charset and
	 * configures it to report malformed input and unmappable characters.
	 * 
	 * @param charset the charset whose decoder to return
	 * @return a new instance of the decoder for the specified charset
	 */
	public static CharsetDecoder newSafeDecoder(Charset charset) {
		return charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);
	}

	/**
	 * Returns the decoded string for the specified array of UTF-8 bytes. Throws
	 * exceptions if the array is not valid UTF-8.
	 * 
	 * @param array the UTF-8 bytes
	 * @return the decoded string
	 * @throws MalformedInputException if an illegal input byte sequence was
	 *             encountered
	 * @throws UnmappableCharacterException if a legal but unmappable input byte
	 *             sequence was encountered
	 * @throws CharacterCodingException if another exceptions happens during
	 *             decode
	 */
	public static String decodeUtf8Safe(byte[] array) throws
			MalformedInputException, UnmappableCharacterException, CharacterCodingException {
		return decodeUtf8Safe(ByteBuffer.wrap(array));
	}

	/**
	 * Returns the decoded string for the specified buffer of UTF-8 bytes or
	 * {@code null} if the buffer is empty. Throws exceptions if the array is
	 * not valid UTF-8.
	 * 
	 * @param buffer the UTF-8 bytes
	 * @return the decoded string or {@code null} if the buffer has no remaining
	 *         bytes.
	 * @throws MalformedInputException if an illegal input byte sequence was
	 *             encountered
	 * @throws UnmappableCharacterException if a legal but unmappable input byte
	 *             sequence was encountered
	 * @throws CharacterCodingException if another exceptions happens during
	 *             decode
	 */
	public static String decodeUtf8Safe(ByteBuffer buffer) throws
			MalformedInputException, UnmappableCharacterException, CharacterCodingException {
		if (buffer.remaining() == 0) {
			return null; // TODO: Use empty string, not null
		} else {
			CharsetDecoder decoder = newSafeDecoder(UTF_8);
			return decoder.decode(buffer).toString();
		}
	}

	/**
	 * Returns the UTF-8 encoding of the given string.
	 * 
	 * @param string the string to encode
	 * @return the UTF-8 encoding
	 */
	public static ByteBuffer encodeUtf8(String string) {
		return UTF_8.encode(string);
	}

}
