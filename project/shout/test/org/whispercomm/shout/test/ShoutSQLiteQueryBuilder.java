
package org.whispercomm.shout.test;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowSQLiteQueryBuilder;

@Implements(SQLiteQueryBuilder.class)
public class ShoutSQLiteQueryBuilder {
	private String mTables = "";
	StringBuilder mWhereClause;

	@Implementation
	public static String buildQueryString(boolean distinct, String tables,
			String[] columns, String where, String groupBy, String having,
			String orderBy, String limit) {
		return ShadowSQLiteQueryBuilder.buildQueryString(distinct, tables, columns, where, groupBy,
				having, orderBy, limit);
	}

	@Implementation
	public void setTables(String inTables) {
		this.mTables = inTables;
	}

	@Implementation
	public void appendWhere(CharSequence inWhere) {
		if (mWhereClause == null) {
			mWhereClause = new StringBuilder(inWhere.length() + 16);
		}
		if (mWhereClause.length() > 0) {
			mWhereClause.append(" AND ");
		}
		mWhereClause.append(inWhere);
	}
	
	@Implementation
	public Cursor query(SQLiteDatabase db, String[] projectionIn, String selection,
			String[] selectionArgs, String groupBy, String having, String sortOrder) {
		if (mTables == null) {
			return null;
		}
		String whereClause = new String();
		if (selection != null) {
			if (this.mWhereClause != null) {
				whereClause = (mWhereClause.append(" AND " + selection)).toString();
			} else {
				whereClause = selection;
			}
		} else {
			if (this.mWhereClause != null) {
				whereClause = mWhereClause.toString();
			} else {
				whereClause = null;
			}
		}
		return db.query(false, mTables, projectionIn, whereClause, selectionArgs, groupBy, having,
				sortOrder, null);

	}
}
