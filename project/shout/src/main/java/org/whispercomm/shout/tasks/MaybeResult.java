
package org.whispercomm.shout.tasks;

/**
 * An object can hold either a result or an exception.
 * <p>
 * The {@code AsyncTask} object does not provide a way to pass an exception from
 * the {@code doInBackground()} method to the {@code onPostExecute} method. This
 * class can be used as the result to pass either the actual result or an
 * exception.
 * 
 * @author David R. Bild
 * @param <T>
 */
public class MaybeResult<T> {
	private T result;
	private Throwable throwable;

	/**
	 * Creates an {@code MaybeResult} wrapping an empty result.
	 * <p>
	 * Useful if type parameter {@code T} is {@code Void}.
	 */
	public MaybeResult() {
		this.result = null;
		this.throwable = null;
	}

	/**
	 * Creates an {@code MaybeResult} wrapping the specified result.
	 * 
	 * @param result the result to be wrapped.
	 */
	public MaybeResult(T result) {
		this.result = result;
		this.throwable = null;
	}

	/**
	 * Creates an {@code MaybeResult} wrapping the specified exception.
	 * 
	 * @param throwable the exception to be wrapped.
	 */
	public MaybeResult(Throwable throwable) {
		this.result = null;
		this.throwable = throwable;
	}

	/**
	 * Gets the result, if the generating operation succeeded.
	 * 
	 * @return the result or {@code null} if an exception was thrown.
	 */
	public T getResult() {
		return result;
	}

	/**
	 * Gets the thrown exception, if the generation operation threw an
	 * exception.
	 * 
	 * @return the thrown exception or {@code null} if the operation returned a
	 *         result.
	 */
	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * Checks if this instance encapsulates an exception.
	 * 
	 * @return {@code true} if this instance holds an exception and
	 *         {@code false} if it holds a result.
	 */
	public boolean threwException() {
		return throwable != null;
	}

}
