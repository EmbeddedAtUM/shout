
package org.whispercomm.shout.expiry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.whispercomm.shout.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.util.Linkify;

/**
 * Manages the expiration date of the Shout software.
 * 
 * @author David R. Bild
 */
public class ExpiryManager {
	/**
	 * Expiration time
	 */
	public static final DateTime EXPIRY = new DateTime(2012, 8, 31, 23, 59, 59,
			DateTimeZone.forID("America/Detroit"));

	/**
	 * Checks if the expiration time has passed.
	 * 
	 * @return {@code true} if the current system time is after the expiration
	 *         time and {@code false} otherwise.
	 */
	public static boolean hasExpired() {
		return EXPIRY.isBeforeNow();
	}

	/**
	 * Create a dialog informing the user that the installed version of Shout
	 * has expired and needs to be upgraded to continue working.
	 * 
	 * @param context the context used to create the dialog
	 * @param neutral the callback invoked when the user acknowledges the dialog
	 * @param cancel the callback invoked when the user cancels the dialog
	 * @return the created dialog
	 */
	public static AlertDialog buildExpirationDialog(Context context,
			DialogInterface.OnClickListener neutral, DialogInterface.OnCancelListener cancel) {
		SpannableString msg = new SpannableString(context.getResources().getText(
				R.string.expiry_dialog_message));
		Linkify.addLinks(msg, Linkify.WEB_URLS);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.expiry_dialog_title);
		builder.setIcon(R.drawable.icon);
		builder.setMessage(msg);
		builder.setNeutralButton(R.string.expiry_dialog_netural, neutral);
		builder.setOnCancelListener(cancel);

		return builder.create();
	}
}
