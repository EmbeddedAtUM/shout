
package org.whispercomm.shout;

import org.whispercomm.shout.customwidgets.ShoutListViewRow;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.network.BootReceiver;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.network.NetworkService;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.tasks.ReshoutTask;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Toast;

public class ShoutActivity extends ListActivity {

	private static final String TAG = "ShoutActivity";

	private NetworkInterface network;
	private IdManager idManager;
	private Cursor cursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		startBackgroundService();

		this.network = new NetworkInterface(this);
		this.idManager = new IdManager(this);
		this.cursor = ShoutProviderContract
				.getCursorOverAllShouts(getApplicationContext());
		setListAdapter(new TimelineAdapter(this, cursor));
		Log.v(TAG, "Finished onCreate");
	}

	/**
	 * Ensures that the background Shout service is started, if the user has
	 * that option enabled.
	 */
	private void startBackgroundService() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean runInBackground = prefs.getBoolean(
				BootReceiver.START_SERVICE_ON_BOOT, true);
		if (runInBackground) {
			Intent intent = new Intent(this, NetworkService.class);
			this.startService(intent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		network.unbind();
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
		displaySettings();
	}

	public void onClickReshout(LocalShout shout) {
		Log.v(TAG, "Reshout button clicked");

		ReshoutTask task;
		try {
			task = new ReshoutTask(getApplicationContext(), network,
					idManager.getMe());
			task.execute(shout);
		} catch (UserNotInitiatedException e) {
			Toast.makeText(this, "Please set a username before shouting.",
					Toast.LENGTH_LONG).show();
			displaySettings();
		}

	}

	private void displaySettings() {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	public void onClickComment(LocalShout shout) {
		Log.v(TAG, "Comment button clicked");

		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra(MessageActivity.PARENT_ID, shout.getHash());
		startActivity(intent);
	}

	public void onClickDetails(LocalShout shout) {
		Log.v(TAG, "Details buttons clicked");

		Intent intent = new Intent(this, DetailsActivity.class);
		intent.putExtra(DetailsActivity.SHOUT_ID, shout.getHash());
		startActivity(intent);
	}

	private class TimelineAdapter extends CursorAdapter {

		public TimelineAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ShoutListViewRow row = (ShoutListViewRow) view;

			// Get the shout
			LocalShout shout = ShoutProviderContract.retrieveShoutFromCursor(
					context, cursor);

			row.bindShout(shout);
		}

		@Override
		public View newView(final Context context, Cursor cursor,
				ViewGroup parent) {
			return new ShoutListViewRow(context);
		}
	}
}
