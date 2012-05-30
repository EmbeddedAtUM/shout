package org.whispercomm.shout;

import java.security.SecureRandom;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.whispercomm.shout.id.SignatureUtility;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ShoutActivity extends ListActivity {
	
	private static final String TAG = "ShoutActivity";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SignatureUtility sigU = new SignatureUtility(this);
		try {
			sigU.updateUserName("Tony");
		} catch (Exception e) {
			//TODO: auto-generated catch
			Log.v(TAG, "Error setting up Signature Utility");
		}
		
		ArrayList<Shout> shouts = new ArrayList<Shout>();
		
		byte[] arr = new byte[10];
		SecureRandom rand = new SecureRandom();
		SimpleShout s = null;
		
		for (int i = 0; i < 15; i++) {
			rand.nextBytes(arr);
			try {
				s = new SimpleShout(DateTime.now(), sigU.getUser(), "Hello Shout", null, arr);
			} catch (Exception e) {
				//TODO: auto-generated catch
				Log.v(TAG, "Error creating SimpleShout #" + i);
			}
			shouts.add(s);
		}

		TimelineAdapter adapter = new TimelineAdapter(this.getApplicationContext(), shouts);
		setListAdapter(adapter);

		Log.v(TAG, "Finished onCreate");
    }
    
	static class ViewHolder {
		ImageView avatar;
		TextView origSender;
		TextView sender;
		TextView message;
		TextView age;
	}

	private class TimelineAdapter extends ArrayAdapter<Shout> {

		ArrayList<Shout> items;

		public TimelineAdapter(Context context, ArrayList<Shout> shouts) {
			super(context, R.layout.row, R.id.message, shouts);
			this.items = shouts;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get current shout
			Shout shout = items.get(position);
			Log.v(TAG, "Shout: " + shout);

			View rowView = convertView;
			Log.v(TAG, "Entered getView");
			// Inflate the view
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.row, parent, false);

				ViewHolder viewHolder = new ViewHolder();
				viewHolder.avatar = (ImageView) rowView.findViewById(R.id.avatar);
				viewHolder.origSender = (TextView) rowView.findViewById(R.id.origsender);
				viewHolder.sender = (TextView) rowView.findViewById(R.id.sender);
				viewHolder.message = (TextView) rowView.findViewById(R.id.message);
				viewHolder.age = (TextView) rowView.findViewById(R.id.age);

				rowView.setTag(viewHolder);

				Log.v(TAG, "View inflated");
			}

			// Gets the view information
			ViewHolder holder = (ViewHolder) rowView.getTag();

			DateTime ageText = shout.getTimestamp();
			
			holder.avatar.setImageResource(R.drawable.defaultavatar);
			holder.origSender.setText(shout.getSender().getUsername());
			holder.sender.setText(shout.getSender().getUsername());
			holder.age.setText(DateTimeConvert.dtToString(ageText));
			holder.message.setText(shout.getMessage());
			Log.v(TAG, "Textview text set");

			// TODO: Color will always alternate, but white should never be the
			// first color (bug was seen when starting this Activity from
			// another)
			// TODO: Shout doesn't have a .id member so alternating colors doesn't work
			/*
			if (shout.id % 2 == 0)
				rowView.setBackgroundColor(0xFFD2F7FF);
			else
				rowView.setBackgroundColor(0xFFFFFFFF);
			*/
			rowView.setBackgroundColor(0xFFFFFFFF);
			
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