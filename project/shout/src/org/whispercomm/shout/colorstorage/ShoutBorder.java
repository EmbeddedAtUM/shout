
package org.whispercomm.shout.colorstorage;

import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.ECPublicKey;

public class ShoutBorder {

	private int borderColor = 0;
	private String username;
	private ECPublicKey publicKey;
	private boolean hasSeenWarning;

	public ShoutBorder(User user) {
		username = user.getUsername();
		publicKey = user.getPublicKey();
	}

	public ShoutBorder(String newusername, ECPublicKey newPublicKey) {
		username = newusername;
		publicKey = newPublicKey;
		hasSeenWarning = false;
	}

	public ShoutBorder(String newusername, ECPublicKey newPubKey, int color, boolean warn) {
		borderColor = color;
		username = newusername;
		publicKey = newPubKey;
		hasSeenWarning = warn;
	}

	public ShoutBorder(String newusername, ECPublicKey newPublicKey, int color) {
		borderColor = color;
		username = newusername;
		publicKey = newPublicKey;
		hasSeenWarning = false;
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

	public void setWarningStatus(boolean flag) {
		hasSeenWarning = flag;
	}

	public boolean getWarningStatus() {
		return hasSeenWarning;
	}

	public void setUsername(String newName) {
		username = newName;
	}

}
