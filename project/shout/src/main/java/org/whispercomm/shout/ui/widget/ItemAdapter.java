
package org.whispercomm.shout.ui.widget;

import android.database.DataSetObserver;

/**
 * A more generic version of {@link android.widget.Adapter Adapter}. Instead of
 * just adapting the underlying data to a {@link android.view.View View}, the
 * data can be converted to any type, as specified by the generic parameter.
 * 
 * @param <T> the object type to which to convert the underlying data
 * @see WrappingItemAdapter
 */
public interface ItemAdapter<T> {
	/* Much of the javadoc content is copied from android.widget.Adapter */

	/**
	 * How many items are in the data set represented by this ItemAdapter.
	 * 
	 * @return count of items
	 */
	public int getCount();

	/**
	 * Get the data item associated with this specified position in the data
	 * set.
	 * 
	 * @param position the position of the item we want within the adapter's
	 *            data set
	 * @return the data at the specified position
	 */
	public Object getItem(int position);

	/**
	 * Get the adapted representation of the data at the specified position in
	 * the data set.
	 * 
	 * @param position the position of the item we want within the adapter's
	 *            data set
	 * @return the adapted representation of the data at the specified position
	 */
	public T get(int position);

	/**
	 * @return {code true} if this adapter doesn't contain any data
	 */
	public boolean isEmpty();

	/**
	 * Register an observer that is called when changes happen to the data used
	 * by this adapter.
	 * 
	 * @param observer the object that gets notified when the data set changes
	 */
	public void registerDataSetObserver(DataSetObserver observer);

	/**
	 * Unregister an observer that has previously been registered with this
	 * adapter via {@link #registerDataSetObserver(DataSetObserver)}.
	 * 
	 * @param observer the object to unregister
	 */
	public void unregisterDataSetObserver(DataSetObserver observer);

}
