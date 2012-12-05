
package org.whispercomm.shout.ui.preference;

import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNameInvalidException;
import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.ui.DialogFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
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

	/**
	 * This filter will constrain edits not to make the number of bytes of the
	 * text greater than the specified length.
	 */
	private class MaxByteFilter implements InputFilter {
		private int max;

		public MaxByteFilter(int max) {
			this.max = max;
		}

		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			int keep = max - (dest.toString().getBytes().length - (dend - dstart));

			if (keep <= 0) {
				return "";
			} else if (keep >= end - start) {
				return null;
			} else {
				keep += start;
				if (Character.isHighSurrogate(source.charAt(keep - 1))) {
					--keep;
					if (keep == start) {
						return "";
					}
				}
				return source.subSequence(start, keep);
			}
		}
	}

	private void configurePreference(Context context) {
		this.context = context;
		final EditText editText = getEditText();
		editText.setFilters(new InputFilter[] {
				new MaxByteFilter(SerializeUtility.USERNAME_SIZE_MAX)
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
