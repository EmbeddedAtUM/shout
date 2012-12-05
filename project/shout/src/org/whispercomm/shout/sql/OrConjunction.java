
package org.whispercomm.shout.sql;

public class OrConjunction implements SqlConjunction {

	@Override
	public String getConjunction() {
		return " OR ";
	}

	@Override
	public String toString() {
		return getConjunction();
	}

}
