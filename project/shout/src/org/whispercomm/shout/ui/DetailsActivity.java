
package org.whispercomm.shout.ui;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.ui.widget.ShoutChainView;
import org.whispercomm.shout.ui.widget.ShoutView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class DetailsActivity extends AbstractShoutViewActivity {
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

	private LocalShout shout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		Bundle extras = getIntent().getExtras();
		shout = getShoutFromBundle(extras);
		switch (shout.getType()) {
			case SHOUT:
				ShoutChainView view = (ShoutChainView) findViewById(R.id.commentsview);
				view.bindShouts(shout);
				view.setVisibility(View.VISIBLE);
				break;
			default:
				break;

		}
		ShoutView shoutView = (ShoutView) findViewById(R.id.shoutview);
		shoutView.bindShout(shout);
		shoutView.showDetails();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private LocalShout getShoutFromBundle(Bundle bundle) {
		if (bundle == null) {
			return null;
		}
		byte[] hash = bundle.getByteArray(SHOUT_ID);
		if (hash == null) {
			return null;
		}
		LocalShout shout = ShoutProviderContract.retrieveShoutByHash(getApplicationContext(), hash);
		return shout;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "Finished onDestroy");
	}
}
