
package org.whispercomm.shout;

import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.tasks.ReshoutTask;

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
import android.widget.ListView;
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "Click at position " + position + ", id " + id);
		ViewHolder holder = (ViewHolder) v.getTag();
		holder.buttonHolder.setVisibility(View.VISIBLE);
		holder.expanded = true;
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

	public void onClickReshout(View v) {
		Log.v(TAG, "Reshout button clicked");
		// Hack to get shout ID
		ViewGroup rowView = (ViewGroup) v.getParent().getParent();
		ViewHolder holder = (ViewHolder) rowView.getTag();
		int id = holder.id;
		Log.v(TAG, "Shout ID received as " + id);
		// Handle the reshout
		ReshoutTask reshoutTask = new ReshoutTask(getApplicationContext());
		reshoutTask.execute(id);
	}

	public void onClickComment(View v) {
		Log.v(TAG, "Comment button clicked");
		ViewGroup rowView = (ViewGroup) v.getParent().getParent();
		ViewHolder holder = (ViewHolder) rowView.getTag();
		int id = holder.id;
		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra(MessageActivity.PARENT_ID, id);
		startActivity(intent);
	}

	public void onClickDetails(View v) {
		Log.v(TAG, "Details buttons clicked");
		ViewGroup rowView = (ViewGroup) v.getParent().getParent();
		ViewHolder holder = (ViewHolder) rowView.getTag();
		int id = holder.id;
		Log.v(TAG, "Shout ID received as " + id);
		Intent intent = new Intent(this, DetailsActivity.class);
		intent.putExtra(DetailsActivity.SHOUT_ID, id);
		startActivity(intent);
	}

	static class ViewHolder {
		ImageView avatar;
		TextView origSender;
		TextView sender;
		TextView message;
		TextView age;
		ViewGroup buttonHolder;
		boolean expanded = false;
		int id = -1;
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

			// Find the views
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.id = id;
			holder.buttonHolder.setVisibility(View.GONE);

			// Set the Shout data in the views
			holder.avatar.setImageResource(R.drawable.defaultavatar);
			holder.sender.setText(shout.getSender().getUsername());
			holder.age.setText(ShoutMessageUtility.getDateTimeAge(shout.getTimestamp()));
			ShoutType type = ShoutMessageUtility.getShoutType(shout);
			switch (type) {
				case SHOUT:
					holder.message.setText(shout.getMessage());
					holder.origSender.setText(shout.getSender().getUsername());
					break;
				case RESHOUT:
					holder.message.setText(shout.getParent().getMessage());
					holder.origSender.setText(shout.getParent().getSender().getUsername());
					break;
				case COMMENT:
					holder.message.setText(shout.getMessage());
					holder.origSender.setText("comment"); // FIXME
					break;
				case RECOMMENT:
					holder.message.setText(shout.getParent().getMessage());
					holder.origSender.setText(shout.getParent().getSender().getUsername());
					break;
				default:
					throw new IllegalStateException("Did not get valid ShoutType");
			}
			return;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View rowView = inflater.inflate(R.layout.row, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.avatar = (ImageView) rowView.findViewById(R.id.avatar);
			holder.origSender = (TextView) rowView.findViewById(R.id.origsender);
			holder.sender = (TextView) rowView.findViewById(R.id.sender);
			holder.age = (TextView) rowView.findViewById(R.id.age);
			holder.message = (TextView) rowView.findViewById(R.id.message);
			holder.buttonHolder = (ViewGroup) rowView.findViewById(R.id.buttonHolder);
			rowView.setTag(holder);
			Log.v(TAG, "View inflated");
			return rowView;
		}
	}
}
