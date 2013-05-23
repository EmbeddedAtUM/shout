
package org.whispercomm.shout.provider;

import java.security.spec.InvalidKeySpecException;

import org.whispercomm.shout.User;
import org.whispercomm.shout.colorstorage.ShoutBorder;
import org.whispercomm.shout.crypto.ECPublicKey;
import org.whispercomm.shout.crypto.KeyGenerator;
import org.whispercomm.shout.provider.ColorProvider.ColorDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

public class ShoutColorContract {

	private static final String TAG = ShoutColorContract.class.getSimpleName();
	static final String AUTHORITY = "org.whispercomm.shout.color.provider";

	/*
	 * adds another user to the database if they are new does nothing if they
	 * are not
	 * @param user the potential new user
	 */

	public static void saveShoutBorder(Context context, User user) {
		String username = user.getUsername();
		ECPublicKey key = user.getPublicKey();
		ShoutBorder shout = new ShoutBorder(username, key);
		int[] colorsInUse = compareUsernames(context, shout.getUsername());
		boolean doesPubKeyExist = doesPublicKeyUsernamePairExist(context, shout.getUsername(),
				shout.getPublicKey());
		addShoutBorderwithColor(context, shout, colorsInUse, doesPubKeyExist);
	}

	public static void addShoutBorderwithColor(Context context,
			ShoutBorder shout,
			int[] colorsInUse,
			boolean doesPubKeyExist) {
		Log.v(TAG, "doesPubKeyExist val: " + String.valueOf(doesPubKeyExist));
		Log.v(TAG, "colorsInUse.length: " + String.valueOf(colorsInUse.length));
		if (colorsInUse.length == 0) {
			shout.setBorderColor(Color.BLUE);
			addShoutBorder(context, shout);
			Log.v(TAG, "shoutborder added to db");
		}
		else {
			if (doesPubKeyExist == true) {
				Log.v(TAG, "pubkey username pair exists");
				return;
			}
			else {
				if ((setColor(colorsInUse.length)) == -1) {
					shout.setBorderColor(Color.RED);
					addShoutBorder(context, shout);
				}
				else {
					shout.setBorderColor(setColor(colorsInUse.length));
					addShoutBorder(context, shout);
				}
			}
		}
	}

	/*
	 * adds a ShoutBorder object to the database
	 */
	public static Uri addShoutBorder(Context context, ShoutBorder shoutborder) {
		ContentValues values = new ContentValues();
		values.put(ColorDatabase.KEY_USERNAME, shoutborder.getUsername());
		byte[] keyRepresentation = KeyGenerator.encodePublic(shoutborder.getPublicKey());
		String keyString = Base64.encodeToString(keyRepresentation, Base64.DEFAULT);
		values.put(ColorDatabase.KEY_PUBLIC_KEY, keyString);
		values.put(ColorDatabase.KEY_COLOR, shoutborder.getBorderColor());
		values.put(ColorDatabase.WARNING_SEEN, "false");
		Uri mNewUri = context.getContentResolver().insert(ColorProvider.COLOR_URI, values);
		Log.v(TAG, "shoutborder supposedly added to db");
		return mNewUri;

	}

	public static ShoutBorder getShoutBorder(Context context, User user) {
		return getShoutBorder(context, user.getUsername(), user.getPublicKey());
	}

