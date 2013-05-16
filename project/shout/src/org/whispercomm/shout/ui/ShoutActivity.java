
package org.whispercomm.shout.ui;

import java.io.File;
import java.io.IOException;

import org.whispercomm.shout.R;
import org.whispercomm.shout.tutorial.TutorialActivity;
import org.whispercomm.shout.tutorial.TutorialManager;
import org.whispercomm.shout.ui.fragment.MessageFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * The main activity for Shout. The activity displays a list of all received
 * shouts and provides interfaces for shouting, reshouting, and commenting.
 * 
 * @author David R. Bild
 */
public class ShoutActivity extends AbstractShoutViewActivity {

	private Uri imageUri;
	private static final int CAMERA_REQUEST = 1;
	private static final String TAG = MessageFragment.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected void initialize() {
		super.initialize();
		TutorialManager.showHelp(this);
		setContentView(R.layout.activity_shout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_shout, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_include_image_camera:
				this.requestCamera();

				break;
			case R.id.settings:
				SettingsActivity.show(this);
				break;
			case R.id.compose:
				MessageActivity.shout(this);
				break;
			case R.id.help:
				TutorialActivity.show(this);
				break;
			case R.id.about:
				AlertDialog dialog = DialogFactory.aboutDialog(this);
				dialog.show();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CAMERA_REQUEST:
				if (resultCode == Activity.RESULT_OK)
					onCameraResult(data);
				break;
			default:
				Log.i(TAG,
						String.format("Ignoring unexpected activity request code: %d", requestCode));
		}
	}

	private void onCameraResult(Intent data) {
		MessageActivity.shout(this, imageUri.getPath());
	}

	private void requestCamera() {
		try {
			File pictureDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
			File photo = File.createTempFile("camera", "jpg", pictureDir);

			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			imageUri = Uri.fromFile(photo);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

			startActivityForResult(cameraIntent, CAMERA_REQUEST);
		} catch (IOException e) {
			Log.w(TAG, "Error creating file for camera image", e);
			Toast.makeText(this, "Unable to get image from camera.", Toast.LENGTH_LONG).show();
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "Camera not found.", Toast.LENGTH_LONG).show();
		}
	}
}
