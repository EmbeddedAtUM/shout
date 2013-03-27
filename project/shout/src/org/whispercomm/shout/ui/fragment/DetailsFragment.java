
package org.whispercomm.shout.ui.fragment;

import java.util.HashSet;
import java.util.Set;

import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.CursorLoader;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.ui.DetailsActivity;
import org.whispercomm.shout.ui.widget.ExpandableView;
import org.whispercomm.shout.ui.widget.FullListView;
import org.whispercomm.shout.ui.widget.ShoutView;
import org.whispercomm.shout.util.FormattedAge;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetailsFragment extends SherlockFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOADER_COMMENTS = 0;
	private static final int LOADER_RESHOUTS = 1;

	/*
	 * The BitmapDescriptorFactory requires a Context, so these can't be
	 * configured statically
	 */
	private BitmapDescriptor ORIGINAL_MARKER;
	private BitmapDescriptor COMMENT_MARKER;
	private BitmapDescriptor RESHOUT_MARKER;

	private ShoutView mShoutView;

	@SuppressWarnings("unused")
	private ExpandableView mMapView;
	// Place the sender's marker
	private ExpandableView mCommentsView;
	private ExpandableView mReshoutsView;

	private SupportMapFragment mMapFragment;
	private GoogleMap mMap;
	private FullListView mCommentsList;
	private FullListView mReshoutsList;

	private LocalShout mShout;

	private CommentAdapter mCommentAdapter;
	private ReshoutAdapter mReshoutAdapter;

	private CommentObserver mCommentObserver;
	private ReshoutObserver mReshoutObserver;

	private Set<LatLng> mLatLngReshouts = new HashSet<LatLng>();
	private Set<LatLng> mLatLngComments = new HashSet<LatLng>();

	private boolean mLayout = false;
	private Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mShout = getShoutFromBundle(getActivity().getIntent().getExtras());

		mCommentAdapter = new CommentAdapter(getActivity(), null);
		mReshoutAdapter = new ReshoutAdapter(getActivity(), null);

		mCommentObserver = new CommentObserver();
		mReshoutObserver = new ReshoutObserver();

		mCommentAdapter.registerDataSetObserver(mCommentObserver);
		mReshoutAdapter.registerDataSetObserver(mReshoutObserver);

		mHandler = new Handler(Looper.getMainLooper());
	}

	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle icicle) {
		View v = inflator.inflate(R.layout.fragment_details, container, true);

		mShoutView = (ShoutView) v.findViewById(R.id.shoutview);

		mMapView = (ExpandableView) v.findViewById(R.id.expandable_location);
		mCommentsView = (ExpandableView) v.findViewById(R.id.expandable_comments);
		mReshoutsView = (ExpandableView) v.findViewById(R.id.expandable_reshouts);

		mMapFragment = (SupportMapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		mMap = mMapFragment.getMap();

		mCommentsList = (FullListView) mCommentsView.findViewById(
				R.id.content);
		mReshoutsList = (FullListView) mReshoutsView.findViewById(
				R.id.content);

		mCommentsList.setAdapter(mCommentAdapter);
		mReshoutsList.setAdapter(mReshoutAdapter);

		getLoaderManager().initLoader(LOADER_COMMENTS, null, this);
		getLoaderManager().initLoader(LOADER_RESHOUTS, null, this);

		ORIGINAL_MARKER = BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_RED);
		COMMENT_MARKER = BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
		RESHOUT_MARKER = BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

		mShoutView.bindShout(mShout);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		mLayout = true;
		onDisplayOriginalLocation();

		// // Place the sender's marker
		// LatLng latLng = getLatLng(mShout);
		// if (latLng != null) {
		// CameraPosition pos = new CameraPosition(latLng, 14, 0, 0);
		// mMap.addMarker(new MarkerOptions().position(latLng));
		// mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
		//
		// // Add comment markers
		// for (Shout comment : mShout.getComments()) {
		// mMap.addMarker(new MarkerOptions().position(getLatLng(comment)).icon(
		// BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
		// }
		//
		// // Add reshouter markers
		// Cursor c = ShoutProviderContract.getReshouts(getActivity(), mShout);
		// c.moveToFirst();
		// while (!c.isAfterLast()) {
		// mMap.addMarker(new MarkerOptions()
		// .position(
		// getLatLng(
		// ShoutProviderContract.retrieveShoutFromCursor(getActivity(), c)))
		// .icon(
		// BitmapDescriptorFactory
		// .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
		// c.moveToNext();
		// }
		// }
	}

	private void onDisplayOriginalLocation() {
		// Show the location, if available
		LatLng latLng = getLatLng(mShout);
		if (latLng != null) {
			CameraPosition pos = new CameraPosition(latLng, 14, 0, 0);
			mMap.addMarker(new MarkerOptions().position(latLng).icon(ORIGINAL_MARKER));
			mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
		}
	}

	private void onDisplayReshoutLocations(ReshoutAdapter reshouts) {
		int count = reshouts.getCount();
		for (int i = 0; i < count; ++i) {
			LocalShout reshout = reshouts.getItem(i);
			LatLng latlng = getLatLng(reshout);
			if (latlng != null && !mLatLngReshouts.contains(latlng)) {
				mLatLngReshouts.add(latlng);
				mMap.addMarker(new MarkerOptions().position(latlng).icon(RESHOUT_MARKER)
						.title(reshout.getSender().getUsername()));
			}
		}
		if (count > 1)
			zoomMap();
	}

	private void onDisplayCommentLocations(CommentAdapter comments) {
		int count = comments.getCount();
		for (int i = 0; i < count; ++i) {
			LocalShout comment = comments.getItem(i);
			LatLng latlng = getLatLng(comment);
			if (latlng != null && !mLatLngComments.contains(latlng)) {
				mLatLngComments.add(latlng);
				mMap.addMarker(new MarkerOptions().position(latlng).icon(COMMENT_MARKER)
						.title(comment.getSender().getUsername()).snippet(comment.getMessage()));
			}
		}
		if (count > 1)
			zoomMap();
	}

	private void zoomMap() {
		return;
		// if (mLayout) {
		// LatLngBounds.Builder bb = new LatLngBounds.Builder();
		//
		// LatLng original = getLatLng(mShout);
		// if (original != null)
		// bb.include(original);
		//
		// for (LatLng loc : mLatLngComments) {
		// bb.include(loc);
		// }
		//
		// for (LatLng loc : mLatLngReshouts) {
		// bb.include(loc);
		// }
		//
		// mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bb.build(),
		// 50));
		// }
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
			return null;
		}
		byte[] hash = bundle.getByteArray(DetailsActivity.SHOUT_ID);
		if (hash == null) {
			return null;
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
		onDisplayCommentLocations(mCommentAdapter);
	}

	private void onReshoutCountChanged(int count) {
		mReshoutsView.setSubheader(String.format("%d", count));
		if (count == 0)
			mReshoutsView.setVisibility(View.GONE);
		else
			mReshoutsView.setVisibility(View.VISIBLE);
		onDisplayReshoutLocations(mReshoutAdapter);
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
			HashReference<Avatar> avatarRef = shout.getSender().getAvatar();

			mMessage.setText(message);
			mSender.setText(sender);
			mTimestamp.setText(timestamp);
			if (avatarRef.isAvailable())
				mAvatar.setImageBitmap(avatarRef.get().getBitmap());

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
			HashReference<Avatar> avatarRef = shout.getSender().getAvatar();

			mSender.setText(sender);
			mTimestamp.setText(timestamp);
			if (avatarRef.isAvailable())
				mAvatar.setImageBitmap(avatarRef.get().getBitmap());

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
	private static class CommentAdapter extends CursorAdapter {

		public CommentAdapter(Context context, Cursor c) {
			super(context, c, 0);
		}

		@Override
		public LocalShout getItem(int pos) {
			Cursor c = (Cursor) super.getItem(pos);
			return ShoutProviderContract.retrieveShoutFromCursor(mContext, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			CommentItem item = (CommentItem) view;
			final LocalShout shout = ShoutProviderContract.retrieveShoutFromCursor(
					context, cursor);
			item.bindShout(shout);
		}

		@Override
		public View newView(final Context context, Cursor cursor,
				ViewGroup parent) {
			return CommentItem.create(context, parent);
		}
	}

	/**
	 * Adapter for reshouts
	 */
	private static class ReshoutAdapter extends CursorAdapter {

		public ReshoutAdapter(Context context, Cursor c) {
			super(context, c, 0);
		}

		@Override
		public LocalShout getItem(int pos) {
			Cursor c = (Cursor) super.getItem(pos);
			return ShoutProviderContract.retrieveShoutFromCursor(mContext, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ReshoutItem item = (ReshoutItem) view;
			final LocalShout shout = ShoutProviderContract.retrieveShoutFromCursor(
					context, cursor);
			item.bindShout(shout);
		}

		@Override
		public View newView(final Context context, Cursor cursor,
				ViewGroup parent) {
			return ReshoutItem.create(context, parent);
		}
	}
}
