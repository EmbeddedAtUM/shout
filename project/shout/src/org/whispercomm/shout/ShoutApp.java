
package org.whispercomm.shout;

import org.whispercomm.shout.content.ContentManager;
import org.whispercomm.shout.content.ShoutImageStorage;

import android.app.Application;

public class ShoutApp extends Application {

	private ContentManager mContentManager;
	private ShoutImageStorage mShoutImageStorage;

	@Override
	public void onCreate() {
		super.onCreate();
		mContentManager = new ContentManager(this);
		mShoutImageStorage = new ShoutImageStorage(mContentManager);
	}

	@Override
	public Object getSystemService(String name) {
		if (ContentManager.SHOUT_CONTENT_SERVICE.equals(name)) {
			return mContentManager;
		} else {
			return super.getSystemService(name);
		}
	}
}
