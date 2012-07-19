
package org.whispercomm.shout;

import org.whispercomm.manes.client.maclib.ManesActivityHelper;
import org.whispercomm.shout.customwidgets.DialogFactory;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.network.ErrorCode;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.tasks.AsyncTaskCallback.AsyncTaskCompleteListener;
import org.whispercomm.shout.tasks.CommentTask;
import org.whispercomm.shout.tasks.SendShoutTask;
import org.whispercomm.shout.tasks.ShoutTask;
import org.whispercomm.shout.terms.AgreementListener;
import org.whispercomm.shout.terms.AgreementManager;
import org.whispercomm.shout.thirdparty.Utf8ByteLengthFilter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message);

		AgreementManager.getConsent(this, new AgreementListener() {

			@Override
			public void accepted() {
				initialize();
			}

			@Override
			public void declined() {
				finish();
			}

		});

	}

	private void initialize() {
		initializeViews();
		parent = getParent(getIntent().getExtras());
		idManager = new IdManager(getApplicationContext());
		network = new NetworkInterface(MessageActivity.this);
	}

	private void initializeViews() {
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
		if (network != null) {
			network.unbind();
		}
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

	private void shoutSent(ErrorCode result) {
		switch (result) {
			case SUCCESS:
				Toast.makeText(this, R.string.send_shout_success, Toast.LENGTH_SHORT)
						.show();
				finish();
				break;
			case MANES_NOT_INSTALLED:
				DialogFactory.buildInstallationPromptDialog(this,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ManesActivityHelper.launchManesInstallation(MessageActivity.this);
								finish();
							}
						}).show();
				break;
			case MANES_NOT_REGISTERED:
				DialogFactory.buildRegistrationPromptDialog(this,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ManesActivityHelper
										.launchRegistrationActivity(MessageActivity.this);
								finish();
							}
						}).show();
				break;
			case IO_ERROR:
				Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
						.show();
				finish();
				break;
			case SHOUT_CHAIN_TOO_LONG:
				Log.e(TAG, "SHOUT_CHAIN_TOO_LONG error.  Unable to send shout.");
				Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
						.show();
				finish();
				break;
			default:
				finish();
				break;
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
			AsyncTaskCompleteListener<ErrorCode> {
		@Override
		public void onComplete(ErrorCode result) {
			shoutSent(result);
		}
	}
}
