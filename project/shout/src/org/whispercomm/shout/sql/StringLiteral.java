
package org.whispercomm.shout.sql;

public class StringLiteral {

	private final String value;

	public StringLiteral(String value) {
		this.value = value;
	}

	public String toString() {
		return new String("'" + value + "'");
	}
}
