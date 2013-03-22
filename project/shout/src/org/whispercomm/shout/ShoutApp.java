
package org.whispercomm.shout;

import org.whispercomm.shout.content.AvatarStorage;
import org.whispercomm.shout.content.ContentManager;

import android.app.Application;

public class ShoutApp extends Application {

	private ContentManager mContentManager;
	private AvatarStorage mAvatarStorage;

	@Override
	public void onCreate() {
		super.onCreate();
		mContentManager = new ContentManager(this);
		mAvatarStorage = new AvatarStorage(mContentManager);
	}

	@Override
	public Object getSystemService(String name) {
		if (AvatarStorage.SHOUT_AVATAR_SERVICE.equals(name)) {
			return mAvatarStorage;
		} else if (ContentManager.SHOUT_CONTENT_SERVICE.equals(name)) {
			return mContentManager;
		} else {
			return super.getSystemService(name);
		}
	}
}
