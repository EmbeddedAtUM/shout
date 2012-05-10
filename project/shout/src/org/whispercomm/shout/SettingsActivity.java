package org.whispercomm.shout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {

	public static final String TAG = "SettingsActivity";

	boolean autoRotatePreference;
	String usernamePreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		addPreferencesFromResource(R.xml.preferences);
		
		// Setup Twitter Login preference click listener
		Preference twitterLoginPref = (Preference) findPreference("twitterLoginPref");
		twitterLoginPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
						Log.v(TAG, "twitterLoginPref clicked");

						startActivity(new Intent(preference.getContext(),
								LoginActivity.class));

						return true;
            }
        });

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
}