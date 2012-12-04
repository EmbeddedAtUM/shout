
package org.whispercomm.shout.ui.preference;

import org.whispercomm.shout.R;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

// TODO Someone please get rid of this class. I couldn't find another way to curve the top preference category
public class ShoutPreferenceCategory extends PreferenceCategory {

	public ShoutPreferenceCategory(Context context) {
		super(context);
	}

	public ShoutPreferenceCategory(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ShoutPreferenceCategory(Context context, AttributeSet attrs,
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
