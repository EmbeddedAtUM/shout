
package org.whispercomm.shout.ui.widget;

import java.util.ArrayList;

import org.whispercomm.shout.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

/**
 * A view that shows items in a list. The items come from the
 * {@link ListAdapter} associated with this view. All items are rendered to
 * views, unlike {@link android.widget.ListView ListView}, which only renders
 * the currently-visible items.
 * <p>
 * This view supports the same {@code divider} and {@code dividerHeight} XML
 * attributes as {code ListView}.
 * <p>
 * Much of this class was adapted from the source of
 * {@link android.widget.ListView ListView}.
 */
public class FullListView extends LinearLayout {

	/**
	 * View type for the dividers between items
	 */
	public static final int ITEM_VIEW_TYPE_DIVIDER = -3;

	int mItemCount;
	int mOldItemCount;

	ListAdapter mAdapter;
	DataSetObserver mDataSetObserver;

	Drawable mDivider;
	int mDividerHeight;

	final RecycleBin mRecycler = new RecycleBin();

	final boolean[] mIsScrap = new boolean[1];

	public FullListView(Context context) {
		this(context, null);
	}

	public FullListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FullListView(Context context, AttributeSet attrs, int defStyle) {
		/*
		 * When we upgrade to min API 11, change to super(context, attrs,
		 * defStyle);
		 */
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FullListView, defStyle, 0);

		final Drawable d = a.getDrawable(R.styleable.FullListView_android_divider);
		if (d != null) {
			// If a divider is specified, use its intrinsic height for divider
			// height
			setDivider(d);
		}

		// Use the height specified, zero being the default
		final int dividerHeight = a.getDimensionPixelSize(
				R.styleable.FullListView_android_dividerHeight, 0);
		if (dividerHeight != 0) {
			setDividerHeight(dividerHeight);
		}

