
package org.whispercomm.shout.ui;

import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import com.actionbarsherlock.view.MenuItem;

public class MapActivity extends AbstractShoutViewActivity {
	@SuppressWarnings("unused")
	private static final String TAG = MapActivity.class.getSimpleName();

	public static final String SHOUT_ID = "shout_id";

	/**
	 * Starts the map activity to display the location of the specified and
	 * related shouts.
	 * 
	 * @param context the context used to start the activity
	 * @param hash the shout to display
	 */
	public static void show(Context context, Shout shout) {
		Intent intent = new Intent(context, MapActivity.class);
		intent.putExtra(SHOUT_ID, shout.getHash());
		context.startActivity(intent);
	}

	private byte[] hash;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_map);
		hash = getIntent().getExtras().getByteArray(SHOUT_ID);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				Intent upIntent = new Intent(this, DetailsActivity.class);
				upIntent.putExtra(DetailsActivity.SHOUT_ID, hash);
				if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
					// This activity is not part of the application's task, so
					// create a new task
					// with a synthesized back stack.
					TaskStackBuilder.create(this)
							.addNextIntent(upIntent)
							.startActivities();
					finish();
				} else {
					// This activity is part of the application's task, so
					// simply
					// navigate up to the hierarchical parent activity.
					NavUtils.navigateUpTo(this, upIntent);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
