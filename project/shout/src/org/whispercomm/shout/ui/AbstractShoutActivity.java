
package org.whispercomm.shout.ui;

import org.whispercomm.manes.client.maclib.ManesActivityHelper;
import org.whispercomm.shout.network.BootReceiver;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.network.NetworkInterface.ShoutServiceConnection;
import org.whispercomm.shout.network.NetworkService;
import org.whispercomm.shout.terms.AgreementListener;
import org.whispercomm.shout.terms.AgreementManager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Base class for Shout {@code Activity Activities} that takes care of:
 * <ul>
 * <li>managing the {@link NetworkInterface} instance,</li>
 * <li>starting the background Shout service,</li>
 * <li>ensuring the user agreement is accepted and prompting if not,</li>
 * <li>prompting for Manes client installation, if needed, and</li>
 * <li>prompting for Manes client registration, if needed.</li>
 * </ul>
 * <p>
 * The {@link #initialize()} method will be invoked after the user agreement is
 * accepted (or immediately if it already was), so any initialization dependent
 * on acceptance of the terms should be done there.
 * 
 * @author David R. Bild
 */
public class AbstractShoutActivity extends Activity {

	protected NetworkInterface network;

	private final DialogInterface.OnClickListener installClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			ManesActivityHelper
					.launchManesInstallation(AbstractShoutActivity.this);
		}
	};

	private final DialogInterface.OnClickListener registerClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			ManesActivityHelper
					.launchRegistrationActivity(AbstractShoutActivity.this);
		}
	};

	private final DialogInterface.OnCancelListener finishCancelListener = new DialogInterface.OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			finish();
		}
	};

	private final DialogInterface.OnCancelListener stayCancelListener = new DialogInterface.OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			finish();
		}
	};

	private final AgreementListener agreementListener = new AgreementListener() {
		@Override
		public void accepted() {
			initialize();
		}

		@Override
		public void declined() {
			finish();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AgreementManager.getConsent(this, agreementListener);
	}

	@Override
	protected void onDestroy() {
		uninitialize();
		super.onDestroy();
	}

	/**
	 * This method will be invoked after the user agreement is accepted (or
	 * immediately if it already was). Do any initialization dependent of
	 * acceptance of those terms here.
	 * <p>
	 * N.B., this method will be invoked before your call to super.onCreate()
	 * returns.
	 * <p>
	 * Child classes must call through to the super class implementation.
	 */
	protected void initialize() {
		startBackgroundService();
		network = new NetworkInterface(this, new ShoutServiceConnection() {
			@Override
			public void manesNotInstalled() {
				DialogFactory.buildInstallationPromptDialog(AbstractShoutActivity.this,
						installClickListener, finishCancelListener).show();
			}

			@Override
			public void manesNotRegistered() {
				DialogFactory.buildRegistrationPromptDialog(AbstractShoutActivity.this,
						registerClickListener, finishCancelListener).show();
			}
		});
	}

	private void uninitialize() {
		if (network != null) {
			network.unbind();
		}
	}

	/**
	 * Ensures that the background Shout service is started, if the user has
	 * that option enabled.
	 */
	private void startBackgroundService() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean runInBackground = prefs.getBoolean(
				BootReceiver.START_SERVICE_ON_BOOT, true);
		if (runInBackground) {
			Intent intent = new Intent(this, NetworkService.class);
			this.startService(intent);
		}
	}

	/**
	 * Prompts the user to install the Manes client.
	 * 
	 * @param finish {@code true} if the activity should finish if the
	 *            registration does not succeed or {@code false} if it should
	 *            remain active.
	 */
	public void promptForInstallation(boolean finish) {
		DialogFactory.buildInstallationPromptDialog(AbstractShoutActivity.this,
				installClickListener, finish ? finishCancelListener : stayCancelListener).show();
	}

	/**
	 * Prompts the user to register the Manes client.
	 * 
	 * @param finish {@code true} if the activity should finish if the
	 *            registration does not succeed or {@code false} if it should
	 *            remain active.
	 */
	public void promptForRegistration(boolean finish) {
		DialogFactory.buildRegistrationPromptDialog(AbstractShoutActivity.this,
				registerClickListener, finish ? finishCancelListener : stayCancelListener).show();
	}
}
