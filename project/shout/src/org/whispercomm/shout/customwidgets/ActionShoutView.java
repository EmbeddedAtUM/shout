package org.whispercomm.shout.customwidgets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Displays a shout with an action bar underneath. The display of the bar can be
 * toggled by clicking the shout.
 * 
 * @author David R. Bild
 * 
 */
public class ActionShoutView extends LinearLayout {

	private ShoutView shoutView;

	private LinearLayout actionBar;
	private ImageButton btnReshout;
	private ImageButton btnComment;
	private ImageButton btnDetails;

	private boolean barVisible;

	// Listeners called when a button is clicked
	private OnClickListener reshoutListener;
	private OnClickListener commentListener;
	private OnClickListener detailsListener;

	public ActionShoutView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.actionshoutview, this);
		initialize(attributeSet);
	}

	public ActionShoutView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.actionshoutview, this);
		initialize(null);
	}

	private void initialize(AttributeSet attributeSet) {
		this.setOrientation(VERTICAL);
		initializeViews();
		initializeActionBar();
		initializeAttributes(attributeSet);
	}

	private void initializeViews() {
		shoutView = (ShoutView) findViewById(R.id.shoutview);
		actionBar = (LinearLayout) findViewById(R.id.actionbar);
		btnReshout = (ImageButton) findViewById(R.id.reshoutButton);
		btnComment = (ImageButton) findViewById(R.id.commentButton);
		btnDetails = (ImageButton) findViewById(R.id.detailsButton);
	}

	private void initializeActionBar() {
		setBarVisibility(false);

		btnReshout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (reshoutListener != null) {
					reshoutListener.onClick(shoutView.getBoundShout());
				}
			}
		});

		btnComment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (commentListener != null) {
					commentListener.onClick(shoutView.getBoundShout());
				}
			}
		});

		btnDetails.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (detailsListener != null) {
					detailsListener.onClick(shoutView.getBoundShout());
				}
			}
		});
	}

	/**
	 * Autoregisters a callback listener for the methods specified by the
	 * onReshoutClick, onCommentClick, and onDetailsClick attributes in the
	 * layout XML, if given.
	 * 
	 * @param attributeSet
	 */
	private void initializeAttributes(AttributeSet attributeSet) {
		String methodName;
		TypedArray array = getContext().obtainStyledAttributes(attributeSet,
				R.styleable.ActionShoutView);
		for (int i = 0; i < array.getIndexCount(); ++i) {
			int attr = array.getIndex(i);
			switch (attr) {
			case R.styleable.ActionShoutView_onReshoutClick:
				methodName = array.getString(attr);
				if (methodName != null) {
					this.setReshoutOnClickListener(createOnClickListener(methodName));
				}
				break;
			case R.styleable.ActionShoutView_onCommentClick:
				methodName = array.getString(attr);
				if (methodName != null) {
					this.setCommentOnClickListener(createOnClickListener(methodName));
				}
				break;
			case R.styleable.ActionShoutView_onDetailsClick:
				methodName = array.getString(attr);
				if (methodName != null) {
					this.setDetailsOnClickListener(createOnClickListener(methodName));
				}
				break;
			}
		}
	}

	private OnClickListener createOnClickListener(final String methodName) {
		return new OnClickListener() {
			private Method mHandler;

			@Override
			public void onClick(Shout shout) {
				if (mHandler == null) {
					try {
						mHandler = getContext().getClass().getMethod(
								methodName, Shout.class);
					} catch (NoSuchMethodException e) {
						throw new IllegalStateException(String.format(
								"%s is not a valid method in the Activity.",
								methodName), e);
					}
				}

				try {
					mHandler.invoke(getContext(), shout);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				} catch (InvocationTargetException e) {
					throw new IllegalStateException(e);
				}
			}
		};
	}

	/**
	 * Sets the visibility of the action bar displayed below the shout.
	 * 
	 * @param visibility
	 *            {@code true} to show the action bar; {@code false} otherwise.
	 */
	public void setBarVisibility(boolean visibility) {
		if (visibility) {
			barVisible = true;
			actionBar.setVisibility(VISIBLE);
		} else {
			barVisible = false;
			actionBar.setVisibility(GONE);
		}
	}

	/**
	 * Toggles the visibility of the action bar displayed below the shout.
	 */
	public void toggleBarVisibility() {
		setBarVisibility(!barVisible);
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
		shoutView.bindShout(shout, numComments, numReshouts);
		setBarVisibility(false);

		// Hide comment button when displaying a comment.
		switch (shout.getType()) {
		case SHOUT:
			btnComment.setVisibility(VISIBLE);
			break;
		case COMMENT:
			btnComment.setVisibility(GONE);
			break;
		}
	}

	/**
	 * Sets the {@link OnClickListener} callback for the reshout button.
	 * 
	 * @param l
	 *            the click listener
	 */
	public void setReshoutOnClickListener(OnClickListener l) {
		this.reshoutListener = l;
	}

	/**
	 * Sets the {@link OnClickListener} callback for the comment button.
	 * 
	 * @param l
	 *            the click listener
	 */
	public void setCommentOnClickListener(OnClickListener l) {
		this.commentListener = l;
	}

	/**
	 * Sets the {@link OnClickListener} callback for the details button.
	 * 
	 * @param l
	 *            the click listener
	 */
	public void setDetailsOnClickListener(OnClickListener l) {
		this.detailsListener = l;
	}

	/**
	 * Callback class called when an action button is clicked.
	 * 
	 * @author David R. Bild
	 * 
	 */
	public static interface OnClickListener {
		public void onClick(Shout shout);
	}

}
