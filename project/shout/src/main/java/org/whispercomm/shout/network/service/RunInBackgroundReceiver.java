
package org.whispercomm.shout.network.service;

import org.whispercomm.shout.terms.AgreementManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Starts the Shout network service, if the user has enabled it.
 * 
 * @author David R. Bild
 */
public class RunInBackgroundReceiver extends BroadcastReceiver {
	private static final String TAG = RunInBackgroundReceiver.class.getSimpleName();

	public static final String RUN_IN_BACKGROUND = "runInBackground";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, String.format("%s received.", intent.getAction()));

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean startOnBoot = settings.getBoolean(RUN_IN_BACKGROUND, true);

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
