package org.whispercomm.shout;

import org.whispercomm.shout.provider.ShoutProviderContract;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
		setListAdapter(new TimelineAdapter(getApplicationContext(), cursor));
		Log.v(TAG, "Finished onCreate");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cursor.close();
		Log.v(TAG, "Finished onDestroy");
	}

	static class ViewHolder {
		ImageView avatar;
		TextView origSender;
		TextView sender;
		TextView message;
		TextView age;

	}

	private class TimelineAdapter extends CursorAdapter {

		private int count;

		public TimelineAdapter(Context context, Cursor c) {
			super(context, c);
			count = 0;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();
			int idIndex = cursor
					.getColumnIndex(ShoutProviderContract.Shouts._ID);
			int id = cursor.getInt(idIndex);
			Shout shout = ShoutProviderContract.retrieveShoutById(context, id);

			holder.avatar.setImageResource(R.drawable.defaultavatar);
			holder.origSender.setText(shout.getSender().getUsername());
			holder.sender.setText(shout.getSender().getUsername());
			holder.age
					.setText(DateTimeConvert.dtToString(shout.getTimestamp()));
			holder.message.setText(shout.getMessage());

			Log.v(TAG, "View " + id + " set");
			return;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View rowView = inflater.inflate(R.layout.row, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.avatar = (ImageView) rowView.findViewById(R.id.avatar);
			holder.origSender = (TextView) rowView
					.findViewById(R.id.origsender);
			holder.sender = (TextView) rowView.findViewById(R.id.sender);
			holder.message = (TextView) rowView.findViewById(R.id.message);
			holder.age = (TextView) rowView.findViewById(R.id.age);

			rowView.setTag(holder);
			if (count % 2 == 0) {
				rowView.setBackgroundColor(0xFFD2F7FF);
			} else {
				rowView.setBackgroundColor(0XFFFFFFFF);
			}

			count++;
			Log.v(TAG, "View inflated");
			return rowView;
		}

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
}