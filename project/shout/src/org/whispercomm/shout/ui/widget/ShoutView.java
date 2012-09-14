
package org.whispercomm.shout.ui.widget;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.util.Encoders;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.content.Context;
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

	private ImageView reshoutIcon;
	private TextView reshouters;

	private TextView message;

	private TextView commentCount;
	private TextView age;

	private TableLayout detailsTable;

	/**
	 * The shout current bound to this view
	 */
	private LocalShout shout;

	public ShoutView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoutview, this);
		initializeViews();
	}

	public ShoutView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoutview, this);
		initializeViews();
	}

	private void initializeViews() {
		avatar = (ImageView) findViewById(R.id.avatar);
		sender = (TextView) findViewById(R.id.origsender);
		reshouters = (TextView) this.findViewById(R.id.sender);
		age = (TextView) findViewById(R.id.age);
		message = (TextView) findViewById(R.id.message);
		reshoutIcon = (ImageView) findViewById(R.id.reshoutIcon);
		commentCount = (TextView) findViewById(R.id.commentCount);
		detailsTable = (TableLayout) findViewById(R.id.shoutDetails);
	}

	/**
	 * Sets the Shout to be displayed by the view.
	 * 
	 * @param shout the Shout to be displayed
	 */
	public void bindShout(LocalShout shout) {
		this.shout = shout;

		sender.setText(shout.getSender().getUsername());
		avatar.setImageResource(R.drawable.defaultavatar);

		message.setText(shout.getMessage());
		age.setText(ShoutMessageUtility.getDateTimeAge(shout.getTimestamp()));

		if (shout.getType() == ShoutType.SHOUT) {
			commentCount.setText(String.format("Comments (%d)",
					shout.getCommentCount()));
		} else {
			commentCount.setText("");
		}

		if (shout.getReshoutCount() > 0) {
			reshoutIcon.setVisibility(View.VISIBLE);
			reshouters.setVisibility(View.VISIBLE);
			int count = shout.getReshouters().size();
			reshouters.setText(String.format("Reshouted by %d %s.", count, count == 1 ? "user"
					: "users"));

		} else {
			reshoutIcon.setVisibility(View.GONE);
			reshouters.setVisibility(View.GONE);
		}

		detailsTable.setVisibility(GONE);
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
		detailsTable.setVisibility(VISIBLE);
		// Add the time sent
		ShoutDetailRow timeSent = new ShoutDetailRow(getContext());
		timeSent.setTitleText("Time Sent");
		String sent = ShoutMessageUtility.getReadableDateTime(shout.getTimestamp());
		timeSent.setEntryText(sent);
		detailsTable.addView(timeSent);
		// Add the time received
		ShoutDetailRow timeReceived = new ShoutDetailRow(getContext());
		timeReceived.setTitleText("Time Received");
		String received = ShoutMessageUtility.getReadableDateTime(shout.getReceivedTime());
		timeReceived.setEntryText(received);
		detailsTable.addView(timeReceived);
		// Add the signature
		ShoutDetailRow signature = new ShoutDetailRow(getContext());
		signature.setTitleText("Signature");
		signature.setEntryText(String.format("R<%s> S<%s>",
				Encoders.toHexString(shout.getSignature().getS().toByteArray()),
				Encoders.toHexString(shout.getSignature().getS().toByteArray())));
		detailsTable.addView(signature);
		// Add the hash
		ShoutDetailRow hash = new ShoutDetailRow(getContext());
		hash.setTitleText("Hash");
		hash.setEntryText(Encoders.toHexString(shout.getHash()));
		detailsTable.addView(hash);
	}

	public void hideDetails() {
		detailsTable.setVisibility(GONE);
		detailsTable.removeAllViews();
	}

}
