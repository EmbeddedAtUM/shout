
package org.whispercomm.shout.ui.widget;

import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.R;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.util.FormattedAge;
import org.whispercomm.shout.util.FormattedAge.AgeListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * A custom component for displaying a single Shout.
 * 
 * @author David R. Bild
 */
public class ShoutView extends RelativeLayout {

	private TextView sender;
	private ImageView avatar;

	private TextView message;

	private TextView reshoutCount;
	private TextView commentCount;
	private TextView age;

	private TableLayout detailsTable;

	/**
	 * The shout current bound to this view
	 */
	private LocalShout shout;

	private Handler mHandler;

	private FormattedAge formattedAge;

	public ShoutView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoutview, this);
		initializeViews();
		mHandler = new Handler();
		formattedAge = FormattedAge.create(new AgeUpdater());
	}

	public ShoutView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoutview, this);
		initializeViews();
	}

	@Override
	public void onAttachedToWindow() {
		formattedAge.restart();
	}

	@Override
	public void onDetachedFromWindow() {
		formattedAge.stop();
	}

	@SuppressLint("NewApi")
	private void initializeViews() {
		avatar = (ImageView) findViewById(R.id.avatar);
		sender = (TextView) findViewById(R.id.sender);
		age = (TextView) findViewById(R.id.age);
		message = (TextView) findViewById(R.id.message);
		commentCount = (TextView) findViewById(R.id.commentCount);
		reshoutCount = (TextView) this.findViewById(R.id.reshoutCount);
		detailsTable = (TableLayout) findViewById(R.id.shoutDetails);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			message.setTextIsSelectable(true);
		}
	}

	/**
	 * Sets the Shout to be displayed by the view.
	 * 
	 * @param shout the Shout to be displayed
	 */
	public void bindShout(LocalShout shout) {
		this.shout = shout;

		/*
		 * Space is placed after username because when the text is ellipsized,
		 * the TextView adds one space. Thus, the layout is set up to assume a
		 * space after the username.
		 */
		sender.setText(String.format("%s ", shout.getSender().getUsername()));
		HashReference<Avatar> avatarRef = shout.getSender().getAvatar();
		if (avatarRef.isAvailable())
			avatar.setImageBitmap(avatarRef.get().getBitmap());
		else
			avatar.setImageResource(R.drawable.defaultavatar);

		message.setText(shout.getMessage());
		formattedAge.setDateTime(shout.getTimestamp());

		if (shout.getType() == ShoutType.SHOUT) {
			commentCount.setText(String.format("Comments (%d)",
					shout.getCommentCount()));
		} else {
			commentCount.setText("");
		}

		if (shout.getReshoutCount() > 0) {
			reshoutCount.setVisibility(View.VISIBLE);
			int count = shout.getReshouters().size();
			reshoutCount.setText(String.format("and %d %s", count, count == 1 ? "reshouter"
					: "reshouters"));
		} else {
			reshoutCount.setVisibility(View.GONE);
		}

		detailsTable.setVisibility(GONE);
	}

	private class AgeUpdater implements AgeListener, Runnable {

		private String mAge;

		@Override
		public void run() {
			if (age != null)
				age.setText(mAge);
		}

		@Override
		public void update(String age) {
			this.mAge = age;
			mHandler.post(this);
		}

	}

	/**
	 * Returns the shout currently shout.
	 * 
	 * @return the currently bound shout; {@code null} if none is bound.
	 */
	public LocalShout getBoundShout() {
		return this.shout;
	}

	public void showDetails() {
		detailsTable.removeAllViews();
		detailsTable.setVisibility(VISIBLE);
		// Add the time sent
		ShoutDetailRow timeSent = new ShoutDetailRow(getContext());
		timeSent.setTitleText("Time Sent");
		String sent = FormattedAge.formatAbsolute(shout.getTimestamp());
		timeSent.setEntryText(sent);
		detailsTable.addView(timeSent);
		// Add the time received
		ShoutDetailRow timeReceived = new ShoutDetailRow(getContext());
		timeReceived.setTitleText("Time Received");
		String received = FormattedAge.formatAbsolute(shout.getReceivedTime());
		timeReceived.setEntryText(received);
		detailsTable.addView(timeReceived);
		// Add the location
		Location location = shout.getLocation();
		if (location != null) {
			ShoutDetailRow longitude = new ShoutDetailRow(getContext());
			ShoutDetailRow latitude = new ShoutDetailRow(getContext());
			longitude.setTitleText("Longitude");
			latitude.setTitleText("Latitude");
			longitude.setEntryText(String.format("%f\u00b0", location.getLongitude()));
			latitude.setEntryText(String.format("%f\u00b0", location.getLatitude()));
			detailsTable.addView(longitude);
			detailsTable.addView(latitude);
		}

		// // Add the signature
		// ShoutDetailRow signature = new ShoutDetailRow(getContext());
		// signature.setTitleText("Signature");
		// signature.setEntryText(String.format("R<%s> S<%s>",
		// Encoders.toHexString(shout.getSignature().getS().toByteArray()),
		// Encoders.toHexString(shout.getSignature().getS().toByteArray())));
		// detailsTable.addView(signature);
		// // Add the hash
		// ShoutDetailRow hash = new ShoutDetailRow(getContext());
		// hash.setTitleText("Hash");
		// hash.setEntryText(Encoders.toHexString(shout.getHash()));
		// detailsTable.addView(hash);
	}

	public void hideDetails() {
		detailsTable.setVisibility(GONE);
		detailsTable.removeAllViews();
	}

}
