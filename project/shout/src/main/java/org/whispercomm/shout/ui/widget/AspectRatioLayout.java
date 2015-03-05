
package org.whispercomm.shout.ui.widget;

import org.whispercomm.shout.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * A Layout that maintains a restricted aspect ratio.
 * <p>
 * This layout takes up to two constraints, the minimum and maximum aspect
 * ratio, specified by the XML attributes {@code minRatio} and {@code maxRatio}.
 */
public class AspectRatioLayout extends FrameLayout {

	private float mMinAspectRatio;

	private float mMaxAspectRatio;

	public AspectRatioLayout(Context context)
	{
		this(context, null);
	}

	public AspectRatioLayout(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public AspectRatioLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioLayout,
				defStyle, 0);

		float minRatio = a.getFloat(R.styleable.AspectRatioLayout_minRatio, 0);
		float maxRatio = a.getFloat(R.styleable.AspectRatioLayout_maxRatio,
				Float.POSITIVE_INFINITY);

		setAspectRatio(minRatio, maxRatio);

		a.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		/* Figure out desired size */
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		/* Then check the aspect ratio */
		float curWidth = this.getMeasuredWidth();
		float curHeight = this.getMeasuredHeight();
		float aspectRatio = curWidth / curHeight;

		/* Modify to meet constraint */
		int newWidth = (int) curWidth;
		int newHeight = (int) curHeight;
		if (aspectRatio < mMinAspectRatio) {
			// Too tall. Decrease height.
			newHeight = (int) (newWidth * mMinAspectRatio);
		} else if (aspectRatio > mMaxAspectRatio) {
			// Too wide. Decrease width.
			newWidth = (int) (newHeight / mMaxAspectRatio);
		}

		super.onMeasure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
	}

	private void checkConstraints() {
		if (mMinAspectRatio > mMaxAspectRatio)
			throw new IllegalArgumentException(
					"Minimum aspect ratio must be less than the maximum.");
	}

	public void setAspectRatio(float minRatio, float maxRatio) {
		mMinAspectRatio = minRatio;
		mMaxAspectRatio = maxRatio;
		checkConstraints();
		requestLayout();
	}

	public float getMinAspectRatio() {
		return mMinAspectRatio;
	}

	public void setMinAspectRatio(float ratio) {
		mMinAspectRatio = ratio;
		checkConstraints();
		requestLayout();
	}

	public float getMaxAspectRatio() {
		return mMaxAspectRatio;
	}

	public void setMaxAspectRatio(float ratio) {
		mMaxAspectRatio = ratio;
		checkConstraints();
		requestLayout();
	}

}
