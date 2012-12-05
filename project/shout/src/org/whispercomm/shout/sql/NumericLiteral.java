
package org.whispercomm.shout.sql;

public class NumericLiteral<N extends Number> implements SqlLiteral {

	private final N value;

	public NumericLiteral(N value) {
		this.value = value;
	}

	public N getValue() {
		return value;
	}

	public String toString() {
		return " " + value.toString() + " ";
	}

}
