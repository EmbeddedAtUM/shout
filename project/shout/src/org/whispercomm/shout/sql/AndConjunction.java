
package org.whispercomm.shout.sql;

public class AndConjunction implements SqlConjunction {

	@Override
	public String getConjunction() {
		return " AND ";
	}

	@Override
	public String toString() {
		return getConjunction();
	}

}
