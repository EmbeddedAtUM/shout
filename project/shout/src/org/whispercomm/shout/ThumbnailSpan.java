
package org.whispercomm.shout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.widget.TextView;

import com.squareup.picasso.Target;

/**
 * This span allows thumbnail of an image to show in the shout. It is used
 * together with Picasso library and thus implements Target.
 * 
 * @author Bowen Xu
 */

public class ThumbnailSpan extends ReplacementSpan implements Target {

	private static final String TAG = ThumbnailSpan.class.getSimpleName();
	private static Context mContext;
	private final TextView view;
	private Drawable mDrawable;
	private Spannable mSpannable;

	public ThumbnailSpan(TextView View, Spannable spannable) {
		mContext = View.getContext();
		view = View;
		mSpannable = spannable;
		mDrawable = mContext.getResources().getDrawable(R.drawable.defaultimage);

	}

	@Override
	public void onError() {
		Log.i(TAG, "The Bitmap failed to load.");

		mDrawable = mContext.getResources().getDrawable(R.drawable.brokenpicture);
		refreshMessage();
	}

	@Override
	public void onSuccess(Bitmap arg0) {

		Log.i(TAG, "The Bitmap is successfully loaded.");

		mDrawable = new BitmapDrawable(mContext.getResources(), scaleBitmap(arg0, 128));
		refreshMessage();

	}

	/**
	 * Refresh the textview after reset mDrawable.
	 */
	private void refreshMessage() {
		int start = mSpannable.getSpanStart(this);
		int end = mSpannable.getSpanEnd(this);
		if (start != -1 && end != -1) {
			mSpannable.removeSpan(this);
			mSpannable.setSpan(this, start, end,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			view.setText(mSpannable);
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

	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
			int bottom, Paint paint) {
		Drawable b = getDrawable();
		canvas.save();

		int transY = bottom - b.getBounds().bottom;

		canvas.translate(x, transY);
		b.draw(canvas);
		canvas.restore();

	}

	@Override
	public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
		Drawable d = getDrawable();
		Rect rect = d.getBounds();

		if (fm != null) {
			fm.ascent = -rect.bottom;
			fm.descent = 0;

			fm.top = fm.ascent;
			fm.bottom = 0;
		}

		return rect.right;
	}

	private Drawable getDrawable() {
		mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(),
				mDrawable.getIntrinsicHeight());
		return mDrawable;
	}

}
