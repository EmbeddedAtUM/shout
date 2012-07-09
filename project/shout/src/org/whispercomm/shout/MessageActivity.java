package org.whispercomm.shout;

import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.tasks.AsyncTaskCallback.AsyncTaskCompleteListener;
import org.whispercomm.shout.tasks.CommentTask;
import org.whispercomm.shout.tasks.SendShoutTask;
import org.whispercomm.shout.tasks.ShoutTask;
import org.whispercomm.shout.thirdparty.Utf8ByteLengthFilter;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
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

	private NetworkInterface network;

	private IdManager idManager;

	private Button btnSend;
	private EditText edtMessage;
	private FrameLayout frmProgressBar;

	private LocalShout parent = null;

	@SuppressLint("ShowToast")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message);

		network = new NetworkInterface(this);
		idManager = new IdManager(getApplicationContext());

		btnSend = (Button) findViewById(R.id.send);
		edtMessage = (EditText) findViewById(R.id.compose);
		frmProgressBar = (FrameLayout) findViewById(R.id.frmProgressBar);

		edtMessage.setFilters(new InputFilter[] { new Utf8ByteLengthFilter(
				SerializeUtility.MAX_MESSAGE_SIZE) });

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
				break;
			case RECOMMENT:
				parent = parent.getParent().getParent();
				parentId = ShoutProviderContract.storeShout(
						getApplicationContext(), parent);
				break;
			default:
				break;
			}
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

	@Override
	public void onDestroy() {
		network.unbind();
		super.onDestroy();
	}

	private void promptForUsername() {
		Toast.makeText(getApplicationContext(),
				"Set up a user before you Shout!", Toast.LENGTH_LONG).show();
		startActivity(new Intent(this, SettingsActivity.class));
	}

	private void showProgressBar() {
		frmProgressBar.setVisibility(FrameLayout.VISIBLE);
		btnSend.setEnabled(false);
	}

	private void hideProgressBar() {
		frmProgressBar.setVisibility(FrameLayout.GONE);
		btnSend.setEnabled(true);
	}

	public void onClickSend(View v) {
		showProgressBar();
		String content = edtMessage.getText().toString();
		try {
			if (parent == null) {
				new ShoutTask(getApplicationContext(),
						new ShoutCreationCompleteListener(), idManager.getMe())
						.execute(content);
			} else {
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
			new SendShoutTask(network, new ShoutSendCompleteListener())
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
