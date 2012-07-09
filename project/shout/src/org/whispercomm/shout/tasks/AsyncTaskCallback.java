
package org.whispercomm.shout.tasks;

import android.os.AsyncTask;

/**
 * An subclass of {@link AsyncTask} that accepts callback to invoke with the
 * result of the background computation.
 * <p>
 * Using the callback pattern, instead of
 * {@link AsyncTask#onPostExecute(Result)}, allow one subclass to be shared
 * among multiple activities that handle the result in different ways.
 * <p>
 * Currently only {@code AsyncTask#onPostExecute(Result)} is exposed via a
 * callback. Otherwise methods like {@link AsyncTask#onPreExecute()} could be
 * supported as well, but we haven't needed them yet.
 * 
 * @see AsyncTask
 * @author David R. Bild
 * @param <Params> the type of the parameters sent to the task upon execution
 * @param <Progress> the type of the progress units published during the
 *            background computation
 * @param <Result> the type of the result of the background computation
 */
public abstract class AsyncTaskCallback<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {

	private AsyncTaskCompleteListener<Result> completeListener;

	public AsyncTaskCallback(AsyncTaskCompleteListener<Result> completeListener) {
		this.completeListener = completeListener;
	}

	@Override
	protected final void onPostExecute(Result result) {
		if (completeListener != null) {
			completeListener.onComplete(result);
		}
	}

	/**
	 * Interface definition for a callback to be invoked with the result of the
	 * send operation.
	 * <p>
	 * This callback should be used in place of the
	 * {@link AsyncTask#onPostExecute(Result)} method.
	 * 
	 * @author David R. Bild
	 */
	public interface AsyncTaskCompleteListener<Result> {
		/**
		 * Called when the task has completed.
		 * <p>
		 * This method will be invoked on the main application thread.
		 * 
		 * @param result the result of
		 *            {@link AsyncTask#doInBackground(Object...)}.
		 */
		public void onComplete(Result result);
	}

}
