package org.whispercomm.shout.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class ShoutProviderDatabaseHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "shout_base";
	private static final int DB_VERSION = 1;
	
	// The following is some ugly code
	private static final String SQL_CREATETABLE_USER = "CREATE TABLE User (" +
		"ID INT NOT NULL PRIMARY KEY ASC AUTOINCREMENT, " +
		"Public_Key VARCHAR(16) UNIQUE, " +
		"Username VARCHAR(16)" +
		" );";
	private static final String SQL_CREATETABLE_SHOUT = "CREATE TABLE Shout ( " +
		"_ID INT PRIMARY KEY NOT NULL ASC AUTOINCREMENT, " +
		"User_ID INT, " +
		"FOREIGN KEY(User_ID) REFERENCES `User(_ID)`, " +
		"Content VARCHAR(140), " +
		"Date VARCHAR(19) " +
		");";
	private static final String SQL_CREATETABLE_USER_SHOUTS = "CREATE TABLE User_Shouts ( " +
			"_ID INT NOT NULL PRIMARY KEY ASC AUTOINCREMENT, " +
			"Shout_ID INT NOT NULL UNIQUE, " +
			"FOREIGN KEY(Shout_ID) REFERENCES Shout(_ID), " +
			"Reshout_ID INT, " +
			"FOREIGN KEY(Reshout_ID) REFERENCES Shout(_ID) " +
			");";
	private static final String SQL_CREATETABLE_TAG = "CREATE TABLE Tag ( " +
		"_ID INT NOT NULL PRIMARY KEY ASC AUTOINCREMENT, " +
		"Name VARCHAR(139) " +
		");";
	private static final String SQL_CREATETABLE_TAG_ASSIGNMENT = "CREATE TABLE Tag_Assignment ( " +
		"_ID INT NOT NULL PRIMARY KEY ASC AUTOINCREMENT, " +
		"Tag_ID INT NOT NULL, " +
		"FOREIGN KEY(Tag_ID) REFERENCES Tag(_ID), " +
		"Shout_ID INT NOT NULL, " +
		"FOREIGN KEY(Shout_ID) REFERENCES Shout(_ID) " +
		");";
	// Hopefully done with all that.
	// TODO Decide if we should use some sort of query builder
	
	public ShoutProviderDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ShoutProviderDatabaseHelper.SQL_CREATETABLE_USER);
		db.execSQL(ShoutProviderDatabaseHelper.SQL_CREATETABLE_SHOUT);
		db.execSQL(ShoutProviderDatabaseHelper.SQL_CREATETABLE_USER_SHOUTS);
		db.execSQL(ShoutProviderDatabaseHelper.SQL_CREATETABLE_TAG);
		db.execSQL(ShoutProviderDatabaseHelper.SQL_CREATETABLE_TAG_ASSIGNMENT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
