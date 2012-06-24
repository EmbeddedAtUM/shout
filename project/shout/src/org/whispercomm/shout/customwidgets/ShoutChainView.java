package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class ShoutChainView extends LinearLayout {

	private boolean loaded;
	private int parentId;

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

	public void bindShouts(int parentId) {
		this.removeAllViews();
		this.loaded = false;
		this.parentId = parentId;

	}

	private void loadShouts() {
		Cursor cursor = ShoutProviderContract.getCursorOverShoutComments(
				getContext(), parentId);

		final int idIndex = cursor
				.getColumnIndex(ShoutProviderContract.Shouts._ID);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			int id = cursor.getInt(idIndex);
			Shout comment = ShoutProviderContract.retrieveShoutById(
					getContext(), id);
			this.addView(createChild(comment));
			cursor.moveToNext();
		}
		this.loaded = true;

	}

	private View createChild(Shout shout) {
		ShoutChainViewRow row = new ShoutChainViewRow(getContext());
		row.bindShout(shout, 0, 0);
		return row;
	}

}
