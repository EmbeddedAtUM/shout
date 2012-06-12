package org.whispercomm.shout.customwidgets;

import android.content.Context;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TwitterPreference extends Preference {

	// ImageView mIcon = null;
	// TextView mTitle = null;
	// ImageView mArrow = null;
	//
	// int mIconResId = -1;
	// int mTitleResId = -1;
	// boolean mIconAnimation = false;
	// boolean mNoArrow = false;

	public TwitterPreference(Context context) {
		super(context);
		// init(null);
	}

	public TwitterPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init(attrs);
	}

	public TwitterPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// init(attrs);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		View newView = super.onCreateView(parent);

		((TextView) newView.findViewById(android.R.id.title))
				.setTextColor(Color.BLACK);
		((TextView) newView.findViewById(android.R.id.summary))
				.setTextColor(Color.DKGRAY);

		return newView;
	}
}