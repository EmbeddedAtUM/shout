
package org.whispercomm.shout.ui.widget;

import android.database.DataSetObserver;
import android.widget.Adapter;

/**
 * An abstract base implementation of {@link ItemAdapter} that delegates to an
 * underlying {@link Adapter}. Rather than copying and modifying all the
 * {@link Adapter} implementations, it's easier to just wrap an instance of one
 * with a WrappingItemAdapter.
 * 
 * @param <T> the object type to which to convert the underlying data
 * @see WrappingItemAdapter
 */
public abstract class WrappingItemAdapter<T> implements ItemAdapter<T> {

	private Adapter mWrappedAdapter;

	/**
	 * Creates a new WrappingItmAdapter using the supplied {@link Adapter} as
	 * the data source.
	 * 
	 * @param adapter the adapter to use as the underlying data source
	 */
	public WrappingItemAdapter(Adapter adapter) {
		mWrappedAdapter = adapter;
	}

	/**
	 * @return the Adapter serving as the underlying data source
	 */
	protected Adapter getWrappedAdapter() {
		return mWrappedAdapter;
	}

	@Override
	public int getCount() {
		return mWrappedAdapter.getCount();
	}

	@Override
	public Object getItem(int position) {
		return mWrappedAdapter.getItem(position);
	}

	@Override
	public boolean isEmpty() {
		return mWrappedAdapter.isEmpty();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mWrappedAdapter.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mWrappedAdapter.unregisterDataSetObserver(observer);
	}

}
