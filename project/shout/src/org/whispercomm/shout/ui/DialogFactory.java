
package org.whispercomm.shout.ui;

import org.whispercomm.shout.R;
import org.whispercomm.shout.ui.widget.ShoutAgreementView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class DialogFactory {

	public static AlertDialog aboutDialog(Context context) {

		TextView message = new TextView(context);
		SpannableString s = new SpannableString(
				"For more information check out http://whispercomm.org/shout/\n\n"
						+ GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(context));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		message.setText(s);
		message.setTextSize(18);
		message.setPadding(10, 0, 10, 0);
		message.setMovementMethod(LinkMovementMethod.getInstance());

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(message)
				.setTitle("About")
				.setPositiveButton("Ok", null);
		return builder.create();
	}

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
				.setPositiveButton(R.string.user_agreement_dialog_postive, positive)
				.setNegativeButton(R.string.user_agreement_dialog_negative, negative)
				.setTitle(R.string.user_agreement_dialog_title);
		return builder.create();
	}

	public static AlertDialog buildRegistrationPromptDialog(final Context context,
			DialogInterface.OnClickListener neutral, DialogInterface.OnCancelListener cancel) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.ic_dialog).setTitle(R.string.register_manes_dialog_title)
				.setMessage(R.string.register_manes_dialog_message)
				.setCancelable(true)
				.setNeutralButton(R.string.register_manes_dialog_neutral, neutral)
				.setOnCancelListener(cancel);
		return builder.create();
	}

	public static AlertDialog buildInstallationPromptDialog(final Context context,
			DialogInterface.OnClickListener neutral, DialogInterface.OnCancelListener cancel) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.ic_dialog).setTitle(R.string.install_manes_dialog_title)
				.setMessage(R.string.install_manes_dialog_message).setCancelable(true)
				.setNeutralButton(R.string.install_manes_dialog_neutral, neutral)
				.setOnCancelListener(cancel);
		return builder.create();
	}

	public static AlertDialog colorCodingExplanation(Context context,
			DialogInterface.OnClickListener positive) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.colorExplanation).setCancelable(false)
				.setPositiveButton(R.string.expiry_dialog_netural, positive)
				.setCancelable(false);
		return builder.create();
	}
}
