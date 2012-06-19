
package org.whispercomm.shout;

import org.joda.time.DateTime;
import org.whispercomm.shout.id.SignatureUtility;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MessageActivity extends Activity {

	public static final String TAG = "MessageActivity";

	private Toast successToast;
	private Toast failedToast;
	private Toast noUserToast;
	private SignatureUtility signUtility;
	private ShoutCreator creator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message);

		successToast = Toast.makeText(this, "Message Successfully Sent.",
				Toast.LENGTH_SHORT);

		failedToast = Toast
				.makeText(this, "An error occured, please try again later.",
						Toast.LENGTH_SHORT);
		noUserToast = Toast.makeText(getApplicationContext(), "Set up a user before you Shout!",
				Toast.LENGTH_LONG);
		signUtility = new SignatureUtility(getApplicationContext());
		creator = new ShoutCreator(getApplicationContext(), signUtility);
	}

	@Override
	public void onResume() {
		super.onResume();
		User user = signUtility.getUser();
		if (user == null) {
			finish();
			noUserToast.show();
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
	}

	public void onClickSend(View v) {
		Log.v(TAG, "Send button clicked");
		EditText editor = (EditText) findViewById(R.id.compose);
		String content = editor.getText().toString();
		Log.v(TAG, "Shout text received as: " + content);
		new SendShoutTask().execute(content);
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
	}

	private class SendShoutTask extends AsyncTask<String, Void, Boolean> {

		// TODO Progress indicator?

		@Override
		protected Boolean doInBackground(String... params) {
			return creator.createShout(DateTime.now(), params[0], null);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result.booleanValue()) {
				successToast.show();
			} else {
				failedToast.show();
			}
		}
	}
}
