
package org.whispercomm.shout.ui.widget;

import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNameInvalidException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;

public class ShoutUsernamePreference extends ShoutEditTextPreference {

	private static final String TAG = ShoutUsernamePreference.class
			.getSimpleName();

	private IdManager idManager;

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
		this.setOnPreferenceClickListener(preListener);
		this.setOnPreferenceChangeListener(postListener);
		this.idManager = new IdManager(context);
	}

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
				return false;
			}
			return true;
		}
	};
}
