
package org.whispercomm.shout.provider;

import java.util.List;

import org.whispercomm.shout.Shout;

import android.net.Uri;

public class ShoutSearchContract {

	public static class Message {
		public static final String TABLE_NAME = "messages";
		
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				ShoutProviderContract.CONTENT_URI_BASE, TABLE_NAME);
		
		public static final String SHOUT = "Shout";
		public static final String MESSAGE = "Content";
	}
	
	/**
	 * Searches for Shouts with the given string in the message body.
	 * 
	 * @param searchString
	 * @return List of Shouts matching the query, empty list if no Shouts matched
	 */
	public static List<Shout> searchShoutMessage(String searchString) {
		return null;
	}
}
