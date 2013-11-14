package com.agreeya.memoir.sqlitedatabase;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataSource {

	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_TYPE, MySQLiteHelper.COLUMN_PATH,
			MySQLiteHelper.COLUMN_TIME };

	public DataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public PathRepo createPath(String type, String path, double time) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TYPE, type);
		values.put(MySQLiteHelper.COLUMN_PATH, path);
		values.put(MySQLiteHelper.COLUMN_TIME, time);
		long insertId = database.insert(MySQLiteHelper.TABLE_SAVED_PATH, null,
				values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_PATH,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		PathRepo newPathRepo = cursorToPathRepo(cursor);
		cursor.close();
		return newPathRepo;
	}

	public TripRepo createTrip(String trip_name, String trip_description,
			String trip_source, String trip_destination, double total_time) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TRIP_NAME, trip_name);
		values.put(MySQLiteHelper.COLUMN_TRIP_DESC, trip_description);
		values.put(MySQLiteHelper.COLUMN_TRIP_SOURCE, trip_source);
		values.put(MySQLiteHelper.COLUMN_TRIP_DEST, trip_destination);
		values.put(MySQLiteHelper.COLUMN_TOTAL_TIME, total_time);
		long insertId = database.insert(MySQLiteHelper.TABLE_SAVED_TRIP, null,
				values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_TRIP,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		TripRepo newTripRepo = cursorToTripRepo(cursor);
		cursor.close();
		return newTripRepo;
	}

	// public void deletePath(PathRepo path) {
	// long id = path.getId();
	// System.out.println("Comment deleted with id: " + id);
	// database.delete(MySQLiteHelper.TABLE_SAVED_PATH, MySQLiteHelper.COLUMN_ID
	// + " = " + id, null);
	// }

	public List<PathRepo> getAllPaths() {
		List<PathRepo> pathList = new ArrayList<PathRepo>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_PATH,
				allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			PathRepo path = cursorToPathRepo(cursor);
			pathList.add(path);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return pathList;
	}

	public List<TripRepo> getAllTrips() {
		List<TripRepo> tripList = new ArrayList<TripRepo>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_TRIP,
				allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			TripRepo path = cursorToTripRepo(cursor);
			tripList.add(path);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return tripList;
	}
	
	private PathRepo cursorToPathRepo(Cursor cursor) {
		PathRepo path = new PathRepo();
		path.setId(cursor.getLong(0));
		path.setType(cursor.getString(1));
		path.setPath(cursor.getString(2));
		path.setTime(cursor.getDouble(3));
		return path;
	}

	private TripRepo cursorToTripRepo(Cursor cursor) {
		TripRepo path = new TripRepo();
		path.setId(cursor.getLong(0));
		path.setTripName(cursor.getString(1));
		path.setTripDescription(cursor.getString(2));
		path.setTripSource(cursor.getString(3));
		path.setTripDestination(cursor.getString(4));
		path.setTripTime(cursor.getDouble(5));
		return path;
	}
}