
package org.whispercomm.shout.util;

import java.util.regex.Pattern;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.errors.InvalidEncodingException;

import android.net.Uri;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * Helper methods for working with Shout URIs, sha-256 hashes represented as hex
 * strings with scheme <code>shout://</code>.
 */
public class ShoutUriUtils {

	public static final String SCHEME = "shout";

	public static final Hash parseUri(Uri uri) throws IllegalArgumentException {

		// Validate scheme
		String scheme = uri.getScheme().toLowerCase();
		if (!scheme.equals(SCHEME)) {
			throw new IllegalArgumentException(String.format(
					"Invalid scheme.  Expected %s. Got %s.", SCHEME, scheme));
		}

		// Validate hash
		String hash = uri.getSchemeSpecificPart().substring(2);
		try {
			return new Hash(hash);
		} catch (InvalidEncodingException e) {
			throw new IllegalArgumentException(String.format("Invalid shout URI received: ", hash),
					e);
		}

	}

	public static final Pattern SHOUT_URI_PATTERN = Pattern.compile("shout://\\p{XDigit}{64}",
			Pattern.CASE_INSENSITIVE);

	public static void addLinks(TextView text) {
		Linkify.addLinks(text, SHOUT_URI_PATTERN, null, null, null);
	}

}
