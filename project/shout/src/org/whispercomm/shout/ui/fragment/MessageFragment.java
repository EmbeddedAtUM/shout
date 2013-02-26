
package org.whispercomm.shout.ui.fragment;

import java.io.IOException;

import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.R;
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
import org.whispercomm.shout.ui.AbstractShoutActivity;
import org.whispercomm.shout.ui.SettingsActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MessageFragment extends SherlockFragment {

	private static final String TAG = MessageFragment.class.getSimpleName();

	public static final String PARENT_ID = "parent";

	private LocationProvider mLocation;
	private boolean isLocationAttached;

	private MenuItem menuItemAttachLocation;
	private MenuItem menuItemSend;
	private EditText edtMessage;
	private FrameLayout frmProgressBar;
	private RelativeLayout frmTxtParent;

	private boolean isMessageEmpty = true;

	// We don't use a shout to show the original sender while commenting anymore
	// To change it back un-comment the shoutParent below and remove sender,
	// message, and avatar.
	// private ShoutView shoutParent;
	private TextView sender, message;
	private ImageView avatar;
	private LocalShout parent = null;
	private IdManager idManager;

	private Toast toast;

	public interface MessageFragmentContainer {
		public void messageFragmentFinished();
	}

	private Activity activity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setHasOptionsMenu(true);

		parent = getParent(activity.getIntent().getExtras());
		idManager = new IdManager(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_message, container, false);
		initializeViews(v);
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
		super.onCreateOptionsMenu(menu, inflator);
		inflator.inflate(R.menu.activity_message, menu);

		menuItemAttachLocation = menu.findItem(R.id.menu_include_location);
		menuItemSend = menu.findItem(R.id.menu_send);

		if (isMessageEmpty)
			menuItemSend.setEnabled(false);
		initializeAttachLocation();
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
		mLocation = new LocationProvider(activity);
	}

	@Override
	public void onStop() {
		mLocation.stop();
		super.onStop();
	}

	private void initializeViews(View v) {
		frmProgressBar = (FrameLayout) v.findViewById(R.id.frmProgressBar);
		// shoutParent = (ShoutView) v.findViewById(R.id.shoutParent);
		avatar = (ImageView) v.findViewById(R.id.commentAvatar);
		message = (TextView) v.findViewById(R.id.commentMessage);
		sender = (TextView) v.findViewById(R.id.commentOrigsender);
		frmTxtParent = (RelativeLayout) v.findViewById(R.id.frmParent);
		edtMessage = (EditText) v.findViewById(R.id.compose);

		edtMessage.setFilters(new InputFilter[] {
				new Utf8ByteLengthFilter(
						SerializeUtility.MESSAGE_SIZE_MAX)
		});
		edtMessage.addTextChangedListener(new EditMessageWatcher());

		Intent intent = activity.getIntent();
		if (intent.getAction() == Intent.ACTION_SEND) {
			String text = intent.getExtras().getString(Intent.EXTRA_TEXT);
			edtMessage.setText(text);
			if (edtMessage.getText().toString().isEmpty())
				setIsMessageEmpty(true);
			else
				setIsMessageEmpty(false);

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
		toast = Toast.makeText(activity, toastResid, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, toast.getYOffset());
		toast.show();
	}

	private void initializeAttachLocation() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(activity);
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

		parent = ShoutProviderContract.retrieveShoutByHash(activity, parentHash);
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
			((MessageFragmentContainer) activity).messageFragmentFinished();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void promptForUsername() {
		Toast.makeText(activity,
				"Set up a user before you Shout!", Toast.LENGTH_LONG).show();
		startActivity(new Intent(activity, SettingsActivity.class));
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
			new ShoutTask(activity,
					new ShoutCreationCompleteListener(), me, location)
					.execute(content);
		} else {
			new CommentTask(activity,
					new ShoutCreationCompleteListener(), me, location,
					parent).execute(content);
		}
	}

	private void shoutCreated(LocalShout result) {
		if (result != null) {
			new SendShoutTask(((AbstractShoutActivity) activity).getNetwork(),
					new ShoutSendCompleteListener())
					.execute(result);
		} else {
			Toast.makeText(activity, R.string.create_shout_failure,
					Toast.LENGTH_LONG).show();
			hideProgressBar();
		}
	}

	private void shoutSent(SendResult result) {
		MessageFragmentContainer container = (MessageFragmentContainer) activity;
		try {
			result.getResultOrThrow();
			Toast.makeText(activity, R.string.send_shout_success, Toast.LENGTH_SHORT)
					.show();
			container.messageFragmentFinished();
		} catch (NotConnectedException e) {
			Toast.makeText(activity, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
			container.messageFragmentFinished();
		} catch (ShoutChainTooLongException e) {
			Log.e(TAG, "SHOUT_CHAIN_TOO_LONG error.  Unable to send shout.");
			Toast.makeText(activity, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
			container.messageFragmentFinished();
		} catch (ManesNotInstalledException e) {
			((AbstractShoutActivity) activity).promptForInstallation();
		} catch (ManesNotRegisteredException e) {
			((AbstractShoutActivity) activity).promptForRegistration();
		} catch (IOException e) {
			Toast.makeText(activity, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
			container.messageFragmentFinished();
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
			if (!edtMessage.getText().toString().equals(""))
				setIsMessageEmpty(false);
			else
				setIsMessageEmpty(true);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	}

	private void setIsMessageEmpty(boolean isEmpty) {
		isMessageEmpty = isEmpty;
		if (menuItemSend != null)
			menuItemSend.setEnabled(!isEmpty);
	}
}
