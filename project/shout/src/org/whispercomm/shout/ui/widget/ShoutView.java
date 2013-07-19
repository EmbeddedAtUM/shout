
package org.whispercomm.shout.ui.widget;

import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.ShoutImage;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.provider.image.ImageProviderContract;
import org.whispercomm.shout.text.ShoutLinkify;
import org.whispercomm.shout.util.FormattedAge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
		this(context, null);
	}

	@SuppressLint("NewApi")
	private void initializeViews() {
		avatar = (ImageView) findViewById(R.id.avatar);
		sender = (TextView) findViewById(R.id.sender);
		age = (TextView) findViewById(R.id.age);
		message = (TextView) findViewById(R.id.message);
		commentCount = (TextView) findViewById(R.id.commentCount);
		reshoutCount = (TextView) this.findViewById(R.id.reshoutCount);

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

		/*
		 * Load the avatar using Picasso library
		 */
		HashReference<ShoutImage> avatarRef = shout.getSender().getAvatar();
		Uri mUri = ImageProviderContract.imageUri(avatarRef.getHash());
		Picasso.with(this.getContext()).load(mUri.toString())
				.placeholder(R.drawable.defaultavatar)
				.error(R.drawable.defaultavatar).into(avatar);

		message.setText(shout.getMessage());
		ShoutLinkify.addLinks(message);
		Linkify.addLinks(message, Linkify.ALL);

		/*
		 * If the sent time is after the received time, base the duration period
		 * on the time received.
		 */
		String formattedAge;
		if (shout.getTimestamp().isAfter(shout.getReceivedTime()))
			formattedAge = FormattedAge.formatAge(shout.getReceivedTime(), true);
		else
			formattedAge = FormattedAge.formatAge(shout.getTimestamp(), true);
		age.setText(formattedAge);

		if (shout.getType() == ShoutType.SHOUT) {
			commentCount.setText(String.format("Comments (%d)",
					shout.getCommentCount()));
		} else {
			commentCount.setText("");
		}

		if (shout.getReshouterCount() > 0) {
			reshoutCount.setVisibility(View.VISIBLE);
			int count = shout.getReshouterCount();
			reshoutCount.setText(String.format("and %d %s", count, count == 1 ? "reshouter"
					: "reshouters"));
		} else {
			reshoutCount.setVisibility(View.GONE);
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
