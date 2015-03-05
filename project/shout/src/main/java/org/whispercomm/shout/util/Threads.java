
package org.whispercomm.shout.util;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

import java.util.concurrent.ThreadFactory;

import android.os.Process;

/**
 * Helper classes for creating background threads.
 * 
 * @author David R. Bild
 */
public class Threads {

	/**
	 * Factory for threads with priority set to
	 * {@link android.os.Process.THREAD_PRIORITY_BACKGROUND
	 * THREAD_PRIORITY_BACKGROUND}.
	 */
	public static class BackgroundThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			return new BackgroundThread(r);
		}
	}

	/**
	 * Thread with priority set to
	 * {@link android.os.Process.THREAD_PRIORITY_BACKGROUND
	 * THREAD_PRIORITY_BACKGROUND}.
	 */
	private static class BackgroundThread extends Thread {
		public BackgroundThread(Runnable r) {
			super(r);
		}

		@Override
		public void run() {
			Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
			super.run();
		}
	}

}
