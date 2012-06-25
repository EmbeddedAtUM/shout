package org.whispercomm.shout;

import org.whispercomm.shout.customwidgets.ActionShoutView;
import org.whispercomm.shout.customwidgets.ShoutListViewRow;
import org.whispercomm.shout.provider.ShoutProvider;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.tasks.ReshoutTask;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class ShoutActivity extends ListActivity {

	private static final String TAG = "ShoutActivity";

	private Cursor cursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.cursor = ShoutProviderContract
				.getCursorOverAllShouts(getApplicationContext());
		setListAdapter(new TimelineAdapter(this, cursor));
		Log.v(TAG, "Finished onCreate");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cursor.close();
		Log.v(TAG, "Finished onDestroy");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.settings:
			intent = new Intent(this, SettingsActivity.class);
			break;
		case R.id.compose:
			intent = new Intent(this, MessageActivity.class);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		startActivity(intent);
		return true;
	}

	public void onClickShout(View v) {
		Log.v(TAG, "Shout button clicked");
		startActivity(new Intent(this, MessageActivity.class));
	}

	public void onClickSettings(View v) {
		Log.v(TAG, "Settings button clicked");
		startActivity(new Intent(this, SettingsActivity.class));
	}

	public void onClickReshout(Shout shout) {
		Log.v(TAG, "Reshout button clicked");

		// TODO: Pass around Shout objects, not local ids...
		// That will avoid the need for this hack for getting the id of an
		// already existing shout
		int id = ShoutProviderContract.storeShout(getApplicationContext(),
				shout);

		ReshoutTask task = new ReshoutTask(getApplicationContext());
		task.execute(id);
	}

	public void onClickComment(Shout shout) {
		Log.v(TAG, "Comment button clicked");

		// TODO: Pass around Shout objects, not local ids...
		// That will avoid the need for this hack for getting the id of an
		// already existing shout
		int id = ShoutProviderContract.storeShout(getApplicationContext(),
				shout);

		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra(MessageActivity.PARENT_ID, id);
		startActivity(intent);
	}

	public void onClickDetails(Shout shout) {
		Log.v(TAG, "Details buttons clicked");

		// TODO: Pass around Shout objects, not local ids...
		// That will avoid the need for this hack for getting the id of an
		// already existing shout
		int id = ShoutProviderContract.storeShout(getApplicationContext(),
				shout);

		Intent intent = new Intent(this, DetailsActivity.class);
		intent.putExtra(DetailsActivity.SHOUT_ID, id);
		startActivity(intent);
	}

	// TODO: Get rid of this. RowHolder should be it's own custom component.
	static class RowHolder {
		ShoutListViewRow rowView;
		ActionShoutView actionShoutView;
	}

	private class TimelineAdapter extends CursorAdapter {

		public TimelineAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// Get the shout
			int idIndex = cursor
					.getColumnIndex(ShoutProviderContract.Shouts._ID);
			int id = cursor.getInt(idIndex);
			Shout shout = ShoutProviderContract.retrieveShoutById(context, id);

			// Get the comment count
			int commentIndex = cursor
					.getColumnIndex(ShoutProvider.COMMENT_COUNT_COLUMN);
			int commentCount = cursor.getInt(commentIndex);

			// Get the reshout count
			int reshoutIndex = cursor
					.getColumnIndex(ShoutProvider.RESHOUT_COUNT_COLUMN);
			int reshoutCount = cursor.getInt(reshoutIndex);

			// Find the views
			RowHolder holder = (RowHolder) view.getTag();
			// TODO Cast directly to rowView, drop holder?
			// Bind the shout to the shout view
			holder.rowView.bindShout(shout, commentCount, reshoutCount);
		}

		@Override
		public View newView(final Context context, Cursor cursor,
				ViewGroup parent) {
			final RowHolder holder = new RowHolder();

			ShoutListViewRow row = new ShoutListViewRow(context);
			holder.rowView = row;
			holder.actionShoutView = (ActionShoutView) row
					.findViewById(R.id.actionshoutview);

			row.setTag(holder);
			return row;
		}
	}
}
