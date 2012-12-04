
package org.whispercomm.shout.sql;

public class Comparison implements SqlCondition {

	private SqlExpression leftHandSide;
	private SqlExpression rightHandSide;

	private SqlOperator sqlOperator;

	public Comparison(SqlExpression lhs, SqlOperator op, SqlExpression rhs) {
		this.leftHandSide = lhs;
		this.rightHandSide = rhs;
		this.sqlOperator = op;
	}

	public SqlExpression getLeftHandSide() {
		return leftHandSide;
	}

	public SqlExpression getRightHandSide() {
		return rightHandSide;
	}

	public SqlOperator getOperator() {
		return sqlOperator;
	}

	public void setOperator(SqlOperator operator) {
		this.sqlOperator = operator;
	}

	public String toString() {
		String expression = leftHandSide.toString() + sqlOperator.toString()
				+ rightHandSide.toString();
		return expression;
	}
}
