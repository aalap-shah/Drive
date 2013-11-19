package com.agreeya.memoir.sqlitedatabase;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.agreeya.memoir.activity.AudioElement;
import com.agreeya.memoir.activity.DriveActivity.PathChangeListner;
import com.agreeya.memoir.activity.Element;
import com.agreeya.memoir.activity.PhotoElement;
import com.agreeya.memoir.activity.VideoElement;

public class DataSource {

	private static SQLiteDatabase database;
	private static MySQLiteHelper dbHelper;
	private static PathChangeListner mListner;
	private static String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_TYPE, MySQLiteHelper.COLUMN_PATH,
			MySQLiteHelper.COLUMN_TIME };

	public static void init(Context context, PathChangeListner listner) {
		dbHelper = new MySQLiteHelper(context);
		mListner = listner;
	}

	public static void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public static void close() {
		dbHelper.close();
	}

	public static void createPath(String type, String path, double time) {

		Log.v("asdfg", " create path");
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TYPE, type);
		values.put(MySQLiteHelper.COLUMN_PATH, path);
		values.put(MySQLiteHelper.COLUMN_TIME, time);
		long insertId = database.insert(MySQLiteHelper.TABLE_SAVED_PATH, null,
				values);
		// Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_PATH,
		// allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
		// null, null, null);
		// cursor.moveToFirst();
		// PathRepo newPathRepo = cursorToPathRepo(cursor);
		// cursor.close();

		// if(elemList== null){
		// elemList = new ArrayList<Element>();
		// }

		getAllElements().add(getElementFromPathType(type, path));
		
//		if (elemList != null)
//			elemList.add(getElementFromPathType(type, path));

		if (mListner != null)
			mListner.onPathChanged();
		// return newPathRepo;
	}

	// public TripRepo createTrip(String trip_name, String trip_description,
	// String trip_source, String trip_destination, double total_time) {
	// ContentValues values = new ContentValues();
	// values.put(MySQLiteHelper.COLUMN_TRIP_NAME, trip_name);
	// values.put(MySQLiteHelper.COLUMN_TRIP_DESC, trip_description);
	// values.put(MySQLiteHelper.COLUMN_TRIP_SOURCE, trip_source);
	// values.put(MySQLiteHelper.COLUMN_TRIP_DEST, trip_destination);
	// values.put(MySQLiteHelper.COLUMN_TOTAL_TIME, total_time);
	// long insertId = database.insert(MySQLiteHelper.TABLE_SAVED_TRIP, null,
	// values);
	// Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_TRIP,
	// allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
	// null, null, null);
	// cursor.moveToFirst();
	// TripRepo newTripRepo = cursorToTripRepo(cursor);
	// cursor.close();
	// return newTripRepo;
	// }

	// public void deletePath(PathRepo path) {
	// long id = path.getId();
	// System.out.println("Comment deleted with id: " + id);
	// database.delete(MySQLiteHelper.TABLE_SAVED_PATH, MySQLiteHelper.COLUMN_ID
	// + " = " + id, null);
	// }

	// private static List<PathRepo> pathList = new ArrayList<PathRepo>();
	//
	// public static List<PathRepo> getAllPaths() {
	// if (pathList == null) {
	//
	// Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_PATH,
	// allColumns, null, null, null, null, null);
	// cursor.moveToFirst();
	// while (!cursor.isAfterLast()) {
	// PathRepo path = cursorToPathRepo(cursor);
	// pathList.add(path);
	// cursor.moveToNext();
	// }
	// // Make sure to close the cursor
	// cursor.close();
	// }
	// return pathList;
	// }

	private static ArrayList<Element> elemList = null;

	public static ArrayList<Element> getAllElements() {
		String type, path;
		Log.v("asdfg", "get all elements :" + elemList);
		if (elemList == null) {
			elemList = new ArrayList<Element>();
			Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_PATH,
					allColumns, null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				type = cursorToPathRepo(cursor).getType();
				path = cursorToPathRepo(cursor).getPath();
				if (getElementFromPathType(type, path) != null) {
					elemList.add(getElementFromPathType(type, path));
				}
				cursor.moveToNext();
			}
		}
		return elemList;
	}

	private static Element getElementFromPathType(String type, String path) {
		if (type.equalsIgnoreCase("audio")) {
			return (new AudioElement(path));
		} else if (type.equalsIgnoreCase("video")) {
			return (new VideoElement(path));
		} else if (type.equalsIgnoreCase("single")
				|| type.equalsIgnoreCase("mulitshot")) {
			return (new PhotoElement(path));
		} else
			return null;
	}

	// public List<TripRepo> getAllTrips() {
	// List<TripRepo> tripList = new ArrayList<TripRepo>();
	//
	// Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_TRIP,
	// allColumns, null, null, null, null, null);
	// cursor.moveToFirst();
	// while (!cursor.isAfterLast()) {
	// TripRepo path = cursorToTripRepo(cursor);
	// tripList.add(path);
	// cursor.moveToNext();
	// }
	// // Make sure to close the cursor
	// cursor.close();
	// return tripList;
	// }

	private static PathRepo cursorToPathRepo(Cursor cursor) {
		PathRepo path = new PathRepo();
		path.setId(cursor.getLong(0));
		path.setType(cursor.getString(1));
		path.setPath(cursor.getString(2));
		path.setTime(cursor.getDouble(3));
		return path;
	}

	// private TripRepo cursorToTripRepo(Cursor cursor) {
	// TripRepo path = new TripRepo();
	// path.setId(cursor.getLong(0));
	// path.setTripName(cursor.getString(1));
	// path.setTripDescription(cursor.getString(2));
	// path.setTripSource(cursor.getString(3));
	// path.setTripDestination(cursor.getString(4));
	// path.setTripTime(cursor.getDouble(5));
	// return path;
	// }
}