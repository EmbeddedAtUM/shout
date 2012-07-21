
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
				.setPositiveButton("I understand the risks", positive);
		return builder.create();
	}

	public static AlertDialog buildUserAgreementDialog(Context context, ShoutAgreementView view,
			DialogInterface.OnClickListener positive, DialogInterface.OnClickListener negative) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false).setView(view)
				.setPositiveButton("I agree", positive)
				.setNegativeButton("Get me out of here!", negative)
				.setTitle("Shout User Agreement");
		return builder.create();
	}

	public static AlertDialog buildRegistrationPromptDialog(final Context context,
			DialogInterface.OnClickListener neutral, DialogInterface.OnCancelListener cancel) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.icon).setTitle(R.string.register_manes_dialog_title)
				.setMessage(R.string.register_manes_dialog_message)
				.setCancelable(true)
				.setNeutralButton(R.string.register_manes_dialog_neutral, neutral)
				.setOnCancelListener(cancel);
		return builder.create();
	}

	public static AlertDialog buildInstallationPromptDialog(final Context context,
			DialogInterface.OnClickListener neutral, DialogInterface.OnCancelListener cancel) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.icon).setTitle(R.string.install_manes_dialog_title)
				.setMessage(R.string.install_manes_dialog_message).setCancelable(true)
				.setNeutralButton(R.string.install_manes_dialog_neutral, neutral)
				.setOnCancelListener(cancel);
		return builder.create();
	}
}
