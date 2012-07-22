
package org.whispercomm.shout;

import org.whispercomm.shout.network.NetworkService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {

	public static final String TAG = "SettingsActivity";

	/**
	 * Starts the settings activity.
	 * 
	 * @param context the context used to start the activity
	 */
	public static void show(Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		context.startActivity(intent);
	}

	boolean autoRotatePreference;
	String usernamePreference;

	private RunInBackgroundListener runInBackgroundListener;
	private Preference runInBackgroundPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		addPreferencesFromResource(R.xml.preferences);

		// Configure service preference change listener
		runInBackgroundListener = new RunInBackgroundListener();
		runInBackgroundPref = (Preference) findPreference("runInBackground");
		runInBackgroundPref
				.setOnPreferenceChangeListener(runInBackgroundListener);

		Log.v(TAG, "Finished onCreate");
	}

	public void onStart(Bundle savedInstanceState) {
		getPrefs();
	}

	private void getPrefs() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		autoRotatePreference = prefs.getBoolean("rotatePref", true);
		usernamePreference = prefs.getString("usernamePref",
				"Nothing has been entered");
	}

	private class RunInBackgroundListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Intent intent = new Intent(SettingsActivity.this,
					NetworkService.class);

			Boolean value = (Boolean) newValue;
			if (value) {
				SettingsActivity.this.startService(intent);
			} else {
				SettingsActivity.this.stopService(intent);
			}
			return true;
		}
	}
}
