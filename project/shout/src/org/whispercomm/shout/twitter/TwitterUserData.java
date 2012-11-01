
package org.whispercomm.shout.twitter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Provides access to some of the public data associated with a Twitter user.
 * Currently, only the profile image is exposed.
 * 
 * @author David R. Bild
 */
public class TwitterUserData {

	private static final String PROFILE_IMAGE_URL = "https://api.twitter.com/1/users/profile_image?screen_name=%s&size=bigger";

	private final String mUsername;
	private byte[] mProfileImage;

	/**
	 * Constructs a new {@code TwitterUserData} object encapsulating the public
	 * data for the specified user.
	 * 
	 * @param username the user whose data to retrieve
	 * @throws UnknownScreennameException if the specified screenname is not
	 *             invalid
	 * @throws IOException if an error communicating with the server occurs
	 */
	public TwitterUserData(String username) throws UnknownScreennameException, IOException {
		mUsername = username;
		fetchProfileImage();
	}

	/**
	 * Gets the profile image for this user
	 * 
	 * @return the profile image
	 */
	public byte[] getProfileImage() {
		return mProfileImage;
	}

	private void fetchProfileImage() throws UnknownScreennameException,
			IOException {
		try {
			// This URL-URI dance is needed to get proper character escaping in
			// the user-provided username field.
			URL url = new URL(String.format(PROFILE_IMAGE_URL, mUsername));
			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
			mProfileImage = httpGetAsByteArray(uri);
		} catch (UnexpectedStatusCodeException e) {
			if (e.is4XX())
				throw new UnknownScreennameException(mUsername);
			else
				throw e;
		} catch (URISyntaxException e) {
			// Should not happen, as the URI is constructed at design time,
			// except for parameter substitution in the query string
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the entity at the specified URL
	 * 
	 * @param uri the uri to GET
	 * @return the entity
	 * @throws UnexpectedStatusCodeException if a non-200 status code is
	 *             returned
	 * @throws IOException if a communication error occurred
	 */
	private static HttpEntity httpGetEntity(URI uri) throws UnexpectedStatusCodeException,
			IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(uri);

		HttpResponse response = client.execute(httpGet);
		StatusLine statusLine = response.getStatusLine();

		int statusCode = statusLine.getStatusCode();
		if (statusCode == 200) {
			return response.getEntity();
		} else {
			throw new UnexpectedStatusCodeException(statusCode);
		}
	}

	/**
	 * Gets the entity as the specified URL as a {@code byte[]}.
	 * 
	 * @param uri the uri to GET
	 * @return the entity as a {@code byte[]}
	 * @throws UnexpectedStatusCodeException if a non-200 status code is
	 *             returned
	 * @throws IOException if a communication error occurred
	 */
	private static byte[] httpGetAsByteArray(URI uri) throws UnexpectedStatusCodeException,
			IOException {
		HttpEntity entity = httpGetEntity(uri);
		return EntityUtils.toByteArray(entity);
	}

	public static class UnexpectedStatusCodeException extends IOException {

		private static final String DEFAULT_MESSAGE = "Unexpected status code %d";

		private static final long serialVersionUID = -3100440468188987294L;

		private int statusCode;

		public UnexpectedStatusCodeException(int statusCode) {
			super(String.format(DEFAULT_MESSAGE, statusCode));
			this.statusCode = statusCode;
		}

		public UnexpectedStatusCodeException(int statusCode, String detailMessage) {
			super(detailMessage);
			this.statusCode = statusCode;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public boolean is1XX() {
			return isType(100);
		}

		public boolean is2XX() {
			return isType(200);
		}

		public boolean is3XX() {
			return isType(300);
		}

		public boolean is4XX() {
			return isType(400);
		}

		public boolean is5XX() {
			return isType(500);
		}

		private boolean isType(int type) {
			return (type <= statusCode) && (statusCode < type + 100);
		}

	}

}
