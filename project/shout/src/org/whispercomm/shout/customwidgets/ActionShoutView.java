
package org.whispercomm.shout.customwidgets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Displays a shout with an action bar underneath. The display of the bar can be
 * toggled by clicking the shout.
 * 
 * @author David R. Bild
 */
public class ActionShoutView extends LinearLayout {
	private static final String TAG = ActionShoutView.class.getSimpleName();

	private ShoutView shoutView;

	private LinearLayout actionBar;
	private ImageButton btnReshout;
	private ImageButton btnComment;
	private ImageButton btnDetails;

	private boolean actionBarVisibility;

	// Listeners called when a button is clicked
	private OnClickListener reshoutListener;
	private OnClickListener commentListener;
	private OnClickListener detailsListener;

	// Listeners called when the action bar is toggled
	private List<ActionBarStateChangeListener> actionBarStateChangeListeners;

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
		actionBarStateChangeListeners = Collections
				.synchronizedList(new ArrayList<ActionBarStateChangeListener>());

		setActionBarVisibility(false);
		this.setToggleActionBarOnClick(true);

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
			public void onClick(LocalShout shout) {
				if (mHandler == null) {
					try {
						mHandler = getContext().getClass().getMethod(
								methodName, LocalShout.class);
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
	 * Enables or disables toggling of the action bar visibility when the view
	 * is clicked.
	 * 
	 * @param toggle {@code true} to toggle on click; {@code false} otherwise.
	 */
	public void setToggleActionBarOnClick(boolean toggle) {
		if (toggle) {
			this.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					toggleActionBarVisibility();
				}
			});
		} else {
			this.setOnClickListener(null);
		}
	}

	/**
	 * Sets the visibility of the action bar displayed below the shout.
	 * 
	 * @param visibility {@code true} to show the action bar; {@code false}
	 *            otherwise.
	 */
	public void setActionBarVisibility(boolean visibility) {
		if (visibility) {
			actionBarVisibility = true;
			actionBar.setVisibility(VISIBLE);
		} else {
			actionBarVisibility = false;
			actionBar.setVisibility(GONE);
		}
		for (ActionBarStateChangeListener l : actionBarStateChangeListeners) {
			l.stateChanged(actionBarVisibility);
		}
	}

	/**
	 * Toggles the visibility of the action bar displayed below the shout.
	 */
	public void toggleActionBarVisibility() {
		setActionBarVisibility(!actionBarVisibility);
	}

	/**
	 * Sets the Shout to be displayed by the view.
	 * 
	 * @param shout the Shout to be displayed
	 */
	public void bindShout(LocalShout shout) {
		shoutView.bindShout(shout);
		setActionBarVisibility(false);

		// Hide comment button when displaying a comment.
		switch (shout.getType()) {
			case SHOUT:
				btnComment.setVisibility(VISIBLE);
				break;
			case COMMENT:
				btnComment.setVisibility(GONE);
				break;
			default:
				Log.e(TAG, "Unexpected shout type in bindShout(LocalShout): "
						+ shout.getType().name());
				break;
		}
	}

	/**
	 * Sets the {@link OnClickListener} callback for the reshout button.
	 * 
	 * @param l the click listener
	 */
	public void setReshoutOnClickListener(OnClickListener l) {
		this.reshoutListener = l;
	}

	/**
	 * Sets the {@link OnClickListener} callback for the comment button.
	 * 
	 * @param l the click listener
	 */
	public void setCommentOnClickListener(OnClickListener l) {
		this.commentListener = l;
	}

	/**
	 * Sets the {@link OnClickListener} callback for the details button.
	 * 
	 * @param l the click listener
	 */
	public void setDetailsOnClickListener(OnClickListener l) {
		this.detailsListener = l;
	}

	/**
	 * Registers the listener to be called when the action bar visibility
	 * changes.
	 * <p>
	 * Each listener is called as many times as it was registered. But you
	 * probably don't actually want to register a listener more than once.
	 * 
	 * @param l the listener to register
	 */
	public void registerActionBarStateChangeListener(
			ActionBarStateChangeListener l) {
		this.actionBarStateChangeListeners.add(l);
	}

	/**
	 * Unregisters the listener, if it was registered. If the listener was
	 * registered more than once, only a single registration is removed.
	 * 
	 * @param l the listener to unregister
	 * @return {@code true} if the listener was unregistered, {@code false} is
	 *         the listener was not already registered.
	 */
	public boolean unregisterActionBarStateChangeListener(
			ActionBarStateChangeListener l) {
		return this.actionBarStateChangeListeners.remove(l);
	}

	/**
	 * Callback class called when an action button is clicked.
	 * 
	 * @author David R. Bild
	 */
	public static interface OnClickListener {
		public void onClick(LocalShout shout);
	}

	/**
	 * Callback class called when the visibility of the action bar changes.
	 * 
	 * @author David R. Bild
	 */
	public static interface ActionBarStateChangeListener {
		/**
		 * @param visibility {@code true} if the action bar is now visibile,
		 *            {@code false} otherwise.
		 */
		public void stateChanged(boolean visibility);
	}

}
