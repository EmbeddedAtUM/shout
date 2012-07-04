package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.R;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.ShoutMessageUtility;
import org.whispercomm.shout.ShoutType;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * A custom component for displaying a single Shout.
 * 
 * @author David R. Bild
 * 
 */
public class ShoutView extends RelativeLayout {

	private TextView sender;
	private ImageView avatar;

	private ImageView reshoutIcon;
	private TextView reshouters;

	private TextView message;

	private TextView commentCount;
	private TextView age;

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
	}

	/**
	 * Sets the Shout to be displayed by the view.
	 * 
	 * @param shout
	 *            the Shout to be displayed
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
			reshouters.setText(String
					.format("Reshouted %s.", ShoutMessageUtility
							.getCountAsText(shout.getReshoutCount())));
		} else {
			reshoutIcon.setVisibility(View.GONE);
			reshouters.setVisibility(View.GONE);
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

}
