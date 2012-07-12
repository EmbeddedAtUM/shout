
package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.LayoutInflater;
import android.widget.TextView;

public class ShoutDetailsView extends ShoutView {

	private TextView signature;
	private TextView hash;

	public ShoutDetailsView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoutdetailsview, this);
		initializeViews();
	}

	public ShoutDetailsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoutdetailsview, this);
		initializeViews();
	}

	private void initializeViews() {
		signature = (TextView) findViewById(R.id.signature);
		hash = (TextView) findViewById(R.id.hash);
	}

	@Override
	public void bindShout(LocalShout shout) {
		super.bindShout(shout);
		signature.setText(Base64.encodeToString(shout.getSignature(), Base64.DEFAULT));
		hash.setText(Base64.encodeToString(shout.getHash(), Base64.DEFAULT));
	}

}
