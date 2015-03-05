
package org.whispercomm.shout.sql;

public class HexLiteral implements SqlLiteral {

	private static final char[] HEX_VALUES = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	private final String hex;

	/**
	 * Create a new HexLiteral from a hex string, and validate that the string
	 * is in fact hex. This is a convenience method for
	 * {@code HexLiteral(hexString, true)}
	 * 
	 * @param hexString The hex literal
	 * @throws InvalidSqlExpression If the hex string is invalid
	 */
	public HexLiteral(String hexString) throws InvalidSqlExpression {
		this(hexString, true);
	}

	/**
	 * Create a new HexLiteral from a hex string, with optional hex validation.
	 * 
	 * @param hexString The hex literal
	 * @param validate {@code true} to have the string validated
	 * @throws InvalidSqlExpression If the hex string is invalid
	 */
	public HexLiteral(String hexString, boolean validate) throws InvalidSqlExpression {
		if (validate) {
			boolean valid = isStringHex(hexString);
			if (!valid) {
				throw new InvalidSqlExpression();
			}
		}
		this.hex = hexString;
	}

	public String toString() {
		return new String(" x'" + hex + "' ");
	}

	public static boolean isStringHex(String str) {
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			for (int j = 0; j < HEX_VALUES.length; j++) {
				if (HEX_VALUES[j] == c) {
					return true;
				}
			}
		}
		return false;
	}
}
