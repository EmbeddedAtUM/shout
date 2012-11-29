
package org.whispercomm.shout.ui;

import org.whispercomm.manes.client.maclib.ManesActivityHelper;
import org.whispercomm.shout.R;
import org.whispercomm.shout.expiry.ExpiryManager;
import org.whispercomm.shout.network.service.BootReceiver;
import org.whispercomm.shout.network.service.NetworkInterface;
import org.whispercomm.shout.network.service.NetworkInterface.ShoutServiceConnection;
import org.whispercomm.shout.network.service.NetworkService;
import org.whispercomm.shout.notification.NotificationSender;
import org.whispercomm.shout.terms.AgreementListener;
import org.whispercomm.shout.terms.AgreementManager;
import org.whispercomm.shout.tutorial.TutorialActivity;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

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
public class AbstractShoutActivity extends SherlockFragmentActivity {

	/** True if the activity is in the resumed state, false otherwise */
	private static boolean visible = false;

	protected NetworkInterface network;

	private final DialogInterface.OnClickListener installClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			ManesActivityHelper
					.startInstallation(AbstractShoutActivity.this);
		}
	};

	private final DialogInterface.OnClickListener registerClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			ManesActivityHelper
					.startRegistration(AbstractShoutActivity.this);
		}
	};

	private final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
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

		/*
		 * If the expiry date is passed, instruct the user to update and then
		 * quit the activity.
		 */
		if (ExpiryManager.hasExpired()) {
			ExpiryManager.buildExpirationDialog(this, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}, new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			}).show();
			return;
		}

		AgreementManager.getConsent(this, agreementListener);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case android.R.id.home:
				intent = new Intent(this, ShoutActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				break;
			case R.id.settings:
				intent = new Intent(this, SettingsActivity.class);
				break;
			case R.id.compose:
				intent = new Intent(this, MessageActivity.class);
				break;
			case R.id.help:
				intent = new Intent(this, TutorialActivity.class);
				break;
			default:
				return super.onOptionsItemSelected(item);
		}

		startActivity(intent);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		visible = true;
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NotificationSender.SHOUT_RECEIVED_NOTIFICATION_ID);
	}

	@Override
	protected void onPause() {
		super.onPause();
		visible = false;
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
						installClickListener, cancelListener).show();
			}

			@Override
			public void manesNotRegistered() {
				DialogFactory.buildRegistrationPromptDialog(AbstractShoutActivity.this,
						registerClickListener, cancelListener).show();
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
	 * @return true if any AbstractShoutActivity is visible (in the resumed
	 *         state), false otherwise
	 */
	public static boolean isVisible() {
		return visible;
	}

	/**
	 * Prompts the user to install the Manes client.
	 */
	public void promptForInstallation() {
		DialogFactory.buildInstallationPromptDialog(AbstractShoutActivity.this,
				installClickListener, cancelListener).show();
	}

	/**
	 * Prompts the user to register the Manes client.
	 */
	public void promptForRegistration() {
		DialogFactory.buildRegistrationPromptDialog(AbstractShoutActivity.this,
				registerClickListener, cancelListener).show();
	}
}
