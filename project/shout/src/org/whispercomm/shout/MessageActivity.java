package org.whispercomm.shout;

import org.joda.time.DateTime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MessageActivity extends Activity {

	Toast successToast;
	Toast failedToast;
	public static final String TAG = "MessageActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.message);

		successToast = Toast.makeText(this, "Message Successfully Sent.",
				Toast.LENGTH_SHORT);

		failedToast = Toast.makeText(this, "An error occured, please try again later.",
				Toast.LENGTH_SHORT);
	}

	public void onClickSend(View v) {
		Log.v(TAG, "Send button clicked");
		EditText editor = (EditText) findViewById(R.id.compose);
		String content = editor.getText().toString();
		Log.v(TAG, "Shout text received as: " + content);
		ShoutCreator.createShout(DateTime.now(), content, null);
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();

		successToast.show();
	}
}
