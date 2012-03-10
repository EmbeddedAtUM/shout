package org.whispercomm.shout;

import java.util.ArrayList;

import org.joda.time.DateTime;

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
import android.widget.TextView;

public class ShoutActivity extends ListActivity {
	
	private static final String TAG = "ShoutActivity";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ArrayList<Shout> shouts = new ArrayList<Shout>();
		for (int i = 0; i < 15; i++) {
			shouts.add(new Shout("Item " + i, DateTime.now()));
		}

		TimelineAdapter adapter = new TimelineAdapter(
				this.getApplicationContext(), shouts);
		setListAdapter(adapter);

		Log.v(TAG, "Finished onCreate");
    }
    
	static class ViewHolder {
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
				viewHolder.message = (TextView) rowView
						.findViewById(R.id.message);
				viewHolder.age = (TextView) rowView.findViewById(R.id.age);

				rowView.setTag(viewHolder);

				Log.v(TAG, "View inflated");
			}

			// Gets the view information
			ViewHolder holder = (ViewHolder) rowView.getTag();

			// Creates the message string
			long timePassed;
			String unit;
			DateTime time = shout.getTimePassed();
			if (time.isAfter(60 * 1000)) {
				timePassed = shout.getTimePassed().getMinuteOfHour();
				unit = "minute";
			} else if (time.isAfter(60 * 60 * 1000)) {
				timePassed = shout.getTimePassed().getHourOfDay();
				unit = "hour";
			} else if (time.isAfter(60 * 60 * 24 * 1000)) {
				timePassed = shout.getTimePassed().getDayOfWeek();
				unit = "day";
			} else {
				timePassed = shout.getTimePassed().getSecondOfMinute();
				unit = "second";
			}
				
			String ageText = String.format("Message received %d %s%s ago.",
					timePassed, unit, timePassed == 1 ? "" : "s");

			Log.v(TAG, ageText);
			holder.age.setText(ageText);
			holder.message.setText(shout.getContent());
			Log.v(TAG, "Textview text set");

			if (shout.id % 2 == 0)
				rowView.setBackgroundColor(0xFFA8FFFF);
			else
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