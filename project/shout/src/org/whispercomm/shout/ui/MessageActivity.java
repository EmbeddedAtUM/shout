
package org.whispercomm.shout.ui;

import java.io.IOException;

import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.network.NetworkInterface.NotConnectedException;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.serialization.ShoutChainTooLongException;
import org.whispercomm.shout.tasks.AsyncTaskCallback.AsyncTaskCompleteListener;
import org.whispercomm.shout.tasks.CommentTask;
import org.whispercomm.shout.tasks.SendResult;
import org.whispercomm.shout.tasks.SendShoutTask;
import org.whispercomm.shout.tasks.ShoutTask;
import org.whispercomm.shout.thirdparty.Utf8ByteLengthFilter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MessageActivity extends AbstractShoutActivity {
	private static final String TAG = MessageActivity.class.getSimpleName();

	private static final String PARENT_ID = "parent";

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
	public static void comment(Context context, Shout shout) {
		Intent intent = new Intent(context, MessageActivity.class);
		if (shout != null) {
			intent.putExtra(PARENT_ID, shout.getHash());
		}
		context.startActivity(intent);
	}

	private IdManager idManager;

	private Button btnSend;
	private EditText edtMessage;
	private FrameLayout frmProgressBar;

	private LocalShout parent = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected void initialize() {
		super.initialize();
		initializeViews();
		parent = getParent(getIntent().getExtras());
		idManager = new IdManager(getApplicationContext());
	}

	private void initializeViews() {
		setContentView(R.layout.message);

		btnSend = (Button) findViewById(R.id.send);
		frmProgressBar = (FrameLayout) findViewById(R.id.frmProgressBar);
		edtMessage = (EditText) findViewById(R.id.compose);

		edtMessage.setFilters(new InputFilter[] {
				new Utf8ByteLengthFilter(
						SerializeUtility.MAX_MESSAGE_SIZE)
		});
	}

	private LocalShout getParent(Bundle extras) {
		if (extras == null) {
			return null;
		}

		byte[] parentHash = extras.getByteArray(PARENT_ID);
		if (parentHash == null) {
			return null;
		}

		parent = ShoutProviderContract.retrieveShoutByHash(getApplicationContext(), parentHash);
		switch (parent.getType()) {
			case COMMENT:
			case RESHOUT:
				return parent.getParent();
			case RECOMMENT:
				return parent.getParent().getParent();
			default:
				return parent;
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
		} else {
			Toast.makeText(this, R.string.create_shout_failure,
					Toast.LENGTH_LONG).show();
			hideProgressBar();
		}
	}

	private void shoutSent(SendResult result) {
		try {
			result.getResultOrThrow();
			Toast.makeText(this, R.string.send_shout_success, Toast.LENGTH_SHORT)
					.show();
			finish();
		} catch (NotConnectedException e) {
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
			finish();
		} catch (ShoutChainTooLongException e) {
			Log.e(TAG, "SHOUT_CHAIN_TOO_LONG error.  Unable to send shout.");
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
			finish();
		} catch (ManesNotInstalledException e) {
			this.promptForInstallation(false);
		} catch (ManesNotRegisteredException e) {
			this.promptForRegistration(false);
		} catch (IOException e) {
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
			finish();
		}

	}

	private class ShoutCreationCompleteListener implements
			AsyncTaskCompleteListener<LocalShout> {
		@Override
		public void onComplete(LocalShout result) {
			shoutCreated(result);
		}
	}

	private class ShoutSendCompleteListener implements
			AsyncTaskCompleteListener<SendResult> {
		@Override
		public void onComplete(SendResult result) {
			shoutSent(result);
		}
	}
}
