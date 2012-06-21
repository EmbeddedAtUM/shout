
package org.whispercomm.shout;

import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.tasks.ShoutTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MessageActivity extends Activity {

	public static final String TAG = "MessageActivity";

	private Toast noUserToast;
	private SignatureUtility signUtility;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message);

		noUserToast = Toast.makeText(getApplicationContext(), "Set up a user before you Shout!",
				Toast.LENGTH_LONG);
		signUtility = new SignatureUtility(getApplicationContext());
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
		new ShoutTask(getApplicationContext()).execute(content);
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
	}
}
