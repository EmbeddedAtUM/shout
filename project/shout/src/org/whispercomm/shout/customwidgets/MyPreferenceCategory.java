package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.R;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class MyPreferenceCategory extends PreferenceCategory {

	public MyPreferenceCategory(Context context) {
		super(context);
	}

	public MyPreferenceCategory(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyPreferenceCategory(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		View newView = super.onCreateView(parent);

		newView.setBackgroundResource(R.drawable.preferencecategoryshape);

		return newView;

	}

}
