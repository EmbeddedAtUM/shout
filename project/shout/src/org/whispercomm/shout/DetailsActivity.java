
package org.whispercomm.shout;

import org.whispercomm.shout.customwidgets.ShoutChainView;
import org.whispercomm.shout.customwidgets.ShoutView;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

// TODO Make this page not look terrible
public class DetailsActivity extends Activity {

	private static final String TAG = "DetailsActivity";
	public static final String SHOUT_ID = "shout_id";
	private LocalShout shout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		Bundle extras = getIntent().getExtras();
		shout = getShoutFromBundle(extras);
		byte[] parentHash;
		switch (shout.getType()) {
			case RESHOUT:
			case COMMENT:
				parentHash = shout.getParent().getHash();
				break;
			case RECOMMENT:
				parentHash = shout.getParent().getParent().getHash();
				break;
			default:
				parentHash = shout.getHash();
				break;
		}
		LocalShout parent = ShoutProviderContract.retrieveShoutByHash(getApplicationContext(),
				parentHash);
		ShoutView shoutView = (ShoutView) findViewById(R.id.shoutview);
		shoutView.bindShout(shout);
		shoutView.showDetails();
		ShoutChainView view = (ShoutChainView) findViewById(R.id.commentsview);
		view.bindShouts(parent);
		view.setVisibility(View.VISIBLE);
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
