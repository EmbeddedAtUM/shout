
package org.whispercomm.shout.ui;

import java.io.IOException;

import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.SimpleLocation;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.location.LocationProvider;
import org.whispercomm.shout.network.service.NetworkInterface.NotConnectedException;
import org.whispercomm.shout.network.shout.ShoutChainTooLongException;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.tasks.AsyncTaskCallback.AsyncTaskCompleteListener;
import org.whispercomm.shout.tasks.CommentTask;
import org.whispercomm.shout.tasks.SendResult;
import org.whispercomm.shout.tasks.SendShoutTask;
import org.whispercomm.shout.tasks.ShoutTask;
import org.whispercomm.shout.thirdparty.Utf8ByteLengthFilter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MessageActivity extends AbstractShoutActivity {
	private static final String TAG = MessageActivity.class.getSimpleName();

	private static final String PARENT_ID = "parent";

	/**
	 * Starts the message activity to send a new original shout.
	 * 
	 * @param context the context used to start the activity
	 */
	public static void shout(Context context) {
		comment(context, null);
	}

	/**
	 * Starts the message activity to send a new comment on the specified shout.
	 * 
	 * @param context the context used to start the activity
	 * @param shout the shout on which to comment
	 */
	public static void comment(Context context, Shout shout) {
		Intent intent = new Intent(context, MessageActivity.class);
		if (shout != null) {
			intent.putExtra(PARENT_ID, shout.getHash());
		}
		context.startActivity(intent);
	}

	private IdManager idManager;

	private LocationProvider mLocation;
	private boolean isLocationAttached;

	private MenuItem menuItemAttachLocation;
	private MenuItem menuItemSend;
	private EditText edtMessage;
	private FrameLayout frmProgressBar;
	private RelativeLayout frmTxtParent;
	// We don't use a shout to show the original sender while commenting anymore
	// To change it back un-comment the shoutParent below and remove sender,
	// message, and avatar.
	// private ShoutView shoutParent;
	private TextView sender, message;
	private ImageView avatar;
	private LocalShout parent = null;

	private Toast toast;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflator = getSupportMenuInflater();
		inflator.inflate(R.menu.activity_message, menu);
		menuItemAttachLocation = menu.findItem(R.id.menu_include_location);
		menuItemSend = menu.findItem(R.id.menu_send);

		if (edtMessage.getText().toString().equals(""))
			menuItemSend.setEnabled(false);

		initializeAttachLocation();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.menu_include_location:
				toggleAttachLocation();
				break;
			case R.id.menu_send:
				send();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		mLocation = new LocationProvider(this);
	}

	@Override
	public void onStop() {
		mLocation.stop();
		super.onStop();
	}

	protected void initialize() {
		super.initialize();
		parent = getParent(getIntent().getExtras());
		initializeViews();
		idManager = new IdManager(getApplicationContext());
	}

	private void initializeViews() {
		setContentView(R.layout.message_activity);

		frmProgressBar = (FrameLayout) findViewById(R.id.frmProgressBar);
		// shoutParent = (ShoutView) findViewById(R.id.shoutParent);
		avatar = (ImageView) findViewById(R.id.commentAvatar);
		message = (TextView) findViewById(R.id.commentMessage);
		sender = (TextView) findViewById(R.id.commentOrigsender);
		frmTxtParent = (RelativeLayout) findViewById(R.id.frmParent);
		edtMessage = (EditText) findViewById(R.id.compose);

		edtMessage.setFilters(new InputFilter[] {
				new Utf8ByteLengthFilter(
						SerializeUtility.MESSAGE_SIZE_MAX)
		});
		edtMessage.addTextChangedListener(new EditMessageWatcher());

		Intent intent = getIntent();
		if (intent.getAction() == Intent.ACTION_SEND) {
			String text = intent.getExtras().getString(Intent.EXTRA_TEXT);
			edtMessage.setText(text);
		}

		if (parent != null) {
			// shoutParent.bindShout(parent);
			LocalShout shout = (LocalShout) parent;
			message.setText(shout.getMessage());
			HashReference<Avatar> avatarRef = shout.getSender().getAvatar();
			if (avatarRef.isAvailable())
				avatar.setImageBitmap(avatarRef.get().getBitmap());
			else
				avatar.setImageResource(R.drawable.defaultavatar);
			sender.setText(shout.getSender().getUsername());
			frmTxtParent.setVisibility(RelativeLayout.VISIBLE);
		}
	}

	private void toggleAttachLocation() {
		int toastResid;
		if (isLocationAttached) {
			toastResid = R.string.toast_untag_location;
			setAttachLocationChecked(false);
		} else {
			toastResid = R.string.toast_tag_location;
			setAttachLocationChecked(true);
		}
		if (toast != null)
			toast.cancel();
		toast = Toast.makeText(this, toastResid, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, toast.getYOffset());
		toast.show();
	}

	private void initializeAttachLocation() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		isLocationAttached = prefs.getBoolean("attachLocation", true);
		setAttachLocationChecked(isLocationAttached);
	}

	private void setAttachLocationChecked(boolean bool) {
		isLocationAttached = bool;
		if (bool) {
			menuItemAttachLocation.setIcon(R.drawable.ic_menu_pin_white);
		} else {
			menuItemAttachLocation.setIcon(R.drawable.ic_menu_pin_rotated_white);
		}
	}

	private LocalShout getParent(Bundle extras) {
		if (extras == null) {
			return null;
		}

		byte[] parentHash = extras.getByteArray(PARENT_ID);
		if (parentHash == null) {
			return null;
		}

		parent = ShoutProviderContract.retrieveShoutByHash(getApplicationContext(), parentHash);
		switch (parent.getType()) {
			case COMMENT:
			case RESHOUT:
				return parent.getParent();
			case RECOMMENT:
				return parent.getParent().getParent();
			default:
				return parent;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (idManager.userIsNotSet()) {
			promptForUsername();
			finish();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void promptForUsername() {
		Toast.makeText(getApplicationContext(),
				"Set up a user before you Shout!", Toast.LENGTH_LONG).show();
		startActivity(new Intent(this, SettingsActivity.class));
	}

	private void showProgressBar() {
		frmProgressBar.setVisibility(FrameLayout.VISIBLE);
		menuItemSend.setEnabled(false);
	}

	private void hideProgressBar() {
		frmProgressBar.setVisibility(FrameLayout.GONE);
		menuItemSend.setEnabled(true);
	}

	public void send() {
		showProgressBar();
		String content = edtMessage.getText().toString();
		Location location = null;
		if (isLocationAttached) {
			location = SimpleLocation.create(mLocation.getLocation());
		}
		Me me;
		try {
			me = idManager.getMe();
		} catch (UserNotInitiatedException e) {
			promptForUsername();
			hideProgressBar();
			return;
		}
		if (parent == null) {
			new ShoutTask(getApplicationContext(),
					new ShoutCreationCompleteListener(), me, location)
					.execute(content);
		} else {
			new CommentTask(getApplicationContext(),
					new ShoutCreationCompleteListener(), me, location,
					parent).execute(content);
		}
	}

	private void shoutCreated(LocalShout result) {
		if (result != null) {
			new SendShoutTask(network, new ShoutSendCompleteListener())
					.execute(result);
		} else {
			Toast.makeText(this, R.string.create_shout_failure,
					Toast.LENGTH_LONG).show();
			hideProgressBar();
		}
	}

	private void shoutSent(SendResult result) {
		try {
			result.getResultOrThrow();
			Toast.makeText(this, R.string.send_shout_success, Toast.LENGTH_SHORT)
					.show();
			finish();
		} catch (NotConnectedException e) {
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
			finish();
		} catch (ShoutChainTooLongException e) {
			Log.e(TAG, "SHOUT_CHAIN_TOO_LONG error.  Unable to send shout.");
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
			finish();
		} catch (ManesNotInstalledException e) {
			this.promptForInstallation();
		} catch (ManesNotRegisteredException e) {
			this.promptForRegistration();
		} catch (IOException e) {
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
			finish();
		}

	}

	private class ShoutCreationCompleteListener implements
			AsyncTaskCompleteListener<LocalShout> {
		@Override
		public void onComplete(LocalShout result) {
			shoutCreated(result);
		}
	}

	private class ShoutSendCompleteListener implements
			AsyncTaskCompleteListener<SendResult> {
		@Override
		public void onComplete(SendResult result) {
			shoutSent(result);
		}
	}

	/**
	 * A textwatcher, listen to the textchange event of edtMessage Enable the
	 * menuItemSend when the text field is not empty.
	 * 
	 * @author Junzhe Zhang
	 */
	private class EditMessageWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			if (!edtMessage.getText().toString().equals("")) {
				if (menuItemSend != null)
					menuItemSend.setEnabled(true);
			} else {
				if (menuItemSend != null)
					menuItemSend.setEnabled(false);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

	}
}
