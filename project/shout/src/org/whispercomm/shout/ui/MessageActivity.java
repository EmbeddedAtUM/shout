
package org.whispercomm.shout.ui;

import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ui.fragment.MessageFragment;
import org.whispercomm.shout.ui.fragment.MessageFragment.MessageFragmentContainer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

public class MessageActivity extends AbstractShoutActivity implements MessageFragmentContainer {
	@SuppressWarnings("unused")
	private static final String TAG = MessageActivity.class.getSimpleName();

	/**
	 * Starts the message activity to send a new original shout.
	 * 
	 * @param context the context used to start the activity
	 */

	public static void shout(Context context) {
		comment(context, null);
	}

	/**
	 * Starts the message activity to send a new comment on the specified shout.
	 * 
	 * @param context the context used to start the activity
	 * @param shout the shout on which to comment
	 */

	public static void shout(Context context, String photoPath) {
		Intent intent = new Intent(context, MessageActivity.class);
		intent.putExtra(MessageFragment.PASS_PHOTO, photoPath);
		context.startActivity(intent);
	}

	public static void comment(Context context, Shout shout) {
		Intent intent = new Intent(context, MessageActivity.class);
		if (shout != null) {
			intent.putExtra(MessageFragment.PARENT_ID, shout.getHash());
		}
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_message);
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
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void messageFragmentFinished() {
		finish();
	}
}
