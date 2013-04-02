
package org.whispercomm.shout.ui.widget;

import org.whispercomm.shout.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Widget for an expandable view.
 * <p>
 * This widget takes two child views, with ids "trigger" and "content". Clicking
 * on the trigger view will toggle the visibility of the content view.
 * <p>
 * If no trigger view is created, a default one will be created and inserted at
 * the top of the expandable view.
 * <p>
 * The widget also takes child views with ids "icon" ({@link ImageView}),
 * "header" ({@link TextView}), and "subheader" ({@link TextView}). These can be
 * accessed via
 * <p>
 * The ExpandableViewState states "state_expanded" and "state_collapsed" are set
 * on this view indicating if the content is shown or hidden. Child views can
 * inherit this state (see {@link View#setDuplicateParentStateEnabled(boolean)})
 * to configure themselves accordingly, e.g., change the drawable on the trigger
 * button.
 */
public class ExpandableView extends RelativeLayout {

	private static final int[] STATE_EXPANDED = {
			R.attr.state_expanded
	};

	private View mContent = null;
	private View mTrigger = null;

	private ImageView mIcon = null;
	private TextView mHeader = null;
	private TextView mSubheader = null;

	private int mHeaderTextAppearance;
	private int mSubheaderTextAppearance;
	private String mHeaderText;
	private String mSubheaderText;

	private Drawable mIconDrawable;

	private int mTriggerPaddingTop;
	private int mTriggerPaddingBottom;
	private int mTriggerPaddingLeft;
	private int mTriggerPaddingRight;

	private int mTriggerBackground;

	private boolean mExpanded;

	public ExpandableView(Context context) {
		this(context, null);
	}

	public ExpandableView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.expandableViewStyle);
	}

	public ExpandableView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableView, defStyle,
				0);

		mHeaderTextAppearance = a.getResourceId(
				R.styleable.ExpandableView_headerTextAppearance,
				android.R.attr.textAppearanceLarge);
		mHeaderTextAppearance = a.getResourceId(
				R.styleable.ExpandableView_subheaderTextAppearance,
				android.R.attr.textAppearanceMedium);
		mHeaderText = a.getString(R.styleable.ExpandableView_header);
		mSubheaderText = a.getString(R.styleable.ExpandableView_subheader);
		mIconDrawable = a.getDrawable(R.styleable.ExpandableView_icon);
		mExpanded = a.getBoolean(R.styleable.ExpandableView_expanded, false);

		int triggerPadding = a.getDimensionPixelSize(R.styleable.ExpandableView_triggerPadding, 0);
		mTriggerPaddingTop = a.getDimensionPixelSize(R.styleable.ExpandableView_triggerPaddingTop,
				triggerPadding);
		mTriggerPaddingBottom = a.getDimensionPixelSize(
				R.styleable.ExpandableView_triggerPaddingBottom, triggerPadding);
		mTriggerPaddingLeft = a.getDimensionPixelSize(
				R.styleable.ExpandableView_triggerPaddingLeft, triggerPadding);
		mTriggerPaddingRight = a.getDimensionPixelSize(
				R.styleable.ExpandableView_triggerPaddingRight, triggerPadding);

		mTriggerBackground = a.getResourceId(R.styleable.ExpandableView_triggerBackground, 0);

		a.recycle();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		/* Find the referenced child views by id */

		// Required child view
		mContent = (View) findViewById(R.id.content);
		if (mContent == null) {
			throw new IllegalArgumentException(
					"The content attribute must refer to a valid child view");
		}

		// If not found, inflate the default trigger view
		mTrigger = (View) findViewById(R.id.trigger);
		if (mTrigger == null) {
			mTrigger = View.inflate(getContext(), R.layout.widget_expandable_view_trigger, this);
			mTrigger = (View) findViewById(R.id.trigger);
		}

		// Get the icon and header views, if available
		mHeader = (TextView) findViewById(R.id.header);
		mSubheader = (TextView) findViewById(R.id.subheader);
		mIcon = (ImageView) findViewById(R.id.icon);

		// Set the default values from the attributes
		if (mHeader != null && mHeaderText != null)
			setHeader(mHeaderText);

		if (mSubheader != null && mSubheaderText != null)
			setSubheader(mSubheaderText);

		if (mIcon != null && mIconDrawable != null)
			setIcon(mIconDrawable);

		setHeaderTextAppearance(mHeaderTextAppearance);
		setSubheaderTextAppearance(mSubheaderTextAppearance);
		setExpanded(mExpanded);
		setTriggerPadding(mTriggerPaddingLeft, mTriggerPaddingTop, mTriggerPaddingRight,
				mTriggerPaddingBottom);
		mTrigger.setBackgroundResource(mTriggerBackground);

		// Configure click listener for trigger
		mTrigger.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleExpanded();
			}
		});

	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		if (mExpanded) {
			final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
			mergeDrawableStates(drawableState, STATE_EXPANDED);
			return drawableState;
		} else {
			return super.onCreateDrawableState(extraSpace);
		}
	}

	public boolean isExpanded() {
		return mExpanded;
	}

	public void setExpanded(boolean expanded) {
		mExpanded = expanded;
		updateContentVisibility();
	}

	public void toggleExpanded() {
		setExpanded(!mExpanded);
	}

	private void updateContentVisibility() {
		if (mContent != null) {
			mContent.setVisibility(mExpanded ? View.VISIBLE : View.GONE);
			refreshDrawableState();
		}
	}

	public void setHeaderTextAppearance(int resid) {
		mHeaderTextAppearance = resid;
		mHeader.setTextAppearance(getContext(), mHeaderTextAppearance);
	}

	public String getHeader() {
		return mHeaderText;
	}

	public void setHeader(int resId) {
		String header = getResources().getString(resId);
		setHeader(header);
	}

	public void setHeader(String text) {
		mHeaderText = text;
		mHeader.setText(text);
	}

	public void setSubheaderTextAppearance(int resid) {
		mSubheaderTextAppearance = resid;
		mSubheader.setTextAppearance(getContext(), mSubheaderTextAppearance);
	}

	public String getSubheader() {
		return mSubheaderText;
	}

	public void setSubheader(int resId) {
		String subheader = getResources().getString(resId);
		setSubheader(subheader);
	}

	public void setSubheader(String text) {
		mSubheaderText = text;
		mSubheader.setText(text);
	}

	public Drawable getIcon() {
		return mIconDrawable;
	}

	public void setIcon(int resId) {
		Drawable icon = getResources().getDrawable(resId);
		setIcon(icon);
	}

	public void setIcon(Drawable drawable) {
		mIconDrawable = drawable;
		mIcon.setBackgroundDrawable(drawable);
	}

	public int getTriggerPaddingTop() {
		return mTriggerPaddingTop;
	}

	public int getTriggerPaddingBottom() {
		return mTriggerPaddingBottom;
	}

	public int getTriggerPaddingLeft() {
		return mTriggerPaddingLeft;
	}

	public int getTriggerPaddingRight() {
		return mTriggerPaddingRight;
	}

	public void setTriggerPadding(int left, int top, int right, int bottom) {
		mTriggerPaddingLeft = left;
		mTriggerPaddingTop = top;
		mTriggerPaddingRight = right;
		mTriggerPaddingBottom = bottom;
		mTrigger.setPadding(left, top, right, bottom);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		SavedState ss = new SavedState(superState);
		ss.expanded = mExpanded;

		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		setExpanded(ss.expanded);
	}

	static class SavedState extends BaseSavedState {
		boolean expanded;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			this.expanded = in.readInt() == 0 ? false : true;
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(this.expanded ? 1 : 0);
		}

		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}
}
