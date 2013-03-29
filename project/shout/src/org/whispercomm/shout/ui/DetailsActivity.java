
package org.whispercomm.shout.ui;

import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import com.actionbarsherlock.view.MenuItem;

public class DetailsActivity extends AbstractShoutViewActivity {
	@SuppressWarnings("unused")
	private static final String TAG = DetailsActivity.class.getSimpleName();

	public static final String SHOUT_ID = "shout_id";

	/**
	 * Starts the details activity to display the specified shout.
	 * 
	 * @param context the context used to start the activity
	 * @param hash the shout to display
	 */
	public static void show(Context context, Shout shout) {
		Intent intent = new Intent(context, DetailsActivity.class);
		intent.putExtra(SHOUT_ID, shout.getHash());
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_details);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				Intent upIntent = new Intent(this, ShoutActivity.class);
				if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
					// This activity is not part of the application's task, so
					// create a new task
					// with a synthesized back stack.
					TaskStackBuilder.from(this)
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
