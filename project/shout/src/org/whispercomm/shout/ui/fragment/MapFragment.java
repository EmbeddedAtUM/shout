
package org.whispercomm.shout.ui.fragment;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.CursorLoader;
import org.whispercomm.shout.provider.ShoutCursorAdapter;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.ui.MapActivity;
import org.whispercomm.shout.ui.fragment.DetailsFragment.CommentItem;
import org.whispercomm.shout.ui.fragment.DetailsFragment.ReshoutItem;
import org.whispercomm.shout.ui.widget.MarkerMapLayer;
import org.whispercomm.shout.ui.widget.MarkerMapSizer;
import org.whispercomm.shout.ui.widget.WrappingItemAdapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends SherlockFragment implements
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

	private LocalShout mShout;

	private SupportMapFragment mMapFragment;
	private GoogleMap mMap;

	private CommentAdapter mCommentAdapter;
	private ReshoutAdapter mReshoutAdapter;

	private CommentMarkerAdapter mCommentMarkerAdapter;
	private ReshoutMarkerAdapter mReshoutMarkerAdapter;

	private MarkerMapLayer mCommentMarkerLayer;
	private MarkerMapLayer mReshoutMarkerLayer;

	private MarkerMapSizer mMapSizer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mShout = getShoutFromBundle(getActivity().getIntent().getExtras());

		mCommentAdapter = new CommentAdapter(getActivity(), null);
		mReshoutAdapter = new ReshoutAdapter(getActivity(), null);

		mCommentMarkerAdapter = new CommentMarkerAdapter(mCommentAdapter);
		mReshoutMarkerAdapter = new ReshoutMarkerAdapter(mReshoutAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle icicle) {
		View v = inflator.inflate(R.layout.fragment_map, container, true);

		mMapFragment = (SupportMapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		mMap = mMapFragment.getMap();

		mReshoutMarkerLayer = new MarkerMapLayer(mMap);
		mReshoutMarkerLayer.setAdapter(mReshoutMarkerAdapter);

		mCommentMarkerLayer = new MarkerMapLayer(mMap);
		mCommentMarkerLayer.setAdapter(mCommentMarkerAdapter);

		mMapSizer = new MarkerMapSizer(mMap, false);
		mMapSizer.addMarkerLayer(mCommentMarkerLayer);
		mMapSizer.addMarkerLayer(mReshoutMarkerLayer);

		getLoaderManager().initLoader(LOADER_COMMENTS, null, this);
		getLoaderManager().initLoader(LOADER_RESHOUTS, null, this);

		ORIGINAL_MARKER = BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_RED);
		COMMENT_MARKER = BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
		RESHOUT_MARKER = BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

		onDisplayOriginalLocation();

		return v;
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

	private void onDisplayOriginalLocation() {
		// Show the location, if available
		LatLng latLng = getLatLng(mShout);
		if (latLng != null) {
			Marker marker = mMap.addMarker(new
					MarkerOptions().position(latLng).icon(ORIGINAL_MARKER)
							.title(mShout.getSender().getUsername()).snippet(mShout.getMessage()));
			mMapSizer.addMarker(marker);
		}
	}

	private LocalShout getShoutFromBundle(Bundle bundle) {
		if (bundle == null) {
			return null;
		}
		byte[] hash = bundle.getByteArray(MapActivity.SHOUT_ID);
		if (hash == null) {
			return null;
		}
		LocalShout shout = ShoutProviderContract.retrieveShoutByHash(getActivity()
				.getApplicationContext(), hash);
		return shout;
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
