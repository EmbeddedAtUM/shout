
package org.whispercomm.shout.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class ToggleTextView extends TextView {

	private ActionShoutView toggleView = null;

	public ToggleTextView(Context context) {
		super(context);
	}

	public ToggleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ToggleTextView(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);
	}

	public void setToggleView(ActionShoutView view) {
		toggleView = view;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP)
			if (toggleView != null) {
				toggleView.toggleActionBarVisibility();
			}
		super.onTouchEvent(event);
		return true;
	}
}
