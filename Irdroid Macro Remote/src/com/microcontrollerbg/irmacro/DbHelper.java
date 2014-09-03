package com.microcontrollerbg.irmacro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * sqlite database helper to create table into SQLite database
 * 
 * @author ketan(Visit my <a
 *         href="http://androidsolution4u.blogspot.in/">blog</a>)
 */
public class DbHelper extends SQLiteOpenHelper {
	static String DATABASE_NAME = "userdata";
	public static final String TABLE_NAME = "users";
	// table for the macro names mname and idm
	public static final String MACROS = "macro";
	public static final String KEY_MACRONAME = "mname";
	public static final String KEY_ID_MACRO = "idm";

	// fields for macro commands
	public static final String KEY_FNAME = "fname";
	public static final String KEY_LNAME = "lname";
	public static final String KEY_ID = "idm";
	public static final String KEY_ID1 = "id";

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + KEY_ID1
				+ " INTEGER PRIMARY KEY," + KEY_ID + " INTEGER, " + KEY_FNAME
				+ " TEXT, " + KEY_LNAME + " TEXT)";
		db.execSQL(CREATE_TABLE);

		String CREATE_TABLEMACROS = "CREATE TABLE " + MACROS + " ("
				+ KEY_ID_MACRO + " INTEGER PRIMARY KEY, " + KEY_MACRONAME
				+ " TEXT)";
		db.execSQL(CREATE_TABLEMACROS);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);

	}

}
