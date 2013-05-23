
package org.whispercomm.shout.ui.widget;

import java.util.Set;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.colorstorage.ShoutBorder;
import org.whispercomm.shout.provider.ShoutColorContract;
import org.whispercomm.shout.provider.ShoutCursorAdapter;
import org.whispercomm.shout.ui.DialogFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

public class TimelineAdapter extends ShoutCursorAdapter {
	private Set<LocalShout> expandedShouts;
	private boolean hasSeen = false;

	public TimelineAdapter(Context context, Cursor c, Set<LocalShout> expandedShouts) {
		super(context, c, 0);
		this.expandedShouts = expandedShouts;
	}

	@Override
	public void bindView(View view, Context context, final LocalShout shout) {
		ShoutListViewRow row = (ShoutListViewRow) view;

		final DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		};

		if (shout.getSender().getUserCount() == 2) {
			ShoutBorder border = ShoutColorContract.getShoutBorder(context, shout.getSender());
			boolean warningStatus = border.getWarningStatus();
			if (hasSeen == false && warningStatus == false) {
				AlertDialog agreement = DialogFactory.colorCodingExplanation(context, positive);
				agreement.show();
				hasSeen = true;
				ShoutColorContract.updateShoutBorder(context, border);
			}
			if (hasSeen == true) {
				ShoutColorContract.updateShoutBorder(context, border);
			}

		}
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
