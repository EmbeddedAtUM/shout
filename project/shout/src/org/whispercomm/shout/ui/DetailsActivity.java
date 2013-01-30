
package org.whispercomm.shout.ui;

import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DetailsActivity extends AbstractShoutActivity {
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
		setContentView(R.layout.activity_details);
	}

}
