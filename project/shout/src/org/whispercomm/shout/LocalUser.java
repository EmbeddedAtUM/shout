
package org.whispercomm.shout;

public interface LocalUser extends User {

	/*
	 * The color for the border
	 */
	public int getColor();

	/*
	 * Gets the number of users with the same username
	 */
	public int getUserCount();
}
