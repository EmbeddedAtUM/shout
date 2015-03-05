
package org.whispercomm.shout.ui.widget;

import org.whispercomm.shout.R;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShoutAgreementView extends LinearLayout {

	TextView text;

	public ShoutAgreementView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeViews(context);
	}

	public ShoutAgreementView(Context context) {
		super(context);
		initializeViews(context);
	}

	private void initializeViews(Context context) {
		this.setClickable(false);
		text = new TextView(context);
		text.setPadding(10, 10, 10, 10); // TODO Make these display independent
		text.setText(R.string.shoutAgreement);
		text.setMovementMethod(LinkMovementMethod.getInstance());
		// TODO Figure out why the entire view lights up on clicks
		this.addView(text);

	}
}
