
package org.whispercomm.shout.sql;

public class JoinClause implements SqlExpression {

	private String tableName;
	private SqlCondition condition;

	public JoinClause(String tableName, SqlCondition condition) {
		this.tableName = tableName;
		this.condition = condition;
	}

	public String toString() {
		return " JOIN " + tableName + " ON " + condition.toString() + " ";
	}

}
