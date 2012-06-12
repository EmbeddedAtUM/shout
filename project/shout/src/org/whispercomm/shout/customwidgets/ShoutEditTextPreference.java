package org.whispercomm.shout.customwidgets;

import android.content.Context;
import android.graphics.Color;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ShoutEditTextPreference extends EditTextPreference {

	public ShoutEditTextPreference(Context context) {
		super(context);
	}

	public ShoutEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ShoutEditTextPreference(Context context, AttributeSet attrs,
			int defstyle) {
		super(context, attrs, defstyle);

	}
	
	@Override
	public void onClick() {
		super.onClick();
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