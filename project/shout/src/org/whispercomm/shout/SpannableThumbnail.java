
package org.whispercomm.shout;

import org.whispercomm.shout.text.ShoutUriSpan;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;

import com.squareup.picasso.Target;

public class SpannableThumbnail extends SpannableStringBuilder implements Target {

	private final Context mContext;
	private final int length;
	private final String URI;
	private final SpannableStringBuilder original;
	private final int start;
	private final int end;
	private final TextView view;
	public Bitmap mBitmap;

	public SpannableThumbnail(CharSequence source, TextView text, SpannableStringBuilder s,
			String uri, int Start, int End) {
		super(source);
		mContext = text.getContext();
		length = source.length();
		view = text;
		URI = uri;
		original = s;
		start = Start;
		end = End;
		this.setSpan(new ImageSpan(mContext, R.drawable.defaultavatar),
				0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		this.setSpan(new ShoutUriSpan(URI), 0, length,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

	}

	private static final String TAG = SpannableThumbnail.class.getSimpleName();

	@Override
	public void onError() {
		Log.i(TAG, "The Bitmap failed to load.");
	}

	@Override
	public void onSuccess(Bitmap arg0) {
		Log.i(TAG, "The Bitmap is successfully loaded.");
		this.clearSpans();

		this.setSpan(new ImageSpan(mContext, scaleBitmap(arg0, 64)), 0,
				length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		this.setSpan(new ShoutUriSpan(URI), 0, length,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		original.replace(start, end, this);
		view.setText(original);

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
}
