
package org.whispercomm.shout.terms;

import org.whispercomm.shout.ui.widget.DialogFactory;
import org.whispercomm.shout.ui.widget.ShoutAgreementView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class AgreementManager {

	private static final String KEY_AGREED = "has_agreed";
	protected static final String TAG = AgreementManager.class.getSimpleName();

	public static void getConsent(Context context, AgreementListener listener) {
		if (!hasAgreed(context)) {
			showAgreement(context, listener);
		} else {
			listener.accepted();
		}
	}

	public static boolean hasAgreed(Context context) {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean agreed = appSharedPrefs.getBoolean(KEY_AGREED, false);
		return agreed;
	}

	private static void recordUserAgreement(Context context) {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = appSharedPrefs.edit();
		editor.putBoolean(KEY_AGREED, true);
		editor.commit();
		Log.v(TAG, "User agreed to terms");
	}

	private static void showAgreement(final Context context, final AgreementListener listener) {
		DialogInterface.OnClickListener positive = new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				recordUserAgreement(context);
				listener.accepted();
			}

		};
		DialogInterface.OnClickListener negative = new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.v(TAG, "User did not agree to the terms");
				listener.declined();
			}

		};
		AlertDialog agreement = DialogFactory.buildUserAgreementDialog(context,
				new ShoutAgreementView(context), positive, negative);
		agreement.show();
	}
}
