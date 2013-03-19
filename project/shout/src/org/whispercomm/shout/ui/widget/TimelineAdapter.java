
package org.whispercomm.shout.ui.widget;

import java.util.Set;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

public class TimelineAdapter extends CursorAdapter {

	private Set<LocalShout> expandedShouts;

	public TimelineAdapter(Context context, Cursor c, Set<LocalShout> expandedShouts) {
		super(context, c, 0);
		this.expandedShouts = expandedShouts;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ShoutListViewRow row = (ShoutListViewRow) view;

		// Get the shout
		final LocalShout shout = ShoutProviderContract.retrieveShoutFromCursor(
				context, cursor);

		row.clearExpandedStateChangeListeners();
		row.registerExpandedStateChangeListener(new ShoutListViewRow.ExpandedStateChangeListener() {
			@Override
			public void stateChanged(boolean expanded) {
				if (expanded) {
					expandedShouts.add(shout);
				} else {
					expandedShouts.remove(shout);
				}
			}
		});

		row.bindShout(shout, expandedShouts.contains(shout));
	}

	@Override
	public View newView(final Context context, Cursor cursor,
			ViewGroup parent) {
		return new ShoutListViewRow(context);
	}

}
