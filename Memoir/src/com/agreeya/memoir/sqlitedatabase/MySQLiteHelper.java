package com.agreeya.memoir.sqlitedatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_SAVED_PATH = "paths";
	public static final String COLUMN_ID = "trip_id";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_PATH = "path";
	public static final String COLUMN_TIME = "time";

	public static final String TABLE_SAVED_TRIP = "trips";
	public static final String COLUMN_TRIP_NAME = "name";
	public static final String COLUMN_TRIP_DESC = "description";
	public static final String COLUMN_TRIP_SOURCE = "source";
	public static final String COLUMN_TRIP_DEST = "destination";
	public static final String COLUMN_TOTAL_TIME = "totaltime";

	private static final String DATABASE_NAME = "paths.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE_PATH = "create table "
			+ TABLE_SAVED_PATH + "(" + COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_TYPE+ " text ," 
			+ COLUMN_PATH + " text ,"
			+ COLUMN_TIME + " real "
//			+ ", FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + TABLE_SAVED_TRIP
//			+ "(" + COLUMN_ID + ")"
					+ ");";

	private static final String DATABASE_CREATE_TRIP = "create table "
			+ TABLE_SAVED_TRIP + "(" + COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_TRIP_NAME + " text ,"
			+ COLUMN_TRIP_DESC + " text ,"
			+ COLUMN_TRIP_SOURCE + " text ,"
			+ COLUMN_TRIP_DEST + " text ,"
			+ COLUMN_TOTAL_TIME + " real );";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_TRIP);
		database.execSQL(DATABASE_CREATE_PATH);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_PATH);
		onCreate(db);
	}

}