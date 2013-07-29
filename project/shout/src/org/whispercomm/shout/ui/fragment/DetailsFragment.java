
package org.whispercomm.shout.ui.fragment;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutImage;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.provider.CursorLoader;
import org.whispercomm.shout.provider.ShoutCursorAdapter;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.provider.image.ImageProviderContract;
import org.whispercomm.shout.text.ShoutLinkify;
import org.whispercomm.shout.ui.AbstractShoutViewActivity;
import org.whispercomm.shout.ui.DetailsActivity;
import org.whispercomm.shout.ui.MapActivity;
import org.whispercomm.shout.ui.widget.ExpandableView;
import org.whispercomm.shout.ui.widget.FullListView;
import org.whispercomm.shout.ui.widget.MarkerMapLayer;
import org.whispercomm.shout.ui.widget.MarkerMapSizer;
import org.whispercomm.shout.ui.widget.WrappingItemAdapter;
import org.whispercomm.shout.util.FormattedAge;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

public class DetailsFragment extends SherlockFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOADER_COMMENTS = 0;
	private static final int LOADER_RESHOUTS = 1;

	/*
	 * The BitmapDescriptorFactory requires a Context, so these can't be
	 * configured statically
	 */
	private static BitmapDescriptor ORIGINAL_MARKER;
	private static BitmapDescriptor COMMENT_MARKER;
	private static BitmapDescriptor RESHOUT_MARKER;

	private ShoutView mShoutView;

	private ExpandableView mMapView;
	private ExpandableView mCommentsView;
	private ExpandableView mReshoutsView;

	private ImageButton mExpandMap;

	private SupportMapFragment mMapFragment;
	private GoogleMap mMap;
	private FullListView mCommentsList;
	private FullListView mReshoutsList;

	private LocalShout mShout;

	private CommentAdapter mCommentAdapter;
	private ReshoutAdapter mReshoutAdapter;

	private CommentMarkerAdapter mCommentMarkerAdapter;
	private ReshoutMarkerAdapter mReshoutMarkerAdapter;

	private CommentObserver mCommentObserver;
	private ReshoutObserver mReshoutObserver;
	private MarkerObserver mMarkerObserver;

	private MarkerMapLayer mCommentMarkerLayer;
	private MarkerMapLayer mReshoutMarkerLayer;

	private boolean mOriginalHasLocation;

	private MarkerMapSizer mMapSizer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mShout = getShoutFromBundle(getActivity().getIntent().getExtras());

		mCommentAdapter = new CommentAdapter(getActivity(), null);
		mReshoutAdapter = new ReshoutAdapter(getActivity(), null);

		mCommentMarkerAdapter = new CommentMarkerAdapter(mCommentAdapter);
		mReshoutMarkerAdapter = new ReshoutMarkerAdapter(mReshoutAdapter);

		mCommentObserver = new CommentObserver();
		mReshoutObserver = new ReshoutObserver();
		mMarkerObserver = new MarkerObserver();

		mCommentAdapter.registerDataSetObserver(mCommentObserver);
		mReshoutAdapter.registerDataSetObserver(mReshoutObserver);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle icicle) {
		View v = inflator.inflate(R.layout.fragment_details, container, true);

		mShoutView = (ShoutView) v.findViewById(R.id.shoutview);

		mMapView = (ExpandableView) v.findViewById(R.id.expandable_location);
		mCommentsView = (ExpandableView) v.findViewById(R.id.expandable_comments);
		mReshoutsView = (ExpandableView) v.findViewById(R.id.expandable_reshouts);

		mExpandMap = (ImageButton) v.findViewById(R.id.expand_map);
		mExpandMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				expandMap();
			}
		});

		mMapFragment = (SupportMapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		mMap = mMapFragment.getMap();

		mCommentsList = (FullListView) mCommentsView.findViewById(
				R.id.content);
		mReshoutsList = (FullListView) mReshoutsView.findViewById(
				R.id.content);

		mCommentsList.setAdapter(mCommentAdapter);
		mReshoutsList.setAdapter(mReshoutAdapter);

		if (mMap != null) {
			mReshoutMarkerLayer = new MarkerMapLayer(mMap);
			mReshoutMarkerLayer.setAdapter(mReshoutMarkerAdapter);
			mReshoutMarkerLayer.registerDataSetObserver(mMarkerObserver);

			mCommentMarkerLayer = new MarkerMapLayer(mMap);
			mCommentMarkerLayer.setAdapter(mCommentMarkerAdapter);
			mCommentMarkerLayer.registerDataSetObserver(mMarkerObserver);

			mMapSizer = new MarkerMapSizer(mMap);
			mMapSizer.addMarkerLayer(mCommentMarkerLayer);
			mMapSizer.addMarkerLayer(mReshoutMarkerLayer);

			ORIGINAL_MARKER = BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_RED);
			COMMENT_MARKER = BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
			RESHOUT_MARKER = BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

			onDisplayOriginalLocation();
		} else {
			mExpandMap.setVisibility(View.INVISIBLE);
		}

		getLoaderManager().initLoader(LOADER_COMMENTS, null, this);
		getLoaderManager().initLoader(LOADER_RESHOUTS, null, this);

		mShoutView.bindShout(mShout);

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
		super.onCreateOptionsMenu(menu, inflator);

		inflator.inflate(R.menu.fragment_details, menu);

		// If this isn't an original shout, hide the comment button
		if (!mShout.getType().equals(ShoutType.SHOUT))
			menu.findItem(R.id.menu_comment).setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.menu_comment:
				((AbstractShoutViewActivity) getActivity()).onClickComment(mShout);
				break;
			case R.id.menu_reshout:
				((AbstractShoutViewActivity) getActivity()).onClickReshout(mShout);
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return false;
	}

	private void onDisplayOriginalLocation() {
		// Show the location, if available
		LatLng latLng = getLatLng(mShout);
		if (latLng != null) {
			mOriginalHasLocation = true;
			Marker marker = mMap.addMarker(new
					MarkerOptions().position(latLng).icon(ORIGINAL_MARKER));
			mMapSizer.addMarker(marker);
		}
		onMarkersChanged();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOADER_COMMENTS:
				return new CommentLoader(getActivity(), mShout);
			case LOADER_RESHOUTS:
				return new ReshoutLoader(getActivity(), mShout);
			default:
				throw new IllegalArgumentException("Unknown Loader id " + id);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int id = loader.getId();
		switch (id) {
			case LOADER_COMMENTS:
				mCommentAdapter.swapCursor(data);
				break;
			case LOADER_RESHOUTS:
				mReshoutAdapter.swapCursor(data);
				break;
			default:
				throw new IllegalArgumentException("Unknown Loader id " + id);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		switch (id) {
			case LOADER_COMMENTS:
				mCommentAdapter.swapCursor(null);
				break;
			case LOADER_RESHOUTS:
				mReshoutAdapter.swapCursor(null);
				break;
			default:
				throw new IllegalArgumentException("Unknown Loader id " + id);
		}
	}

	private LocalShout getShoutFromBundle(Bundle bundle) {
		if (bundle == null) {
			throw new IllegalArgumentException("must have a bundle");
			// return null;
		}
		Hash hash = bundle.getParcelable(DetailsActivity.SHOUT_ID);
		if (hash == null) {
			throw new IllegalArgumentException("must have a hash in bundle");
			// return null;
		}
		LocalShout shout = ShoutProviderContract.retrieveShoutByHash(getActivity()
				.getApplicationContext(), hash);
		return shout;
	}

	private void onCommentCountChanged(int count) {
		mCommentsView.setSubheader(String.format("%d", count));
		if (count == 0)
			mCommentsView.setVisibility(View.GONE);
		else
			mCommentsView.setVisibility(View.VISIBLE);
	}

	private void onReshoutCountChanged(int count) {
		mReshoutsView.setSubheader(String.format("%d", count));
		if (count == 0)
			mReshoutsView.setVisibility(View.GONE);
		else
			mReshoutsView.setVisibility(View.VISIBLE);
	}

	private void onMarkersChanged() {
		if (!mOriginalHasLocation
				&& mCommentMarkerLayer.getMarkerCount() + mReshoutMarkerLayer.getMarkerCount() == 0)
			mMapView.setVisibility(View.GONE);
		else
			mMapView.setVisibility(View.VISIBLE);

	}

	public void expandMap() {
		MapActivity.show(getActivity(), mShout);
	}

	/**
	 * Constructs an {@link LatLng} from the location of the provided shout.
	 * 
	 * @param shout the shout whose location to return
	 * @return the location of the shout as a {@code LatLng} or {@code null} if
	 *         the location is not available
	 */
	private static LatLng getLatLng(Shout shout) {
		Location loc = shout.getLocation();
		if (loc != null)
			return new LatLng(loc.getLatitude(), loc.getLongitude());
		else
			return null;
	}

	private class CommentObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			onCommentCountChanged(mCommentAdapter.getCount());
		}

		@Override
		public void onInvalidated() {
			onCommentCountChanged(0);
		}
	}

	private class ReshoutObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			onReshoutCountChanged(mReshoutAdapter.getCount());
		}

		@Override
		public void onInvalidated() {
			onReshoutCountChanged(0);
		}
	}

	private class MarkerObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			onMarkersChanged();
		}

		@Override
		public void onInvalidated() {
			// Ignore
		}

	}

	public static class ShoutView extends RelativeLayout {

		private int mAvatarSize;
		private int mSenderTextAppearance;
		private int mMessageTextAppearance;
		private int mTimestampLabelTextAppearance;
		private int mTimestampTextAppearance;

		private ImageView mAvatar;
		private TextView mSender;
		private TextView mMessage;
		private TextView mSentTimeLabel;
		private TextView mReceivedTimeLabel;
		private TextView mSentTime;
		private TextView mReceivedTime;

		public ShoutView(Context context) {
			this(context, null);
		}

		public ShoutView(Context context, AttributeSet attrs) {
			this(context, attrs, R.attr.detailsFragmentShoutViewStyle);
		}

		public ShoutView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.fragment_details_shout_view, this);

			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.DetailsFragmentShoutView, defStyle, 0);

			mAvatarSize = a.getDimensionPixelSize(
					R.styleable.DetailsFragmentShoutView_avatarSize, 0);
			if (mAvatarSize == 0)
				throw new IllegalArgumentException("You must supply an avatarSize attribute.");

			mSenderTextAppearance = a.getResourceId(
					R.styleable.DetailsFragmentShoutView_senderTextAppearance,
					android.R.style.TextAppearance);

			mMessageTextAppearance = a.getResourceId(
					R.styleable.DetailsFragmentShoutView_messageTextAppearance,
					android.R.style.TextAppearance);

			mTimestampLabelTextAppearance = a.getResourceId(
					R.styleable.DetailsFragmentShoutView_timestampTextAppearance,
					android.R.style.TextAppearance);

			mTimestampTextAppearance = a.getResourceId(
					R.styleable.DetailsFragmentShoutView_timestampTextAppearance,
					android.R.style.TextAppearance);

			a.recycle();

		}

		@Override
		public void onFinishInflate() {
			super.onFinishInflate();

			mAvatar = (ImageView) findViewById(R.id.avatar);
			mSender = (TextView) findViewById(R.id.sender);
			mMessage = (TextView) findViewById(R.id.message);
			mSentTimeLabel = (TextView) findViewById(R.id.sentTimeLabel);
			mReceivedTimeLabel = (TextView) findViewById(R.id.receivedTimeLabel);
			mSentTime = (TextView) findViewById(R.id.sentTime);
			mReceivedTime = (TextView) findViewById(R.id.receivedTime);

			mAvatar.getLayoutParams().height = mAvatarSize;
			mAvatar.getLayoutParams().width = mAvatarSize;
			mSender.setTextAppearance(getContext(), mSenderTextAppearance);
			mMessage.setTextAppearance(getContext(), mMessageTextAppearance);
			mSentTimeLabel.setTextAppearance(getContext(), mTimestampLabelTextAppearance);
			mReceivedTimeLabel.setTextAppearance(getContext(), mTimestampLabelTextAppearance);
			mSentTime.setTextAppearance(getContext(), mTimestampTextAppearance);
			mReceivedTime.setTextAppearance(getContext(), mTimestampTextAppearance);

			mAvatar.requestLayout();
		}

		public void bindShout(LocalShout shout) {
			HashReference<ShoutImage> avatarRef = shout.getSender().getAvatar();
			Uri mUri = ImageProviderContract.imageUri(avatarRef.getHash());
			Picasso.with(this.getContext()).load(mUri.toString())
					.placeholder(R.drawable.defaultavatar)
					.error(R.drawable.defaultavatar)
					.into(mAvatar);

			mSender.setText(shout.getSender().getUsername());

			mMessage.setText(shout.getMessage());
			ShoutLinkify.addLinks(mMessage);
			Linkify.addLinks(mMessage, Linkify.ALL);

			String sentTime;
			if (shout.getTimestamp().isBefore(shout.getReceivedTime())) {
				sentTime = FormattedAge.formatAbsolute(shout.getTimestamp());
			} else {
				sentTime = FormattedAge.formatAbsolute(shout.getReceivedTime());

			}
			mSentTime.setText(sentTime);

			mReceivedTime.setText(FormattedAge.formatAbsolute(shout.getReceivedTime()));
		}

	}

	public static class CommentItem extends RelativeLayout {

		/**
		 * Inflates and returns a new {@link CommentItem}. The returned item is
		 * not attached to the provided {@code root}.
		 * <p>
		 * Why a factory method, instead of a constructor?
		 * <p>
		 * The layout must be inflated before the constructor is called so that
		 * attributes specified in the XML are available during construction.
		 * See the Javadoc for {@link ReshoutItem#create(Context, ViewGroup)}
		 * for details.
		 * 
		 * @param context the context whose {@code LayoutInflater} to use
		 * @param root the object providing the {@code LayoutParams} for the new
		 *            view
		 * @return the new {@code CommentItem}
		 */
		public static final CommentItem create(Context context, ViewGroup root) {
			return (CommentItem) LayoutInflater.from(context).inflate(
					R.layout.fragment_details_comments_item, root, false);
		}

		private int mAvatarSize;
		private int mSenderTextAppearance;
		private int mTimestampTextAppearance;
		private int mMessageTextAppearance;

		private TextView mMessage;
		private TextView mSender;
		private TextView mTimestamp;
		private ImageView mAvatar;

		/**
		 * Use factory method {@link #create(Context, ViewGroup)} instead.
		 */
		public CommentItem(Context context) {
			this(context, null);
		}

		/**
		 * Use factory method {@link #create(Context, ViewGroup)} instead.
		 */
		public CommentItem(Context context, AttributeSet attrs) {
			this(context, attrs, R.attr.detailsFragmentCommentItemStyle);
		}

		/**
		 * Use factory method {@link #create(Context, ViewGroup)} instead.
		 */
		public CommentItem(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);

			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.DetailsFragmentCommentItem, defStyle, 0);

			mMessageTextAppearance = a.getResourceId(
					R.styleable.DetailsFragmentCommentItem_messageTextAppearance, 0);
			if (mMessageTextAppearance == 0)
				throw new IllegalArgumentException(
						"You must supply a messageTextAppearance attribute.");

			mSenderTextAppearance = a.getResourceId(
					R.styleable.DetailsFragmentCommentItem_senderTextAppearance, 0);
			if (mSenderTextAppearance == 0)
				throw new IllegalArgumentException(
						"You must supply a senderTextAppearance attribute.");

			mTimestampTextAppearance = a.getResourceId(
					R.styleable.DetailsFragmentCommentItem_timestampTextAppearance, 0);
			if (mTimestampTextAppearance == 0)
				throw new IllegalArgumentException(
						"You must supply a timestampTextAppearance attribute.");

			mAvatarSize = a.getDimensionPixelSize(
					R.styleable.DetailsFragmentCommentItem_avatarSize, 0);
			if (mAvatarSize == 0)
				throw new IllegalArgumentException("You must supply an avatarSize attribute.");

			a.recycle();
		}

		@Override
		public void onFinishInflate() {
			super.onFinishInflate();

			mMessage = (TextView) findViewById(R.id.message);
			mSender = (TextView) findViewById(R.id.sender);
			mTimestamp = (TextView) findViewById(R.id.timestamp);
			mAvatar = (ImageView) findViewById(R.id.avatar);

			mMessage.setTextAppearance(getContext(), mMessageTextAppearance);
			mSender.setTextAppearance(getContext(), mSenderTextAppearance);
			mTimestamp.setTextAppearance(getContext(), mTimestampTextAppearance);

			mAvatar.getLayoutParams().height = mAvatarSize;
			mAvatar.getLayoutParams().width = mAvatarSize;
			mAvatar.requestLayout();

		}

		public CommentItem bindShout(LocalShout shout) {
			String message = shout.getMessage();
			String sender = shout.getSender().getUsername();
			String timestamp = FormattedAge.formatAbsolute(shout.getTimestamp());
			HashReference<ShoutImage> avatarRef = shout.getSender().getAvatar();

			Uri mUri = ImageProviderContract.imageUri(avatarRef.getHash());
			Picasso.with(this.getContext()).load(mUri.toString())
					.placeholder(R.drawable.defaultavatar)
					.error(R.drawable.defaultavatar)
					.into(mAvatar);

			mMessage.setText(message);
			mSender.setText(sender);
			mTimestamp.setText(timestamp);

			ShoutLinkify.addLinks(mMessage);
			Linkify.addLinks(mMessage, Linkify.ALL);

			return this;
		}

	}

	public static class ReshoutItem extends RelativeLayout {

		/**
		 * Inflates and returns a new {@link ReshoutItem}. The returned item is
		 * not attached to the provided {@code root}.
		 * <p>
		 * Why a factory method, instead of a constructor?
		 * <p>
		 * Attributes of the {@code ReshoutItem} like {@code android:padding}
		 * that are specified in the XML are ignored if the XML is inflated
		 * inside the constructor. (Those attributes would normally be
		 * interpreted by the super class constructor, but it was called before
		 * the XML was even available.)
		 * <p>
		 * Thus, the ordering needs to be XML inflation followed by object
		 * construction, not vice-versa. Using this factory method ensures the
		 * correct order.
		 * 
		 * @param context the context whose {@code LayoutInflater} to use
		 * @param root the object providing the {@code LayoutParams} for the new
		 *            view
		 * @return the new {@code ReshoutItem}
		 */
		public static final ReshoutItem create(Context context, ViewGroup root) {
			return (ReshoutItem) LayoutInflater.from(context).inflate(
					R.layout.fragment_details_reshouts_item,
					root,
					false);
		}

		private int mAvatarSize;
		private int mSenderTextAppearance;
		private int mTimestampTextAppearance;

		private TextView mSender;
		private TextView mTimestamp;
		private ImageView mAvatar;

		/**
		 * Use factory method {@link #create(Context, ViewGroup)} instead.
		 */
		public ReshoutItem(Context context) {
			this(context, null);
		}

		/**
		 * Use factory method {@link #create(Context, ViewGroup)} instead.
		 */
		public ReshoutItem(Context context, AttributeSet attrs) {
			this(context, attrs, R.attr.detailsFragmentReshoutItemStyle);
		}

		/**
		 * Use factory method {@link #create(Context, ViewGroup)} instead.
		 */
		public ReshoutItem(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);

			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.DetailsFragmentReshoutItem, defStyle, 0);

			mSenderTextAppearance = a.getResourceId(
					R.styleable.DetailsFragmentReshoutItem_senderTextAppearance, 0);
			if (mSenderTextAppearance == 0)
				throw new IllegalArgumentException(
						"You must supply a senderTextAppearance attribute.");

			mTimestampTextAppearance = a.getResourceId(
					R.styleable.DetailsFragmentReshoutItem_timestampTextAppearance, 0);
			if (mTimestampTextAppearance == 0)
				throw new IllegalArgumentException(
						"You must supply a timestampTextAppearance attribute.");

			mAvatarSize = a.getDimensionPixelSize(
					R.styleable.DetailsFragmentReshoutItem_avatarSize, 0);
			if (mAvatarSize == 0)
				throw new IllegalArgumentException("You must supply an avatarSize attribute.");

			a.recycle();
		}

		@Override
		public void onFinishInflate() {
			super.onFinishInflate();

			mSender = (TextView) findViewById(R.id.sender);
			mTimestamp = (TextView) findViewById(R.id.timestamp);
			mAvatar = (ImageView) findViewById(R.id.avatar);

			mSender.setTextAppearance(getContext(), mSenderTextAppearance);
			mTimestamp.setTextAppearance(getContext(), mTimestampTextAppearance);

			mAvatar.getLayoutParams().height = mAvatarSize;
			mAvatar.getLayoutParams().width = mAvatarSize;
			mAvatar.requestLayout();

		}

		public ReshoutItem bindShout(LocalShout shout) {
			String sender = shout.getSender().getUsername();
			String timestamp = FormattedAge.formatAbsolute(shout.getTimestamp());
			HashReference<ShoutImage> avatarRef = shout.getSender().getAvatar();

			Uri mUri = ImageProviderContract.imageUri(avatarRef.getHash());
			Picasso.with(this.getContext()).load(mUri.toString())
					.placeholder(R.drawable.defaultavatar)
					.error(R.drawable.defaultavatar)
					.into(mAvatar);

			mSender.setText(sender);
			mTimestamp.setText(timestamp);

			return this;
		}

	}

	/**
	 * Loader for the comments.
	 */
	private static class CommentLoader extends CursorLoader {
		/* Android does not allow non-static inner class Loaders */

		private Shout mShout;

		public CommentLoader(Context context, Shout shout) {
			super(context);
			this.mShout = shout;
		}

		@Override
		public Cursor onLoadCursor() {
			return ShoutProviderContract.getComments(getContext(), mShout);
		}

	}

	/**
	 * Loader for the reshouts
	 */
	private static class ReshoutLoader extends CursorLoader {
		/* Android does not allow non-static inner class Loaders */

		private Shout mShout;

		public ReshoutLoader(Context context, Shout shout) {
			super(context);
			this.mShout = shout;
		}

		@Override
		public Cursor onLoadCursor() {
			return ShoutProviderContract.getReshouts(getContext(), mShout);
		}

	}

	/**
	 * Adapter for comments
	 */
	private static class CommentAdapter extends ShoutCursorAdapter {

		public CommentAdapter(Context context, Cursor c) {
			super(context, c, 0);
		}

		@Override
		public void bindView(View view, Context context, LocalShout shout) {
			CommentItem item = (CommentItem) view;
			item.bindShout(shout);
		}

		@Override
		public View newView(final Context context, LocalShout shout,
				ViewGroup parent) {
			return CommentItem.create(context, parent);
		}
	}

	/**
	 * Adapter for reshouts
	 */
	private static class ReshoutAdapter extends ShoutCursorAdapter {

		public ReshoutAdapter(Context context, Cursor c) {
			super(context, c, 0);
		}

		@Override
		public void bindView(View view, Context context, LocalShout shout) {
			ReshoutItem item = (ReshoutItem) view;
			item.bindShout(shout);
		}

		@Override
		public View newView(final Context context, LocalShout shou,
				ViewGroup parent) {
			return ReshoutItem.create(context, parent);
		}
	}

	private static class ReshoutMarkerAdapter extends WrappingItemAdapter<MarkerOptions> {

		private ShoutCursorAdapter mWrappedAdapter;

		public ReshoutMarkerAdapter(ShoutCursorAdapter adapter) {
			super(adapter);
			mWrappedAdapter = adapter;
		}

		@Override
		public LocalShout getItem(int position) {
			return mWrappedAdapter.getItem(position);
		}

		@Override
		public MarkerOptions get(int position) {
			LocalShout shout = getItem(position);
			LatLng latlng = getLatLng(shout);
			if (latlng != null)
				return new MarkerOptions().position(latlng).icon(RESHOUT_MARKER)
						.title(shout.getSender().getUsername());
			else
				return null;
		}
	}

	private static class CommentMarkerAdapter extends WrappingItemAdapter<MarkerOptions> {

		private ShoutCursorAdapter mWrappedAdapter;

		public CommentMarkerAdapter(ShoutCursorAdapter adapter) {
			super(adapter);
			mWrappedAdapter = adapter;
		}

		@Override
		public LocalShout getItem(int position) {
			return mWrappedAdapter.getItem(position);
		}

		@Override
		public MarkerOptions get(int position) {
			LocalShout shout = getItem(position);
			LatLng latlng = getLatLng(shout);
			if (latlng != null)
				return new MarkerOptions().position(latlng).icon(COMMENT_MARKER)
						.title(shout.getSender().getUsername()).snippet(shout.getMessage());
			else
				return null;
		}
	}
}
