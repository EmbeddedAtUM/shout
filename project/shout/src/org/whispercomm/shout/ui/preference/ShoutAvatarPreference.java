
package org.whispercomm.shout.ui.preference;

import java.io.IOException;

import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.id.IdManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

public class ShoutAvatarPreference extends BitmapPreference {
	private static final String TAG = ShoutAvatarPreference.class
			.getSimpleName();

	private IdManager idManager;

	public ShoutAvatarPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.configurePreference(context);
	}

	public ShoutAvatarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.configurePreference(context);
	}

	private void configurePreference(Context context) {
		this.setOnPreferenceChangeListener(postListener);
		this.idManager = new IdManager(context);
	}

	private OnPreferenceChangeListener postListener = new OnPreferenceChangeListener() {

		// TODO Make this be the actual preference, not a listener
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Avatar avatar = new Avatar((Bitmap) newValue);
			try {
				idManager.setAvatar(avatar);
			} catch (IOException e) {
				Log.w(TAG, "Error saving avatar.", e);
				Toast.makeText(getContext(), "Error saving avatar.", Toast.LENGTH_LONG).show();
				return false;
			}
			return true;
		}
	};

}
