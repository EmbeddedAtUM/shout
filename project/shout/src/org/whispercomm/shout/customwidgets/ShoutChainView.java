
package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.database.Cursor;
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
		Cursor cursor = ShoutProviderContract.getComments(getContext(), parent);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			LocalShout comment = ShoutProviderContract.retrieveShoutFromCursor(
					getContext(), cursor);
			this.addView(createChild(comment));
			cursor.moveToNext();
		}
		cursor.close();

		this.loaded = true;

	}

	private View createChild(LocalShout shout) {
		ShoutChainViewRow row = new ShoutChainViewRow(getContext());
		row.bindShout(shout);
		return row;
	}

}
