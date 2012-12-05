
package org.whispercomm.shout.sql;

public class SqlPredicate implements SqlExpression {

	private SqlCondition leftHandSide;
	private SqlCondition rightHandSide;
	private SqlConjunction conjunction;

	public SqlPredicate(SqlCondition lhs, SqlConjunction conj, SqlCondition rhs) {
		this.leftHandSide = lhs;
		this.rightHandSide = rhs;
		this.conjunction = conj;
	}

	public SqlCondition getLeftHandSide() {
		return leftHandSide;
	}

	public SqlCondition getRightHandSide() {
		return rightHandSide;
	}

	public SqlConjunction getConjunction() {
		return conjunction;
	}

	@Override
	public String toString() {
		return " ((" + leftHandSide.toString() + ")" + conjunction.toString() + "("
				+ rightHandSide.toString() + ")) ";
	}

}
