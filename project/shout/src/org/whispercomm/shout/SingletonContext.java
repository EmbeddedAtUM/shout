package org.whispercomm.shout;

import android.content.Context;

public class SingletonContext {

	private static Context context = null;

	public static void setContext(Context context) {
		if (SingletonContext.context != null) {
			throw new IllegalStateException();
		} else {
			SingletonContext.context = context;
		}
	}

	public static Context getContext() {
		if (context == null) {
			throw new IllegalStateException();
		} else {
			return context;
		}
	}
	
	private SingletonContext() {
		throw new IllegalStateException();
	}
}
