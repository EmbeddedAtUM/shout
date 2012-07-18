
package org.whispercomm.shout.network;

import org.whispercomm.shout.terms.AgreementManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Starts the Shout network service on system boot, if the user has enabled it.
 * 
 * @author David R. Bild
 */
public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = BootReceiver.class.getSimpleName();

	public static final String START_SERVICE_ON_BOOT = "runInBackground";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "BOOT_COMPLETED received.");

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean startOnBoot = settings.getBoolean(START_SERVICE_ON_BOOT, true);

		boolean hasAgreed = AgreementManager.hasAgreed(context);

		if (startOnBoot && hasAgreed) {
			Log.i(TAG, "Starting Shout network service.");
			Intent service = new Intent(context, NetworkService.class);
			context.startService(service);
		} else {
			Log.i(TAG,
					"Not starting Shout network service because user has disabled autostart.");
		}

	}

}
