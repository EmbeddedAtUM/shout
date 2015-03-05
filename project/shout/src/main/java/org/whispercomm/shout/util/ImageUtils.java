
package org.whispercomm.shout.util;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;

public class ImageUtils {

	/**
	 * Wrapper around {@link Bitmap} to compress bitmap to a byte array. See
	 * {@link Bitmap} for details on the method arguments.
	 * 
	 * @return a byte[] containing the compressed bitmap
	 */
	public static byte[] compressBitmap(Bitmap bitmap, Bitmap.CompressFormat format,
			int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(format, quality, baos);
		return baos.toByteArray();
	}

}
