
package org.whispercomm.shout;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Type-safe encapsulation of a RFC 4288 mime type. This class provides a static
 * cache for reusing the {@code MimeType} objects.
 * 
 * @author David R. Bild
 */
public class MimeType {

	/**
	 * Regular expression for valid MimeType strings, from RFC 4288, Sec. 4.2,
	 * page 6:
	 * 
	 * <pre>
	 * Type and subtype names MUST conform to the following ABNF:
	 * 
	 *   type-name = reg-name
	 *   subtype-name = reg-name
	 * 
	 *   reg-name = 1*127reg-name-chars
	 *   reg-name-chars = ALPHA / DIGIT / "!" /
	 *                   "#" / "$" / "&" / "." /
	 *                   "+" / "-" / "^" / "_"
	 * </pre>
	 */
	private static Pattern MIMETYPE_REGEX = Pattern
			.compile("^([\\w!#$&.+-^]{1,127})/([\\w!#$&.+-^]{1,127})$");

	public static MimeType JPEG = new MimeType("image/jpeg");
	public static MimeType GIF = new MimeType("image/gif");
	public static MimeType PNG = new MimeType("image/png");

	private static ConcurrentMap<String, MimeType> cache = new ConcurrentHashMap<String, MimeType>();

	static {
		cache.put(JPEG.toString(), JPEG);
		cache.put(GIF.toString(), GIF);
		cache.put(PNG.toString(), PNG);
	}

	/**
	 * Normalizes the string representation of the mimetype.
	 * <p>
	 * The current implementation just converts the string to lowercase.
	 * 
	 * @param mimetype the mime type string
	 * @return the normalized mime type string
	 */
	private static String normalize(String mimetype) {
		// Only ASCII characters are valid in mime types, so Locale.US is ok.
		return mimetype.toLowerCase(Locale.US);
	}

	/**
	 * Retrieve the cached {@link MimeType} object for the specified MIME type.
	 * Creates and caches a new {@link MimeType} object if one does not already
	 * exist.
	 * 
	 * @param mimetype the mime type to retrieve
	 * @return the {@code MimeType} object
	 * @throws IllegalArgumentException if the string does not represent a valid
	 *             RFC 4288 mime type
	 */
	public static MimeType get(String mimetype) throws IllegalArgumentException {
		mimetype = normalize(mimetype);
		if (cache.containsKey(mimetype)) {
			return cache.get(mimetype);
		} else {
			cache.putIfAbsent(mimetype, new MimeType(mimetype));
			return get(mimetype);
		}
	}

	private final String mimetype;
	private final String type;
	private final String subtype;

	/**
	 * Creates a new {@code MimeType} object for the specified mime type.
	 * 
	 * @param mimetype the mime type to retrieve
	 * @throws IllegalArgumentException if the string does not represent a valid
	 *             RFC 4288 mime type
	 */
	public MimeType(String mimetype) throws IllegalArgumentException {
		mimetype = normalize(mimetype);

		Matcher matcher = MIMETYPE_REGEX.matcher(mimetype);
		if (!matcher.matches())
			throw new IllegalArgumentException("String is not a valid mimetype: ");

		this.mimetype = mimetype;
		this.type = matcher.group(1);
		this.subtype = matcher.group(2);
	}

	public MimeType(byte[] mimetype) {
		this(decodeAscii(mimetype));
	}

	private static String decodeAscii(byte[] mimetype) {
		try {
			return new String(mimetype, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// Should never happen. Testing will catch a missing encoding.
			throw new RuntimeException(e);
		}
	}

	public String type() {
		return this.type;
	}

	public String subtype() {
		return this.subtype;
	}

	public short length() {
		return (short) this.mimetype.length();
	}

	/**
	 * Checks if this mime type is the set of provided types
	 * 
	 * @param mimetypes the set of mime types to compare with
	 * @return {@code true} if this mime type is the provided set and
	 *         {@code false} otherwise.
	 */
	public boolean is(MimeType... mimetypes) {
		for (MimeType arg : mimetypes) {
			if (equals(arg))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return mimetype;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mimetype == null) ? 0 : mimetype.hashCode());
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
		MimeType other = (MimeType) obj;
		if (mimetype == null) {
			if (other.mimetype != null)
				return false;
		} else if (!mimetype.equals(other.mimetype))
			return false;
		return true;
	}

}
