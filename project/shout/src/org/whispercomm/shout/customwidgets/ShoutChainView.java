package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class ShoutChainView extends LinearLayout {

	public ShoutChainView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ShoutChainView(Context context) {
		super(context);
	}

	public void bindShouts(Cursor cursor) {
		this.removeAllViews();

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
	}

	private View createChild(Shout shout) {
		ShoutChainViewRow row = new ShoutChainViewRow(getContext());
		row.bindShout(shout, 0, 0);
		return row;
	}

}
