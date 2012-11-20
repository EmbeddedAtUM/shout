
package org.whispercomm.shout.provider;

import org.whispercomm.shout.crypto.DsaSignature;
import org.whispercomm.shout.crypto.KeyGenerator;
import org.whispercomm.shout.test.util.TestFactory;

import android.content.ContentValues;
import android.util.Base64;

public class ProviderTestFactory {

	/*
	 * ContentValues for Users
	 */
	public static ContentValues USER_1_VALUES = null;
	public static ContentValues USER_2_VALUES = null;
	public static ContentValues USER_3_VALUES = null;

	public static final int USER_1_ID = 1;
	public static final int USER_2_ID = 2;
	public static final int USER_3_ID = 3;

	/*
	 * ContentValues for Shouts
	 */
	public static ContentValues ROOT_SHOUT_VALUES = null;
	public static ContentValues RESHOUT_SHOUT_VALUES = null;
	public static ContentValues COMMENT_SHOUT_VALUES = null;
	public static ContentValues RECOMMENT_SHOUT_VALUES = null;

	/**
	 * Call this function to initiate the content values for users. Since there
	 * aren't object literals, unfortunately we have to do this. This should
	 * probably be called first thing in your setUp() function.
	 */
	public static void initUserContentValues() {
		USER_1_VALUES = buildUser(TestFactory.TEST_USER_1.getUsername(),
				TestFactory.USER1_PUBKEY_BYTES);
		USER_2_VALUES = buildUser(TestFactory.TEST_USER_2.getUsername(),
				TestFactory.USER2_PUBKEY_BYTES);
		USER_3_VALUES = buildUser(TestFactory.TEST_USER_3.getUsername(),
				TestFactory.USER3_PUBKEY_BYTES);
	}

	/**
	 * Call this function to initiate the global static content Values for
	 * Shouts. This wouldn't be an issue if there were object literals.
	 */
	public static void initShoutContentValues() {
		ROOT_SHOUT_VALUES = buildShout(
				TestFactory.ROOT_SHOUT.getMessage(),
				TestFactory.ROOT_SHOUT.getTimestamp().getMillis(),
				TestFactory.ROOT_SHOUT.getLocation().getLatitude(),
				TestFactory.ROOT_SHOUT.getLocation().getLongitude(),
				KeyGenerator.encodePublic(TestFactory.ROOT_SHOUT.getSender().getPublicKey()),
				TestFactory.ROOT_SHOUT.getHash(),
				DsaSignature.encode(TestFactory.ROOT_SHOUT.getSignature()),
				TestFactory.ROOT_SHOUT.getVersion(),
				USER_1_ID);
		RESHOUT_SHOUT_VALUES = buildShout(
				TestFactory.RESHOUT_SHOUT.getMessage(),
				TestFactory.RESHOUT_SHOUT.getTimestamp().getMillis(),
				TestFactory.RESHOUT_SHOUT.getLocation().getLatitude(),
				TestFactory.RESHOUT_SHOUT.getLocation().getLongitude(),
				KeyGenerator.encodePublic(TestFactory.RESHOUT_SHOUT.getSender().getPublicKey()),
				TestFactory.RESHOUT_SHOUT.getHash(),
				DsaSignature.encode(TestFactory.RESHOUT_SHOUT.getSignature()),
				TestFactory.RESHOUT_SHOUT.getVersion(),
				USER_2_ID);
		COMMENT_SHOUT_VALUES = buildShout(
				TestFactory.COMMENT_SHOUT.getMessage(),
				TestFactory.COMMENT_SHOUT.getTimestamp().getMillis(),
				TestFactory.COMMENT_SHOUT.getLocation().getLatitude(),
				TestFactory.COMMENT_SHOUT.getLocation().getLongitude(),
				KeyGenerator.encodePublic(TestFactory.COMMENT_SHOUT.getSender().getPublicKey()),
				TestFactory.COMMENT_SHOUT.getHash(),
				DsaSignature.encode(TestFactory.COMMENT_SHOUT.getSignature()),
				TestFactory.COMMENT_SHOUT.getVersion(),
				USER_2_ID);
		RECOMMENT_SHOUT_VALUES = buildShout(
				TestFactory.RECOMMENT_SHOUT.getMessage(),
				TestFactory.RECOMMENT_SHOUT.getTimestamp().getMillis(),
				TestFactory.RECOMMENT_SHOUT.getLocation().getLatitude(),
				TestFactory.RECOMMENT_SHOUT.getLocation().getLongitude(),
				KeyGenerator.encodePublic(TestFactory.RECOMMENT_SHOUT.getSender().getPublicKey()),
				TestFactory.RECOMMENT_SHOUT.getHash(),
				DsaSignature.encode(TestFactory.RECOMMENT_SHOUT.getSignature()),
				TestFactory.RECOMMENT_SHOUT.getVersion(),
				USER_3_ID);
	}

	/**
	 * Convenience method for initiating all of the ContentValues
	 */
	public static void initAllContentValues() {
		initUserContentValues();
		initShoutContentValues();
	}

	/**
	 * Use in a tearDown() function, just to be safe.
	 */
	public static void clearAllContentValues() {
		USER_1_VALUES = null;
		USER_2_VALUES = null;
		USER_3_VALUES = null;
		ROOT_SHOUT_VALUES = null;
		RESHOUT_SHOUT_VALUES = null;
		COMMENT_SHOUT_VALUES = null;
		RECOMMENT_SHOUT_VALUES = null;
	}

	/*
	 * Helper functions
	 */
	private static ContentValues buildUser(String name, byte[] publicKey) {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Users.USERNAME, name);
		values.put(ShoutProviderContract.Users.PUB_KEY,
				Base64.encodeToString(publicKey, Base64.DEFAULT));
		return values;
	}

	private static ContentValues buildShout(String message, long time, double latitude,
			double longitude, byte[] authorKey, byte[] hash, byte[] signature, int version,
			int authorId) {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Shouts.MESSAGE, message);
		values.put(ShoutProviderContract.Shouts.TIME_SENT, time);
		values.put(ShoutProviderContract.Shouts.TIME_RECEIVED, System.currentTimeMillis());
		values.put(ShoutProviderContract.Shouts.LATITUDE, latitude);
		values.put(ShoutProviderContract.Shouts.LONGITUDE, longitude);
		values.put(ShoutProviderContract.Shouts.AUTHOR,
				Base64.encodeToString(authorKey, Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.HASH, Base64.encodeToString(hash, Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.SIGNATURE,
				Base64.encodeToString(signature, Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.VERSION, version);
		values.put(ShoutProviderContract.Shouts.USER_PK, authorId);
		return values;
	}
}
