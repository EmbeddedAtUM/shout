
package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogFactory {

	public static AlertDialog buildUsernameChangeDialog(Context context,
			DialogInterface.OnClickListener positive) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.username_change_warning)
				.setCancelable(false)
				.setPositiveButton("OK! OK! Now let me walk into Mordor...", positive);
		return builder.create();
	}

}