	/*
	 * gets a ShoutBorder object out of the database
	 * @param username searches database by username
	 * @param publickey search database by publickey
	 */
	public static ShoutBorder getShoutBorder(Context context, String username, ECPublicKey publicKey) {
		byte[] keyRepresentation = KeyGenerator.encodePublic(publicKey);
		String keyString = Base64.encodeToString(keyRepresentation, Base64.DEFAULT);
		String selection = ColorDatabase.KEY_USERNAME + " =? AND " + ColorDatabase.KEY_PUBLIC_KEY
				+ "=?";
		String[] selectionArgs = {
				username, keyString
		};
		Cursor cursor = context.getContentResolver().query(ColorProvider.COLOR_URI, null, selection
				, selectionArgs, null);
		if (cursor.moveToFirst()) {
			byte[] originalKeyRepresentation = Base64.decode(keyString, Base64.DEFAULT);
			ECPublicKey originalPublicKey = null;
			try {
				originalPublicKey = KeyGenerator.generatePublic(originalKeyRepresentation);
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
			int color = cursor.getInt(3);
			String flag = cursor.getString(4);
			boolean hasSeen = false;
			if (flag.equalsIgnoreCase("true")) {
				hasSeen = true;
			}
			ShoutBorder shoutborder = new ShoutBorder(cursor.getString(1),
					originalPublicKey, color, hasSeen);
			cursor.close();
			return shoutborder;
		} else {
			Log.v(TAG, "username: " + username);
			Log.v(TAG, "cursor returning null, stuff is gonna crash");
			cursor.close();
			return null;
		}

	}

	/*
	 * @param username is a new username by a shouter
	 * @return the colors associated with username
	 */
	public static int[] compareUsernames(Context context, String username) {
		Cursor cursor = context.getContentResolver().query(ColorProvider.COLOR_URI, null,
				ColorDatabase.KEY_USERNAME + " = ?",
				new String[] {
					username
				}, null);
		int[] colorsUsed = {
				0
		};
		try {
			colorsUsed = new int[cursor.getCount()];
			int arrayIndex = 0;
			while (cursor.moveToNext()) {
				int index = cursor.getColumnIndex(ColorDatabase.KEY_COLOR);
				colorsUsed[arrayIndex] = cursor.getInt(index);
				arrayIndex++;
			}
		} catch (NullPointerException e) {
			Log.v(TAG, "cursor is null");

		}
		cursor.close();
		return colorsUsed;
	}

	/*
	 * @param a shouter's public key
	 * @return true if the public key is already in the database, false if it
	 * doesn't
	 */

	public static boolean doesPublicKeyUsernamePairExist(Context context, String username,
			ECPublicKey publicKey) {
		byte[] keyRepresentation = KeyGenerator.encodePublic(publicKey);
		String keyString = Base64.encodeToString(keyRepresentation, Base64.DEFAULT);
		String selection = ColorDatabase.KEY_USERNAME + " = ? AND " + ColorDatabase.KEY_PUBLIC_KEY
				+ "=?";
		Cursor cursor = context.getContentResolver().query(ColorProvider.COLOR_URI, null,
				selection,
				new String[] {
						username, keyString
				}, null);
		boolean flag = false;
		try {
			while (cursor.moveToNext()) {
				if (keyString.equalsIgnoreCase(cursor.getString(2))) {
					flag = true;
					cursor.close();
					return flag;
				}
				else {
					flag = false;
				}
			}
		} catch (NullPointerException e) {
			Log.v(TAG, "cursor's null");
		}
		cursor.close();
		return flag;

	}

	public static int updateShoutBorder(Context context, ShoutBorder oldShoutBorder) {
		Log.v(TAG, "updateShoutBorder called");
		ContentValues values = new ContentValues();
		values.put(ColorDatabase.KEY_USERNAME, oldShoutBorder.getUsername());
		values.put(ColorDatabase.KEY_COLOR, oldShoutBorder.getBorderColor());
		String pubKeyString = Base64.encodeToString(
				KeyGenerator.encodePublic(oldShoutBorder.getPublicKey()), Base64.DEFAULT);
		values.put(ColorDatabase.KEY_PUBLIC_KEY, pubKeyString);
		String flag = "true";
		values.put(ColorDatabase.WARNING_SEEN, flag);
		String mSelectionClause = ColorDatabase.KEY_PUBLIC_KEY + " =? AND "
				+ ColorDatabase.KEY_USERNAME + "=?";
		String[] mSelectionArgs = {
				pubKeyString, oldShoutBorder.getUsername()
		};
		Log.v(TAG, "username update: " + oldShoutBorder.getUsername());
		return context.getContentResolver().update(ColorProvider.COLOR_URI, values,
				mSelectionClause, mSelectionArgs);
	}

	public static int setColor(int index) {
		int[] possibleColors = {
				Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN,

		};
		if ((index) > possibleColors.length) {
			return -1;
		}
		else {
			return possibleColors[index - 1];
		}
	}

}
