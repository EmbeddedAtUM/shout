
package org.whispercomm.shout.provider;

import java.util.List;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.Tag;
import org.whispercomm.shout.User;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * A provider of shouts and shout users seen by this device, to be used for user
 * interface generation and authentication of senders.
 * 
 * @author David Adrian
 */
public class ShoutProviderContract {

	/**
	 * The authority for the Shout content provider
	 */
	public static final String AUTHORITY = "org.whispercomm.shout.provider";

	/**
	 * The content:// style URI for the Shout provider
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public static class Shouts implements BaseColumns {
		public static final String TABLE_NAME = "shout";
		public static final String AUTHOR = "Author";
		public static final String MESSAGE = "Message";
		public static final String PARENT = "Parent";
		public static final String SIGNATURE = "Signature";		
	}

	public static class Users implements BaseColumns {
		public static final String TABLE_NAME = "user";
		public static final String USERNAME = "Name";
		public static final String PUB_KEY = "Key";
	}

	public static Shout retrieveShoutById(long id) {
		return null;
	};

	public static long storeShout(Shout shout) {
		return -1;
	}

	public static User retrieveUserById(long id) {
		return null;
	}

	public static long storeUser(User user) {
		return -1;
	}

	public static Tag retrieveTagById(long id) {
		return null;
	}

	public static List<Tag> retrieveTagsByShoutId(long id) {
		return null;
	}

	public static long storeTag(Tag tag) {
		return -1;
	}
}
