
package org.whispercomm.shout.ui.widget;

import java.util.Set;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.provider.ShoutCursorAdapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

public class TimelineAdapter extends ShoutCursorAdapter {

	private Set<LocalShout> expandedShouts;

	public TimelineAdapter(Context context, Cursor c, Set<LocalShout> expandedShouts) {
		super(context, c, 0);
		this.expandedShouts = expandedShouts;
	}

	@Override
	public void bindView(View view, Context context, final LocalShout shout) {
		ShoutListViewRow row = (ShoutListViewRow) view;

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
	public View newView(final Context context, LocalShout shout,
			ViewGroup parent) {
		return new ShoutListViewRow(context);
	}

}
