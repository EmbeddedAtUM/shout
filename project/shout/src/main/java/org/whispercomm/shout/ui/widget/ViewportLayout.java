
package org.whispercomm.shout.ui.widget;

import org.whispercomm.shout.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/**
 * A Layout that can match its width and height to those of another view(s). For
 * example, it can be used to allow a child of a {@link ScrollView} to take the
 * the largest size such that the whole child can still be viewed on the screen.
 * This class extends {@link FrameLayout} and otherwise behaves as such.
 * <p>
 * The XML attribute {@code viewportWidth} specifies the view from which this
 * layout should take its maximum width. Similarly, {@code viewportHeight}
 * specifies the view from which the maximum height is taken.
 * <p>
 * The maximum size is enforced only if the corresponding layout dimension is
 * set to {@code wrap_content}. E.g., to match the height of the layout to a
 * {@link ScrollView} with id {@code sv}, set these attributes on the
 * ViewportLayout:<br/>
 * {@code layout_height="wrap_content"}<br/>
 * {@code viewportHeight="@id/sv"}<br/>
 * <p>
 * NOTE: This class has only been tested in one specific application for one
 * specific layout. Also, bad things are likely to happen if you nest them or
 * put them in list views or do anything else that's not extremely simple.
 */
public class ViewportLayout extends FrameLayout {

	private int mViewportHeightId;
	private int mViewportWidthId;

	public ViewportLayout(Context context)
	{
		this(context, null);
	}

	public ViewportLayout(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public ViewportLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewportLayout,
				defStyle, 0);

		setViewportHeight(a.getResourceId(R.styleable.ViewportLayout_viewportHeight, 0));
		setViewportWidth(a.getResourceId(R.styleable.ViewportLayout_viewportWidth, 0));

		a.recycle();
	}

	public void setViewportWidth(int resid) {
		mViewportWidthId = resid;
		requestLayout();
	}

	public void setViewportHeight(int resid) {
		mViewportHeightId = resid;
		requestLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		/*
		 * If the measure spec is UNSPECIFIED (i.e., wrap_content), replace max
		 * size with that of the specified parent view.
		 */

		if (mViewportWidthId != 0
				&& MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED)
		{
			View parent = getRootView().findViewById(mViewportWidthId);
			int maxSize = parent.getWidth();
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxSize,
					MeasureSpec.AT_MOST);
		}

		if (mViewportHeightId != 0
				&& MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
			View parent = getRootView().findViewById(mViewportHeightId);
			int maxSize = parent.getHeight();
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxSize,
					MeasureSpec.AT_MOST);
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
