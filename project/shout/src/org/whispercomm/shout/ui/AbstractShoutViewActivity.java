
package org.whispercomm.shout.ui;

import java.io.IOException;

import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.network.service.NetworkInterface.NotConnectedException;
import org.whispercomm.shout.network.shout.ShoutChainTooLongException;
import org.whispercomm.shout.tasks.AsyncTaskCallback.AsyncTaskCompleteListener;
import org.whispercomm.shout.tasks.ReshoutTask;
import org.whispercomm.shout.tasks.SendResult;
import org.whispercomm.shout.tasks.SendShoutTask;

import android.util.Log;
import android.widget.Toast;

public class AbstractShoutViewActivity extends AbstractShoutActivity {

	private static final String TAG = AbstractShoutViewActivity.class.getSimpleName();

	private IdManager idManager;

	protected void initialize() {
		super.initialize();
		idManager = new IdManager(this);
	}

	public void onClickReshout(LocalShout shout, AsyncTaskCompleteListener<LocalShout> listener) {
		try {
			new ReshoutTask(getApplicationContext(), listener,
					idManager.getMe(), shout).execute();
		} catch (UserNotInitiatedException e) {
			Toast.makeText(this, "Please set a username before shouting.",
					Toast.LENGTH_LONG).show();
			SettingsActivity.show(this);
		}
	}

	public void onClickComment(LocalShout shout) {
		MessageActivity.comment(this, shout);
	}

	public void onClickDetails(LocalShout shout) {
		DetailsActivity.show(this, shout);
	}

	private void shoutCreated(LocalShout result) {
		if (result != null) {
			new SendShoutTask(network, new ShoutSendCompleteListener())
					.execute(result);
		} else {
			Toast.makeText(this, R.string.create_shout_failure,
					Toast.LENGTH_LONG).show();
		}
	}

	private void shoutSent(SendResult result) {
		try {
			result.getResultOrThrow();
			Toast.makeText(this, R.string.send_shout_success, Toast.LENGTH_SHORT)
					.show();
		} catch (NotConnectedException e) {
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
		} catch (ShoutChainTooLongException e) {
			Log.e(TAG, "SHOUT_CHAIN_TOO_LONG error.  Unable to send shout.");
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
		} catch (ManesNotInstalledException e) {
			this.promptForInstallation();
		} catch (ManesNotRegisteredException e) {
			this.promptForRegistration();
		} catch (IOException e) {
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
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
