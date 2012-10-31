
package org.whispercomm.shout.tutorial;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class TutorialManager {

	private static final String KEY_TUTORIAL_SHOWN = "tutorial_shown";
	protected static final String TAG = TutorialManager.class.getSimpleName();

	public static void showHelp(Context context) {
		if (!hasShown(context)) {
			TutorialActivity.show(context);
			recordTutorialShown(context);
		}
	}

	public static boolean hasShown(Context context) {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean agreed = appSharedPrefs.getBoolean(KEY_TUTORIAL_SHOWN, false);
		return agreed;
	}

	private static void recordTutorialShown(Context context) {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = appSharedPrefs.edit();
		editor.putBoolean(KEY_TUTORIAL_SHOWN, true);
		editor.commit();
		Log.v(TAG, "User has seen tutorial");
	}
}