		a.recycle();
	}

	/**
	 * Returns the adapter currently in use in this ListView.
	 * 
	 * @return The adapter currently used to display data in this ListView.
	 * @see #setAdapter(ListAdapter)
	 */
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	/**
	 * Sets the data behind this ListView.
	 * 
	 * @param adapter The ListAdapter which is responsible for maintaining the
	 *            data backing this list and for producing a view to represent
	 *            an item in that data set.
	 * @see #getAdapter()
	 */
	public void setAdapter(ListAdapter adapter) {
		if (mAdapter != null && mDataSetObserver != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		resetList();

		mAdapter = adapter;

		if (mAdapter != null) {
			mOldItemCount = mItemCount;
			mItemCount = mAdapter.getCount();

			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

			mRecycler.setViewTypeCount(mAdapter.getViewTypeCount());
		}

		fillList();
	}

	/**
	 * Returns the drawable that will be drawn between each item in the list.
	 * 
	 * @return the current drawable drawn between list elements
	 */
	public Drawable getDivider() {
		return mDivider;
	}

	/**
	 * Sets the drawable that will be drawn between each item in the list. If
	 * the drawable does not have an intrinsic height, you should also call
	 * {@link #setDividerHeight(int)}
	 * 
	 * @param divider The drawable to use.
	 */
	public void setDivider(Drawable divider) {
		if (divider != null) {
			mDividerHeight = divider.getIntrinsicHeight();
		} else {
			mDividerHeight = 0;
		}
		mDivider = divider;

		resetList();

		mRecycler.setDivider();
		fillList();
	}

	/**
	 * @return Returns the height of the divider that will be drawn between each
	 *         item in the list.
	 */
	public int getDividerHeight() {
		return mDividerHeight;
	}

	/**
	 * Sets the height of the divider that will be drawn between each item in
	 * the list. Calling this will override the intrinsic height as set by
	 * {@link #setDivider(Drawable)}
	 * 
	 * @param height The new height of the divider in pixels.
	 */
	public void setDividerHeight(int height) {
		mDividerHeight = height;

		resetList();

		mRecycler.setDivider();
		fillList();
	}

	/**
	 * Override to prevent freezing of any views created by the adapter.
	 */
	@Override
	protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
		dispatchFreezeSelfOnly(container);
	}

	/**
	 * Override to prevent thawing of any views created by the adapter.
	 */
	@Override
	protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
		dispatchThawSelfOnly(container);
	}

	/**
	 * Fills the linear layout with all the items from the adapter.
	 * <p>
	 * First, adds existing views to the recycle bin and detaches them from this
	 * ViewGroup. Then, repopulates the ViewGroup. Finally, frees any remaining
	 * scrap views.
	 */
	private void fillList() {
		// Recycle old views
		mRecycler.fillScrapViews();
		detachAllViewsFromParent();

		// Replace with new views
		if (mItemCount > 0) {
			makeAndAddView(0);
		}
		for (int i = 1; i < mItemCount; i++) {
			if (mDivider != null) {
				makeAndAddDivider();
			}
			makeAndAddView(i);
		}

		// Drop any unused scrap views
		mRecycler.clear();

		// Redraw
		requestLayout();
		invalidate();
	}

	/**
	 * @return a new divider view
	 */
	private View createDivider() {
		View divider = new View(getContext());
		divider.setBackgroundDrawable(mDivider);
		return divider;
	}

	/**
	 * Get a divider.
	 * 
	 * @param isScrap Array of at least 1 boolean, the first entry will become
	 *            true if the returned view was taken from the scrap heap, false
	 *            if otherwise.
	 * @return A divider view
	 */
	View obtainDivider(boolean[] isScrap) {
		isScrap[0] = true;
		View divider;

		divider = mRecycler.getScrapDivider();
		if (divider == null) {
			divider = createDivider();
			isScrap[0] = false;
		}

		return divider;
	}

	/**
	 * Obtain the view and add it to our list of children. The view can be made
	 * fresh or converted from an unused view.
	 * 
	 * @param position Logical position in the list
	 * @return View that was added
	 */
	private View makeAndAddDivider() {
		View divider = obtainDivider(mIsScrap);

		// This needs to be positioned and measured
		setupChild(divider, true, -1, mIsScrap[0]);

		return divider;
	}

	/**
	 * Get a view and have it show the data associated with the specified
	 * position.
	 * 
	 * @param position The position to display
	 * @param isScrap Array of at least 1 boolean, the first entry will become
	 *            true if the returned view was taken from the scrap heap, false
	 *            if otherwise.
	 * @return A view displaying the data associated with the specified position
	 */
	View obtainView(int position, boolean[] isScrap) {
		isScrap[0] = false;
		View scrapView;

		scrapView = mRecycler.getScrapView(position);

		View child;
		if (scrapView != null) {
			child = mAdapter.getView(position, scrapView, this);
			if (child != scrapView) {
				mRecycler.addScrapView(scrapView);
			} else {
				isScrap[0] = true;
			}
		} else {
			child = mAdapter.getView(position, null, this);
		}

		return child;
	}

	/**
	 * Obtain the view and add it to our list of children. The view can be made
	 * fresh or converted from an unused view.
	 * 
	 * @param position Logical position in the list
	 * @return View that was added
	 */
	private View makeAndAddView(int position) {
		View child;

		// Make a new view for this position, or convert an unused view if
		// possible
		child = obtainView(position, mIsScrap);

		// This needs to be positioned and measured
		setupChild(child, false, position, mIsScrap[0]);

		return child;
	}

	/**
	 * Add a view as a child.
	 * 
	 * @param child The view to add
	 * @param position The position of this child
	 * @param divider Is the child a divider
	 * @param recycled Has this view been pulled from the recycle bin?
	 */
	private void setupChild(View child, boolean divider, int position, boolean recycled) {
		// Respect layout params that are already in the view. Otherwise make
		// some up...
		FullListView.LayoutParams p = (FullListView.LayoutParams) child.getLayoutParams();
		if (p == null) {
			p = generateDefaultLayoutParams();
		}

		if (divider) {
			p.viewType = ITEM_VIEW_TYPE_DIVIDER;
			p.height = mDividerHeight;
		} else {
			p.viewType = mAdapter.getItemViewType(position);
		}

		if (recycled) {
			attachViewToParent(child, -1, p);
		} else {
			addViewInLayout(child, -1, p, true);
		}

	}

	/**
	 * Remove all views from layout
	 */
	void resetList() {
		removeAllViews();
		invalidate();
	}

	@Override
	protected FullListView.LayoutParams generateDefaultLayoutParams() {
		return new FullListView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, 0);
	}

	@Override
	protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new FullListView.LayoutParams(getContext(), attrs);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof FullListView.LayoutParams;
	}

	/**
	 * The RecycleBin facilitates reuse of views across changes to the
	 * underlying data. Before the list is redrawn, all child views are moved
	 * into the RecycleBin via {@link #fillScrapViews()} and then detached from
	 * the FullListView. When re-populating the list, the old views can be
	 * obtained for conversion and reattachment via {@link #getScrapView(int)}.
	 */
	class RecycleBin {

		/**
		 * Views that can be used for convert view
		 */
		private ArrayList<View>[] mScrapViews;

		private ArrayList<View> mScrapDividers;

		private int mViewTypeCount;

		public void setViewTypeCount(int viewTypeCount) {
			if (viewTypeCount < 1) {
				throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
			}

			@SuppressWarnings("unchecked")
			ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
			for (int i = 0; i < viewTypeCount; i++) {
				scrapViews[i] = new ArrayList<View>();
			}

			mViewTypeCount = viewTypeCount;
			mScrapViews = scrapViews;
		}

		public void setDivider() {
			mScrapDividers = new ArrayList<View>();
		}

		/**
		 * Clears the scrap views
		 */
		void clear() {
			final int dividerCount = mScrapDividers.size();
			for (int i = 0; i < dividerCount; i++) {
				removeDetachedView(mScrapDividers.remove(dividerCount - 1 - i), false);
			}

			final int typeCount = mViewTypeCount;
			for (int i = 0; i < typeCount; i++) {
				final ArrayList<View> scrap = mScrapViews[i];
				final int scrapCount = scrap.size();
				for (int j = 0; j < scrapCount; j++) {
					removeDetachedView(scrap.remove(scrapCount - 1 - j), false);
				}
			}
		}

		/**
		 * Fill ScrapViews with all of the children of the FullListView.
		 * <p>
		 * NOTE: The caller is responsible for detaching the children after this
		 * method returns.
		 */
		void fillScrapViews() {
			final int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				addScrapView(child);
			}
		}

		/**
		 * Put a view into the ScrapViews list. These views are unordered.
		 * 
		 * @param scrap The view to add
		 */
		void addScrapView(View scrap) {
			FullListView.LayoutParams lp = (FullListView.LayoutParams) scrap.getLayoutParams();
			if (lp == null) {
				return;
			}

			if (lp.viewType == ITEM_VIEW_TYPE_DIVIDER) {
				mScrapDividers.add(scrap);
				return;
			}

			int viewType = lp.viewType;
			mScrapViews[viewType].add(scrap);
		}

		/**
		 * @return A view from the scrap dividers collection.
		 */
		View getScrapDivider() {
			return retrieveFromScrap(mScrapDividers);
		}

		/**
		 * @return A view from the ScrapViews collection. These are unordered.
		 */
		View getScrapView(int position) {
			int whichScrap = mAdapter.getItemViewType(position);
			if (whichScrap >= 0 && whichScrap < mScrapViews.length) {
				return retrieveFromScrap(mScrapViews[whichScrap]);
			}
			return null;
		}

		private View retrieveFromScrap(ArrayList<View> scrapViews) {
			int size = scrapViews.size();
			if (size > 0) {
				return scrapViews.remove(size - 1);
			} else {
				return null;
			}
		}

	}

	class AdapterDataSetObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			mItemCount = getAdapter().getCount();
			fillList();
		}

		@Override
		public void onInvalidated() {
			// Data is invalid so we should reset our state
			mItemCount = 0;
			fillList();
		}

	}

	/**
	 * FullListView extends LayoutParams to provide a place to hold the view
	 * type.
	 */
	public static class LayoutParams extends LinearLayout.LayoutParams {

		/**
		 * View type for this view, as returned by
		 * {@link android.widget.Adapter#getItemViewType(int) }
		 */
		int viewType;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}

		public LayoutParams(int w, int h) {
			super(w, h);
		}

		public LayoutParams(int w, int h, int viewType) {
			super(w, h);
			this.viewType = viewType;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

	}
}
