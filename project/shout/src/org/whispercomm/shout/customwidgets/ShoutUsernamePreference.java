package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.User;
import org.whispercomm.shout.id.SignatureUtility;
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

	private Context context;
	
	public ShoutUsernamePreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.setListeners(context);
	}

	public ShoutUsernamePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setListeners(context);
	}

	public ShoutUsernamePreference(Context context) {
		super(context);
		this.setListeners(context);
	}

	@Override
	public void setOnPreferenceClickListener(
			OnPreferenceClickListener onPreferenceClickListener) {
		super.setOnPreferenceClickListener(onPreferenceClickListener);
	}

	private void setListeners(Context context) {
		this.setOnPreferenceClickListener(preListener);
		this.setOnPreferenceChangeListener(postListener);
		this.context = context;
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

			AlertDialog dialog = DialogFactory.buildUsernameChangeDialog(
					getContext(), positive);
			dialog.show();
			return true;
		}
	};

	private OnPreferenceChangeListener postListener = new OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String newName = (String) newValue;
			SignatureUtility signUtility = new SignatureUtility(context);
			User current = signUtility.getUser(); // TODO Fix lifecycle
			if (current == null) {
				try {
					signUtility.updateUserName(newName);
				} catch (UserNameInvalidException e) {
					Log.v(TAG, e.getMessage());
					return false;
				}
				return true;
			}
			String oldName = current.getUsername();
			if (!oldName.equals(newName)) {
				try {
					signUtility.updateUserName(newName);
					Log.v(TAG, "Updated user name to: " + newName);
					return true;
				} catch (UserNameInvalidException e) {
					Log.v(TAG, e.getMessage());
				}
			}
			return false;
		}
	};
}
