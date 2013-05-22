
package org.whispercomm.shout.provider;

import java.security.spec.InvalidKeySpecException;
import java.util.Random;

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

	// when setting the color, run methods to get colorsInUse doesPubKeyExist
	// and border, while cp is the colorprovider we use passed in
	public static void saveShoutBorder(Context context, ShoutBorder shout) {
		int[] colorsInUse = compareUsernames(context, shout.getUsername());
		boolean doesPubKeyExist = doesPublicKeyExist(context, shout.getPublicKey());
		addShoutBorderwithColor(context, shout, colorsInUse, doesPubKeyExist);

	}

	public static void addShoutBorderwithColor(Context context,
			ShoutBorder shout,
			int[] colorsInUse,
			boolean doesPubKeyExist) {
		if (colorsInUse.length == 0) {
			Log.v(TAG, "setting color to white");
			shout.setBorderColor(Color.WHITE);
			addShoutBorder(context, shout);
		}
		else {
			if (doesPubKeyExist == true) {
				return;
			}
			else {
				shout.setBorderColor(randomColor(colorsInUse, 10, 0));
				addShoutBorder(context, shout);
			}
		}
	}

	public static void addShoutBorderwithColor(Context context, ShoutBorder shout) {
		boolean doesPubKeyExist = doesPublicKeyExist(context, shout.getPublicKey());
		int[] colorsInUse = compareUsernames(context, shout.getUsername());
		if (colorsInUse.length == 0) {
			shout.setBorderColor(Color.WHITE);
			addShoutBorder(context, shout);
			Log.v(TAG, "adding first instance of username to color database");
		}

		else {
			// if the user is already in the database then it doesn't need to be
			// added again
			if (doesPubKeyExist == true) {
				ShoutBorder border = getShoutBorder(context, shout.getUsername(),
						shout.getPublicKey());
				System.out.println(border.getUsername());
				return;
			}
			// if there are more than zero colors in use and the user isn't in
			// the database, the new user gets its own color and is added to the
			// database
			else {
				Log.v(TAG, "setting color to nonzero");
				shout.setBorderColor(randomColor(colorsInUse, 10, 0));
				addShoutBorder(context, shout);
			}
		}
		System.out.println("shoutborder color: " + shout.getBorderColor());

	}

	// adds a ShoutBorder object to the database
	public static Uri addShoutBorder(Context context, ShoutBorder shoutborder) {
		Log.i(TAG, "Add Shout Bourder to ColorProvider");
		ContentValues values = new ContentValues();
		values.put(ColorDatabase.KEY_USERNAME, shoutborder.getUsername());
		byte[] keyRepresentation = KeyGenerator.encodePublic(shoutborder.getPublicKey());
		String keyString = Base64.encodeToString(keyRepresentation, Base64.DEFAULT);
		values.put(ColorDatabase.KEY_PUBLIC_KEY, keyString);
		values.put(ColorDatabase.KEY_COLOR, shoutborder.getBorderColor());
		Uri mNewUri = context.getContentResolver().insert(ColorProvider.COLOR_URI, values);
		return mNewUri;

	}

	public static ShoutBorder getShoutBorder(Context context, User user) {
		return getShoutBorder(context, user.getUsername(), user.getPublicKey());
	}

	// gets a ShoutBorder object out of the database
	public static ShoutBorder getShoutBorder(Context context, String username, ECPublicKey publicKey) {
		byte[] keyRepresentation = KeyGenerator.encodePublic(publicKey);
		String keyString = Base64.encodeToString(keyRepresentation, Base64.DEFAULT);
		Log.v(TAG, "query to get number of username instances");
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
			ShoutBorder shoutborder = new ShoutBorder(cursor.getString(1), color, originalPublicKey);
			cursor.close();
			return shoutborder;
		} else {
			cursor.close();
			return null;
		}

	}

	// returns colors in use
	public static int[] compareUsernames(Context context, String username) {
		Cursor cursor = context.getContentResolver().query(ColorProvider.COLOR_URI, null,
				ColorDatabase.KEY_USERNAME + " = ?",
				new String[] {
					username
				}, null);
		Log.v(TAG, "query to get an array of colors used out of the database");
		// Color is an integer in the database, so colorsUsed should be an int[]
		int[] colorsUsed = {
				0
		};
		try {
			colorsUsed = new int[cursor.getCount()];
			int arrayIndex = 0;
			// Since you used cursor.moveToFirst(), you're skipping the first
			// row here. Get rid of move to first and only use while(moveToNext)
			while (cursor.moveToNext()) {
				// Instead of doing cursor.getString(3), get the index of the
				// column you want, and then getString(index). And this should
				// probably be get int

				// e.g. int index = cursor.getColumnIndex(KEY_COLOR);
				// colorsUsed[whatever] = cursor.getInt(index)

				// Also, why are you always overwriting the same index of the
				// array here?
				int index = cursor.getColumnIndex(ColorDatabase.KEY_COLOR);
				colorsUsed[arrayIndex] = cursor.getInt(index);
				arrayIndex++;
			}
		} catch (NullPointerException e) {
			Log.v(TAG, "cursor's null");

		}

		// cursor.close();
		return colorsUsed;
	}

	// checks to see if the public key exists and returns true if it does, false
	// if it doesn't

	public static boolean doesPublicKeyExist(Context context, ECPublicKey publicKey) {
		byte[] keyRepresentation = KeyGenerator.encodePublic(publicKey);
		String keyString = Base64.encodeToString(keyRepresentation, Base64.DEFAULT);
		Cursor cursor = context.getContentResolver().query(ColorProvider.COLOR_URI, null,
				ColorDatabase.KEY_PUBLIC_KEY + " = ?",
				new String[] {
					keyString
				}, null);
		Log.v(TAG, "query to see if the publicKey already exists in database");
		boolean flag = false;
		try {
			while (cursor.moveToNext()) {
				Log.v(TAG, "query returns more than 0");
				if (keyString.equalsIgnoreCase(cursor.getString(2))) {
					flag = true;
					Log.v(TAG, "public key exists");
					// cursor.close();
					return flag;
				}
				else {
					flag = false;
					Log.v(TAG, "pub key doesn't exist");
				}
			}
		} catch (NullPointerException e) {
			Log.v(TAG, "cursor's null");
		}
		return flag;

	}

	public int updateShoutBorder(Context context, ShoutBorder oldShoutBorder) {
		ContentValues values = new ContentValues();
		values.put(ColorDatabase.KEY_USERNAME, oldShoutBorder.getUsername());
		values.put(ColorDatabase.KEY_COLOR, oldShoutBorder.getBorderColor());
		String mSelectionClause = ColorDatabase.KEY_PUBLIC_KEY + " =?";
		String pubKeyString = Base64.encodeToString(
				KeyGenerator.encodePublic(oldShoutBorder.getPublicKey()), Base64.DEFAULT);
		String[] mSelectionArgs = {
				pubKeyString
		};
		return context.getContentResolver().update(ColorProvider.COLOR_URI, values,
				mSelectionClause, mSelectionArgs);
	}

	public static int randomColor(int[] colorsInUse, double threshold, int count) {
		Random rand = new Random();
		int r = rand.nextInt(255);
		int g = rand.nextInt(255);
		int b = rand.nextInt(255);
		int randColor = Color.rgb(r, g, b);
		double[] labColor = toCIEfromRGBint(randColor);
		int j = 0;
		while (j < colorsInUse.length) {
			double[] inUseCIE = toCIEfromRGBint(colorsInUse[j]);
			if (calcColorDiff(labColor, inUseCIE, threshold) == true) {
				j++;
				continue;
			}
			else if (calcColorDiff(labColor, inUseCIE, threshold) == false) {
				count++;
				j = 0;
				r = rand.nextInt(255);
				g = rand.nextInt(255);
				b = rand.nextInt(255);
				randColor = Color.rgb(r, g, b);
				labColor = toCIEfromRGBint(randColor);
			}
			if (count > 500) {
				threshold = threshold / 2;
				count = 0;
			}
		}

		int aRandomColor = toRGBintfromCIE(labColor);
		return aRandomColor;
	}

	// converts int into CIE color system
	public static double[] toCIEfromRGBint(int[] rgb) {
		int oneColor = Color.rgb(rgb[0], rgb[1], rgb[2]);
		return toCIEfromRGBint(oneColor);
	}

	public static double[] toCIEfromRGBint(int rgbColor) {
		int r = Color.red(rgbColor);
		int g = Color.green(rgbColor);
		int b = Color.blue(rgbColor);
		double var_R = (double) r / 255; // 1
		double var_G = (double) g / 255; // .67059
		double var_B = (double) b / 255; // .1255
		if (var_R > .04045) {
			var_R = Math.pow((var_R + .055) / 1.055, 2.4);
		}
		else {
			var_R = var_R / 12.92;
		}
		if (var_G > .04045) {
			var_G = Math.pow((var_G + .055) / 1.055, 2.4); // .40724
		}
		else {
			var_G = var_G / 12.92;
		}
		if (var_B > .04045) {
			var_B = Math.pow((var_B + .055) / 1.055, 2.4); // .014446
		}
		else {
			var_B = var_B / 12.92;
		}

		var_R = var_R * 100;
		var_G = var_G * 100;
		var_B = var_B * 100;

		double X = var_R * .4124 + var_G * .3576 + var_B * .1805;
		double Y = var_R * .2126 + var_G * .7152 + var_B * .0722;
		double Z = var_R * .0193 + var_G * .1192 + var_B * .9505;

		X = X / 95.047;
		Y = Y / 100;
		Z = Z / 108.883;

		if (X > .008856) {
			X = Math.pow(X, 1.0 / 3);
		}
		else {
			X = 7.787 * X + 16 / 116;
		}
		if (Y > .008856) {
			Y = Math.pow(Y, (1.0 / 3));

		}
		else {
			Y = 7.787 * Y + 16 / 116;
		}
		if (Z > .008856) {
			Z = Math.pow(Z, (1.0 / 3));

		}
		else {
			Z = 7.787 * Z + 16 / 116;
		}
		double CIE_L = (116 * Y - 16);
		double CIE_a = (500 * (X - Y));
		double CIE_b = (200 * (Y - Z));

		double[] labColor = {
				CIE_L, CIE_a, CIE_b
		};
		return labColor;
	}

	public static int toRGBintfromCIE(double[] labColor) {
		double var_Y = (labColor[0] + 16) / 116;
		double var_X = labColor[1] / 500 + var_Y;
		double var_Z = var_Y - labColor[2] / 200;
		if (var_Y * var_Y * var_Y > .008856) {
			var_Y = var_Y * var_Y * var_Y;
		}
		else {
			var_Y = (var_Y - 16 / 116) / 7.787;
		}
		if (var_X * var_X * var_X > .008856) {
			var_X = var_X * var_X * var_X;
		}
		else {
			var_X = (var_X - 16 / 116) / 7.787;
		}
		if (var_Z * var_Z * var_Z > .008856) {
			var_Z = var_Z * var_Z * var_Z;
		}
		else {
			var_Z = (var_Z - 16 / 116) / 7.787;
		}
		double X = 95.047 * var_X;
		double Y = 100 * var_Y;
		double Z = 108.883 * var_Z;

		var_X = X / 100;
		var_Y = Y / 100;
		var_Z = Z / 100;

		double var_R = var_X * 3.2406 + var_Y * -1.5372 + var_Z * -.4986;
		double var_G = var_X * -.9689 + var_Y * 1.8758 + var_Z * .0415;
		double var_B = var_X * .0557 + var_Y * -.2040 + var_Z * 1.057;

		if (var_R > .0031308) {
			var_R = 1.055 * (Math.pow(var_R, 1 / 2.4)) - .055;
		}
		else {
			var_R = 12.92 * var_R;
		}
		if (var_G > .0031308) {
			var_G = 1.055 * (Math.pow(var_G, 1 / 2.4)) - .055;
		}
		else {
			var_G = 12.92 * var_G;
		}
		if (var_B > .0031308) {
			var_B = 1.055 * (Math.pow(var_B, 1 / 2.4)) - .055;
		}
		else {
			var_B = 12.92 * var_B;
		}
		Double doubR = (Double) var_R * 255;
		Double doubG = (Double) var_G * 255;
		Double doubB = (Double) var_B * 255;
		int R = doubR.intValue();
		int G = doubG.intValue();
		int B = doubB.intValue();

		return Color.rgb(R, G, B);
	}

	// equation to calculate difference in color from wikipedia formula
	// checks to see if the difference between the testColor and cieColor is
	// under the threshold
	public static boolean calcColorDiff(double[] testColor, double[] cieColor, double threshold) {
		// simplifies some variables later
		double L1 = testColor[0];
		double L2 = cieColor[0];
		double a1 = testColor[1];
		double a2 = cieColor[1];
		double b1 = testColor[2];
		double b2 = cieColor[2];
		// calculates various variables for the equation
		double deltaL = L2 - L1;
		double Lhat = (L1 + L2) / 2.0;
		double C1star = Math.sqrt(a1 * a1 + b1 * b1);
		double C2star = Math.sqrt(a2 * a2 + b2 * b2);
		double Chat = (C1star + C2star) / 2;
		double a1prime = a1 + a1 / 2
				* (1 - Math.sqrt(Math.pow(Chat, 7) / (Math.pow(Chat, 7) + Math.pow(25, 7))));
		double a2prime = a2 + a2 / 2
				* (1 - Math.sqrt(Math.pow(Chat, 7) / (Math.pow(Chat, 7) + Math.pow(25, 7))));
		double C1prime = Math.sqrt(a1prime * a1prime + b1 * b1);
		double C2prime = Math.sqrt(a2prime * a2prime + b2 * b2);
		double Chatprime = (C1prime + C2prime) / 2;
		double deltaCprime = C2prime - C1prime;
		double h1prime = Math.atan2(b1, a1prime) * Math.PI / 180;
		double h2prime = Math.atan2(b2, a2prime) * Math.PI / 180;
		double deltah;
		if (Math.abs(h1prime - h2prime) < 180 || Math.abs(h1prime - h2prime) == 180) {
			deltah = h2prime - h1prime;
		}
		else if (Math.abs(h1prime - h2prime) > 180 && h2prime <= h1prime) {
			deltah = h2prime - h1prime + 360;
		}
		else {
			deltah = h2prime - h1prime - 360;
		}
		double deltaH = 2 * Math.sqrt(C1prime * C2prime) * Math.sin(deltah / 2);
		double Hprime;
		if (Math.abs(h1prime - h2prime) > 180) {
			Hprime = (h1prime + h2prime + 360) / 2;
		}
		else {
			Hprime = (h1prime + h2prime) / 2;
		}
		double T = 1 - .17 * Math.cos(Hprime - 30 * Math.PI / 180) + .24 * Math.cos(2 * Hprime)
				+ .32 * Math.cos(3 * Hprime + 6 * Math.PI / 180)
				- .20 * Math.cos(4 * Hprime - 63 * Math.PI / 180);
		double S_L = 1 + (.015 * (Lhat - 50))
				* (Lhat - 50 / (Math.sqrt(20 + Math.pow(Lhat - 50, 2))));
		double S_C = 1 + .045 * Chatprime;
		double S_H = 1 + .015 * Chatprime * T;
		double R_T = -2
				* Math.sqrt(Math.pow(Chatprime, 7) / (Math.pow(Chatprime, 7) + Math.pow(25, 7)))
				* Math.sin(60
						* Math.PI
						/ 180
						* Math.exp(-Math.pow((Hprime - 275 * Math.PI / 180) / (25 * Math.PI / 180),
								2)));
		double deltaE = Math.sqrt(Math.pow(deltaL / S_L, 2) + Math.pow(deltaCprime / (S_C), 2)
				+ Math.pow(deltaH / S_H, 2) + R_T * (deltaCprime * deltaH) / (S_C * S_H));
		if (deltaE > threshold)
			return true;
		else
			return false;
	}

}
