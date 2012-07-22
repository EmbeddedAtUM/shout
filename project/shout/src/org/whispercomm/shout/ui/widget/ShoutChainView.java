
package org.whispercomm.shout.ui.widget;

import java.util.List;

import org.whispercomm.shout.LocalShout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class ShoutChainView extends LinearLayout {

	private boolean loaded;
	private LocalShout parent;

	public ShoutChainView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ShoutChainView(Context context) {
		super(context);
	}

	@Override
	public void setVisibility(int visibility) {
		if (visibility == VISIBLE && !loaded) {
			loadShouts();
		}
		super.setVisibility(visibility);
	}

	public void bindShouts(LocalShout parent) {
		this.removeAllViews();
		this.loaded = false;
		this.parent = parent;
	}

	private void loadShouts() {
		List<LocalShout> comments = parent.getComments();
		for (LocalShout comment : comments) {
			this.addView(createChild(comment));
		}

		this.loaded = true;

	}

	private View createChild(LocalShout shout) {
		ShoutChainViewRow row = new ShoutChainViewRow(getContext());
		row.bindShout(shout);
		return row;
	}

}
