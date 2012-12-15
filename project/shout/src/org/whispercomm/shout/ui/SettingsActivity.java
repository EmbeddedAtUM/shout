
package org.whispercomm.shout.ui;

import org.whispercomm.shout.R;
import org.whispercomm.shout.network.service.NetworkService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity {
	private static final String TAG = SettingsActivity.class.getSimpleName();

	private static final int DIALOG_RUN_IN_BACKGROUND_WARNING_ID = 0;

	/**
	 * Starts the settings activity.
	 * 
	 * @param context the context used to start the activity
	 */
	public static void show(Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		context.startActivity(intent);
	}

	private Intent networkServiceIntent;
	private RunInBackgroundListener runInBackgroundListener;
	private CheckBoxPreference runInBackgroundPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		addPreferencesFromResource(R.xml.preferences);

		networkServiceIntent = new Intent(this, NetworkService.class);

		// Configure service preference change listener
		runInBackgroundListener = new RunInBackgroundListener();
		runInBackgroundPref = (CheckBoxPreference) findPreference("runInBackground");
		runInBackgroundPref
				.setOnPreferenceChangeListener(runInBackgroundListener);

		CheckBoxPreference notificationsPref = (CheckBoxPreference) findPreference("show_notifications");
		setPreferenceEnabled(notificationsPref.isChecked());
		notificationsPref.setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						setPreferenceEnabled((Boolean) newValue);
						return true;
					}
				});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void setPreferenceEnabled(Boolean bool) {
		Preference ledPref = findPreference("notification_led");
		Preference soundPref = findPreference("notification_sound");

		if (bool) {
			ledPref.setEnabled(true);
			soundPref.setEnabled(true);
		} else {
			ledPref.setEnabled(false);
			soundPref.setEnabled(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent intent = new Intent(this, ShoutActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private class RunInBackgroundListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {

			Boolean value = (Boolean) newValue;
			if (value) {
				SettingsActivity.this.startService(networkServiceIntent);
			} else {
				showDialog(DIALOG_RUN_IN_BACKGROUND_WARNING_ID);
				SettingsActivity.this.stopService(networkServiceIntent);
			}
			return true;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_RUN_IN_BACKGROUND_WARNING_ID) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle(R.string.settings_runinbackground_warning_title)
					.setIcon(R.drawable.icon)
					.setMessage(R.string.settings_runinbackground_warning_message)
					.setCancelable(false)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Do nothing.
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							runInBackgroundPref.setChecked(true);
							/*
							 * onPreferenceChangeListener() is not called, so
							 * enable service here.
							 */
							SettingsActivity.this.startService(networkServiceIntent);
						}
					});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}
}
