package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutMessageUtility;

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

	// TODO: Get rid of this. Use global id (hash) or LocalShout#localId
	// instead.
	public int id = -1;

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
	 * TODO: Ultimately this should take one parameter, a LocalShout that has
	 * methods to return the comment and reshout counts.
	 * 
	 * @param shout
	 *            the Shout to be displayed
	 * @param numComments
	 *            the comment count for the shout to be displayed
	 * @param numReshouts
	 *            the reshout count for the shout to be displayed
	 */
	public void bindShout(Shout shout, int numComments, int numReshouts) {
		sender.setText(shout.getSender().getUsername());
		avatar.setImageResource(R.drawable.defaultavatar);

		message.setText(shout.getMessage());

		commentCount.setText(String.format("Comments (%d)", numComments));
		age.setText(ShoutMessageUtility.getDateTimeAge(shout.getTimestamp()));

		if (numReshouts > 0) {
			reshoutIcon.setVisibility(View.VISIBLE);
			reshouters.setVisibility(View.VISIBLE);
			reshouters.setText(String.format("Reshouted %s.",
					ShoutMessageUtility.getCountAsText(numReshouts)));
		} else {
			reshoutIcon.setVisibility(View.GONE);
			reshouters.setVisibility(View.GONE);
		}
	}

}
