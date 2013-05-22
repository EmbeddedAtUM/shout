
package org.whispercomm.shout.colorstorage;

import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.ECPublicKey;

public class ShoutBorder {

	private int borderColor = 0;
	private String username;
	private ECPublicKey publicKey;

	public ShoutBorder(User user) {
		username = user.getUsername();
		publicKey = user.getPublicKey();
	}

	public ShoutBorder(String newusername, ECPublicKey newPublicKey) {
		username = newusername;
		publicKey = newPublicKey;
	}

	public ShoutBorder(String newusername, int color, ECPublicKey newPublicKey) {
		borderColor = color;
		username = newusername;
		publicKey = newPublicKey;
	}

	public int getBorderColor() {
		return borderColor;
	}

	public String getUsername() {
		return username;
	}

	public ECPublicKey getPublicKey() {
		return publicKey;
	}

	public void setBorderColor(int color) {
		borderColor = color;
	}

}
