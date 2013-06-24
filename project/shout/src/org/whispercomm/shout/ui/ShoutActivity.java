
package org.whispercomm.shout.ui;

import java.io.File;
import java.io.IOException;

import org.whispercomm.shout.R;
import org.whispercomm.shout.tutorial.TutorialActivity;
import org.whispercomm.shout.tutorial.TutorialManager;
import org.whispercomm.shout.ui.fragment.MessageFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
	private static final int CAMERA_REQUEST = 0;
	private static final int GALLERY_REQUEST = 1;
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
				this.requestImage();
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
			case GALLERY_REQUEST:
				if (resultCode == Activity.RESULT_OK)
					onGalleryResult(data);
				break;
			default:
				Log.i(TAG,
						String.format("Ignoring unexpected activity request code: %d", requestCode));
		}
	}

	private void onCameraResult(Intent data) {
		MessageActivity.shout(this, imageUri.getPath());
	}

	private void onGalleryResult(Intent data) {
		imageUri = data.getData();
		String[] filePathColumn = {
				MediaStore.Images.Media.DATA
		};

		Cursor cursor = getContentResolver().query(imageUri,
				filePathColumn, null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		MessageActivity.shout(this, picturePath);
		cursor.close();

	}

	private void requestImage() {
		String items[] = {
				"Camera", "Gallery"
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choosing Image From")
				.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case CAMERA_REQUEST:
								requestCamera();
								break;
							case GALLERY_REQUEST:
								requestGallery();
								break;
							default:
								Log.i(TAG, "Undefined image request");
						}

					}
				});
		Dialog dialog = builder.create();
		dialog.show();

	}

	private void requestGallery() {
		Intent galleryIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(galleryIntent, GALLERY_REQUEST);
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
