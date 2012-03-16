package org.whispercomm.shout.customwidgets;

import android.content.Context;
import android.graphics.Color;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyEditTextPreference extends EditTextPreference {

	public MyEditTextPreference(Context context) {
		super(context);
	}

	public MyEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyEditTextPreference(Context context, AttributeSet attrs,
			int defstyle) {
		super(context, attrs, defstyle);
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