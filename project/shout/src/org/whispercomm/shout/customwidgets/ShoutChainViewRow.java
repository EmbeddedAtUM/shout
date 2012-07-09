
package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.R;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.util.Conversions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class ShoutChainViewRow extends LinearLayout {

	private static final int EXPANDED_MARGIN_DP = 5;

	private LinearLayout border;
	private ActionShoutView actionShoutView;

	private boolean expanded;

	public ShoutChainViewRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoutchainview_row, this);
		initialize();
	}

	public ShoutChainViewRow(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoutchainview_row, this);
		initialize();
	}

	private void initialize() {
		actionShoutView = (ActionShoutView) findViewById(R.id.actionshoutview);
		border = (LinearLayout) findViewById(R.id.border);

		expanded = false;
		actionShoutView
				.registerActionBarStateChangeListener(new ActionShoutView.ActionBarStateChangeListener() {
					@Override
					public void stateChanged(boolean visibility) {
						setExpanded(visibility);
					}
				});
	}

	/**
	 * Expands the display of the shout. The shout is offset by margins in the
	 * listview.
	 * 
	 * @param expanded {@code true} if the view should be expanded;
	 *            {@code false} otherwise.
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
		setMargins(expanded);
	}

	public void toggleExpanded() {
		setExpanded(!expanded);
	}

	private void setMargins(boolean expanded) {
		int spacing = expanded ? Conversions.dpToPx(EXPANDED_MARGIN_DP,
				getContext().getResources()) : 0;
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) border
				.getLayoutParams();
		params.bottomMargin = spacing;
		border.setLayoutParams(params);
	}

	/**
	 * Sets the Shout to be displayed by the view.
	 * 
	 * @param shout the Shout to be displayed
	 */
	public void bindShout(LocalShout shout) {
		actionShoutView.bindShout(shout);
	}
}
