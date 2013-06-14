
package org.whispercomm.shout.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.whispercomm.shout.R;
import org.whispercomm.shout.SpannableThumbnail;
import org.whispercomm.shout.image.provider.ImageProviderContract.Thumbnails;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ShoutLinkify {

	public static final Pattern SHOUT_URI = Pattern.compile("shout://\\p{XDigit}{64}",
			+Pattern.CASE_INSENSITIVE);

	public static final String LINK_TEXT = "Image";

	public static final boolean addLinks(TextView text) {
		CharSequence t = text.getText();
		SpannableStringBuilder s = SpannableStringBuilder.valueOf(t);

		if (addLinks(s, text)) {
			addLinkMovementMethod(text);
			text.setText(s);
			return true;
		}

		return false;
	}

	private static final void addLinkMovementMethod(TextView t) {
		MovementMethod m = t.getMovementMethod();

		if ((m == null) || !(m instanceof LinkMovementMethod)) {
			if (t.getLinksClickable()) {
				t.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
	}

	/**
	 * Scales a bitmap so that the greatest dimension is @{code dim}
	 */
	public static Bitmap scaleBitmap(Bitmap b, double dim) {
		// Not sure if the following code is clever or a nasty hack...
		int width = b.getWidth();
		int height = b.getHeight();

		int dims[] = {
				width, height
		};

		// If the height is greater than 768 px, scale the image
		int largeIndex = dims[0] > dims[1] ? 0 : 1;
		int smallIndex = largeIndex == 0 ? 1 : 0;

		if (dims[largeIndex] > dim) {
			double ratio = dim / dims[largeIndex];
			dims[smallIndex] *= ratio;
			dims[largeIndex] = (int) dim;
		}

		width = dims[0];
		height = dims[1];

		return Bitmap.createScaledBitmap(b, width, height, false);
	}

	private static final boolean addLinks(SpannableStringBuilder s, TextView text) {
		boolean match = false;

		Matcher matcher = SHOUT_URI.matcher(s);
		Context context = text.getContext();
		SpannableStringBuilder s2 = new SpannableStringBuilder(s);
		while (matcher.find()) {
			match = true;

			int start = matcher.start();
			int end = matcher.end();

			String uri = matcher.group();
			String hashStr = uri.substring(8);

			Uri mUri = Uri.withAppendedPath(Thumbnails.CONTENT_URI, hashStr);
			SpannableThumbnail replacement = new SpannableThumbnail(LINK_TEXT, text, s2, uri,
					start, end);
			Picasso.with(context).load(mUri.toString())
					.placeholder(R.drawable.defaultavatar)
					.error(R.drawable.brokenpicture)
					.into(replacement);

			s.replace(start, end, replacement);
			matcher = SHOUT_URI.matcher(s);
		}

		return match;
	}
}
