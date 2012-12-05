
package org.whispercomm.shout.sql;

public class ColumnName implements SqlField {

	private final String fieldName;

	public ColumnName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public String toString() {
		return " " + fieldName + " ";
	}
}
