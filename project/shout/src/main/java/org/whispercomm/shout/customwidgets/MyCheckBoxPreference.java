package org.whispercomm.shout.customwidgets;

import android.content.Context;
import android.graphics.Color;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyCheckBoxPreference extends CheckBoxPreference {

	public MyCheckBoxPreference(Context context) {
		super(context);
	}

	public MyCheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyCheckBoxPreference(Context context, AttributeSet attrs,
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