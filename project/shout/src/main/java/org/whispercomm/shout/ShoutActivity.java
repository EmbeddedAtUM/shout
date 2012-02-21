package org.whispercomm.shout;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
		Shout shout = new Shout("Test");
		shouts.add(shout);

		TimelineAdapter adapter = new TimelineAdapter(
				this.getApplicationContext(), shouts);
		setListAdapter(adapter);

		Log.v(TAG, "Finished onCreate");
    }
    
	private class TimelineAdapter extends ArrayAdapter<Shout> {

		ArrayList<Shout> items;

		public TimelineAdapter(Context context, ArrayList<Shout> shouts) {
			super(context, R.layout.row, R.id.message, shouts);
			this.items = shouts;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {

			Log.v(TAG, "Entered getView");
			// Inflate the view
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.row, parent, false);
				Log.v(TAG, "View inflated");
			}

			// Get current shout
			Shout shout = items.get(position);
			Log.v(TAG, "Shout: " + shout);

			// Set shout text
			if (shout != null) {
				TextView message = (TextView) view.findViewById(R.id.message);

				message.setText(shout.getContent());
				Log.v(TAG, "Textview text set");
			}
			return view;
		}

	}
}