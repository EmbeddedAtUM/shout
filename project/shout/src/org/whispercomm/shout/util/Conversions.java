package org.whispercomm.shout.util;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Utility methods for converting between various units.
 * 
 * @author David R. Bild
 * 
 */
public class Conversions {

	/**
	 * Convert from display indepedent pixels to pixels.
	 * 
	 * @param dp
	 *            numbers display independent pixels
	 * @param res
	 *            resources object providing access to the screen densitity
	 * @return number of pixels
	 */
	public static int dpToPx(int dp, Resources res) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				res.getDisplayMetrics());
	}

}
