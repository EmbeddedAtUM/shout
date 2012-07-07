package org.whispercomm.shout;

import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.tasks.AsyncTaskCallback.AsyncTaskCompleteListener;
import org.whispercomm.shout.tasks.CommentTask;
import org.whispercomm.shout.tasks.SendShoutTask;
import org.whispercomm.shout.tasks.ShoutTask;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MessageActivity extends Activity {

	public static final String TAG = "MessageActivity";
	public static final String PARENT_ID = "parent";

	private Toast noUserToast;
	private IdManager idManager;

	private CheckBox chkTweet;
	private Button btnSend;
	private EditText edtMessage;
	private FrameLayout frmProgressBar;

	private LocalShout parent = null;

	@SuppressLint("ShowToast")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message);
		idManager = new IdManager(getApplicationContext());

		chkTweet = (CheckBox) findViewById(R.id.tweet);
		btnSend = (Button) findViewById(R.id.send);
		edtMessage = (EditText) findViewById(R.id.compose);
		frmProgressBar = (FrameLayout) findViewById(R.id.frmProgressBar);

		noUserToast = Toast.makeText(getApplicationContext(),
				"Set up a user before you Shout!", Toast.LENGTH_LONG);
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		int parentId = extras.getInt(PARENT_ID, -1);
		if (parentId > 0) {
			parent = ShoutProviderContract.retrieveShoutById(
					getApplicationContext(), parentId);
			ShoutType type = ShoutMessageUtility.getShoutType(parent);
			switch (type) {
			case COMMENT:
			case RESHOUT:
				parent = parent.getParent();
				parentId = ShoutProviderContract.storeShout(
						getApplicationContext(), parent);
				Log.v(TAG,
						"Parent was a comment/reshout, resetting parent to grandparent");
				break;
			case RECOMMENT:
				parent = parent.getParent().getParent();
				parentId = ShoutProviderContract.storeShout(
						getApplicationContext(), parent);
				Log.v(TAG,
						"Parent was a recomment, resetting parent to great grandparent");
				break;
			default:
				break;
			}
			Log.v(TAG, "Parent text received as: " + parent.getMessage());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (idManager.userIsNotSet()) {
			promptForUsername();
			finish();
		}
	}

	private void promptForUsername() {
		noUserToast.show();
		startActivity(new Intent(this, SettingsActivity.class));
	}

	private void showProgressBar() {
		frmProgressBar.setVisibility(FrameLayout.VISIBLE);

		chkTweet.setEnabled(false);
		btnSend.setEnabled(false);
	}

	private void hideProgressBar() {
		frmProgressBar.setVisibility(FrameLayout.GONE);

		chkTweet.setEnabled(true);
		btnSend.setEnabled(true);
	}

	public void onClickSend(View v) {
		Log.v(TAG, "Send button clicked");
		showProgressBar();
		String content = edtMessage.getText().toString();
		Log.v(TAG, "Shout text received as: " + content);
		try {
			if (parent == null) {
				Log.v(TAG, "Creating a new shout...");
				new ShoutTask(getApplicationContext(),
						new ShoutCreationCompleteListener(), idManager.getMe())
						.execute(content);
			} else {
				Log.v(TAG, "Commenting on another shout...");
				new CommentTask(getApplicationContext(),
						new ShoutCreationCompleteListener(), idManager.getMe(),
						parent).execute(content);
			}
		} catch (UserNotInitiatedException e) {
			promptForUsername();
			hideProgressBar();
		}
	}

	private void shoutCreated(LocalShout result) {
		if (result != null) {
			new SendShoutTask(this, new ShoutSendCompleteListener())
					.execute(result);
			finish();
		} else {
			Toast.makeText(this, R.string.create_shout_failure,
					Toast.LENGTH_LONG).show();
			hideProgressBar();
		}
	}

	private void shoutSent(boolean success) {
		if (success) {
			Toast.makeText(this, R.string.send_shout_success,
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
		}
		finish();
	}

	private class ShoutCreationCompleteListener implements
			AsyncTaskCompleteListener<LocalShout> {
		@Override
		public void onComplete(LocalShout result) {
			shoutCreated(result);
		}
	}

	private class ShoutSendCompleteListener implements
			AsyncTaskCompleteListener<Boolean> {
		@Override
		public void onComplete(Boolean result) {
			shoutSent(result);
		}
	}
}
