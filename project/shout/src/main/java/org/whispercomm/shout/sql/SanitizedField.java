
package org.whispercomm.shout.sql;

public class SanitizedField implements SqlField {

	private final String field;

	public SanitizedField(String unsanitizedField) {
		field = unsanitizedField.replaceAll("'", "''");
	}

	@Override
	public String toString() {
		return " " + field + " ";
	}
}
