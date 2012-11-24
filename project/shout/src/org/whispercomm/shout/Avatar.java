
package org.whispercomm.shout;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Class representing an avatar.
 * <p>
 * This class is a thin wrapper around a {@link Bitmap}, providing decoding and
 * max size verification.
 * 
 * @author David R. Bild
 */
public class Avatar {
	public static final int MAX_LEN = 100 * 1024; // 100 KB

	private final byte[] data;

	private final Bitmap bitmap;

	private final MimeType mimetype;

	/**
	 * Constructs an {@code Avatar} from a jpeg-, gif-, or png-encoded image.
	 * The maximum allowable size of the encoded image is {@link #MAX_LEN}
	 * bytes.
	 * 
	 * @param data the encoded bitmap
	 * @throws IllegalArgumentException if {@code data} is larger than
	 *             {@link #MAX_LEN} bytes or the data is not a jpeg-, gif-, or
	 *             png-encoded image.
	 */
	public Avatar(byte[] data) throws IllegalArgumentException {
		if (data.length > MAX_LEN)
			throw new IllegalArgumentException(String.format(
					"Avatar data cannot be more than %d bytes.  Got %d.", MAX_LEN, data.length));

		BitmapFactory.Options options = new BitmapFactory.Options();
		this.data = data;
		this.bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

		if (bitmap == null)
			throw new IllegalArgumentException(
					"Stream does not contain a valid jpeg, gif, or png image");

		mimetype = MimeType.get(options.outMimeType);
	}

	/**
	 * Constructs an {@code Avatar} from a jpeg-, gif-, or png-encoded image.
	 * The maximum allowable size of the encoded image is {@link #MAX_LEN}
	 * bytes.
	 * 
	 * @param stream the encoded bitmap
	 * @throws IOException if the encoded bitmap is larger than {@link #MAX_LEN}
	 *             bytes.
	 */
	public Avatar(InputStream stream) throws IOException {
		this(IOUtils.toByteArray(stream));
	}

	/**
	 * Retrieves the encoded form of the avatar.
	 * 
	 * @see #getMimeType()
	 * @return the encoded avatar
	 */
	public byte[] toByteArray() {
		return data;
	}

	/**
	 * Retrieves the mime type of the byte[] encoding of this avatar
	 * 
	 * @return the mime type of the byte[] encoding of this avatar
	 */
	public MimeType getMimeType() {
		return mimetype;
	}

	/**
	 * Retrieves the avatar as a {@link Bitmap}
	 * 
	 * @return the avatar bitmap
	 */
	public Bitmap getBitmap() {
		return bitmap;
	}

}
