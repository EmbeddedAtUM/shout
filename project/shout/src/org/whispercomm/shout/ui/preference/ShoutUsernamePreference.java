
package org.whispercomm.shout.ui.preference;

import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNameInvalidException;
import org.whispercomm.shout.ui.DialogFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

public class ShoutUsernamePreference extends EditTextPreference {

	private static final String TAG = ShoutUsernamePreference.class
			.getSimpleName();

	private IdManager idManager;
	private Context context;

	public ShoutUsernamePreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.configurePreference(context);
	}

	public ShoutUsernamePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.configurePreference(context);
	}

	public ShoutUsernamePreference(Context context) {
		super(context);
		this.configurePreference(context);
	}

	@Override
	public void setOnPreferenceClickListener(
			OnPreferenceClickListener onPreferenceClickListener) {
		super.setOnPreferenceClickListener(onPreferenceClickListener);
	}

	private void configurePreference(Context context) {
		this.context = context;
		this.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				getEditText().setSelection(getEditText().getText().length());
				return true;
			}
		});
		this.setOnPreferenceChangeListener(postListener);
		this.idManager = new IdManager(context);
	}

	/*
	 * We no longer change the key on a username change. This can be reenabled
	 * by registering this listener in configurePrefence().
	 */
	@SuppressWarnings("unused")
	private OnPreferenceClickListener preListener = new OnPreferenceClickListener() {

		private OnClickListener positive = new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}

		};

		@Override
		public boolean onPreferenceClick(Preference preference) {
			Log.v(TAG, "Username preference clicked");
			if (idManager.userIsNotSet() != true) {
				AlertDialog dialog = DialogFactory.buildUsernameChangeDialog(
						getContext(), positive);
				dialog.show();
			}
			return true;
		}
	};

	private OnPreferenceChangeListener postListener = new OnPreferenceChangeListener() {

		// TODO Make this be the actual preference, not a listener
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String newUsername = (String) newValue;
			try {
				idManager.resetUser(newUsername);
			} catch (UserNameInvalidException e) {
				Toast.makeText(context, "Invalid username. Username not changed.",
						Toast.LENGTH_SHORT).show();
				return false;
			}
			return true;
		}
	};
}
