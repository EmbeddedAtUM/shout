
package org.whispercomm.shout.ui;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.tasks.AsyncTaskCallback.AsyncTaskCompleteListener;
import org.whispercomm.shout.ui.widget.ShoutChainView;
import org.whispercomm.shout.ui.widget.ShoutView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
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

	private LocalShout shout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void bindShout() {
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
		TextView message = (TextView) shoutView.findViewById(R.id.message);
		Linkify.addLinks(message, Linkify.ALL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// If this is a comment
		if (shout.getParent() != null) {
			getSupportMenuInflater().inflate(R.menu.activity_details_comment, menu);
		} else {
			getSupportMenuInflater().inflate(R.menu.activity_details_shout, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				Intent intent = new Intent(this, ShoutActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case R.id.menu_comment:
				onClickComment(shout);
				break;
			// case R.id.menu_reshout:
			// onClickReshout(shout);
			// break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return false;
	}

	private class ShoutCreatedListener implements AsyncTaskCompleteListener<LocalShout> {
		@Override
		public void onComplete(LocalShout result) {
			bindShout();
		}
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
		bindShout();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
