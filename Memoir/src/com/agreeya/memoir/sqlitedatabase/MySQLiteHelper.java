package com.agreeya.memoir.sqlitedatabase;

import java.util.ArrayList;

import com.agreeya.memoir.activity.AudioElement;
import com.agreeya.memoir.activity.Element;
import com.agreeya.memoir.activity.PhotoElement;
import com.agreeya.memoir.activity.VideoElement;
import com.agreeya.memoir.activity.DriveActivity.PathChangeListner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

	private static PathChangeListner mListner;
	private static String SINGLE = "single";
	private static String MULTISHOT = "multishot";
	private static String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_TYPE, MySQLiteHelper.COLUMN_PATH,
			MySQLiteHelper.COLUMN_TIME };

	// Database creation sql statement
	private static final String DATABASE_CREATE_PATH = "create table "
			+ TABLE_SAVED_PATH + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TYPE + " text ,"
			+ COLUMN_PATH + " text ," + COLUMN_TIME + " real "
			// + ", FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " +
			// TABLE_SAVED_TRIP
			// + "(" + COLUMN_ID + ")"
			+ ");";

	private static final String DATABASE_CREATE_TRIP = "create table "
			+ TABLE_SAVED_TRIP + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TRIP_NAME
			+ " text ," + COLUMN_TRIP_DESC + " text ," + COLUMN_TRIP_SOURCE
			+ " text ," + COLUMN_TRIP_DEST + " text ," + COLUMN_TOTAL_TIME
			+ " real );";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}

	public void setListener( PathChangeListner listner){
		mListner = listner;		
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

	public void createPath(String type, String path, double time) {

		SQLiteDatabase database = this.getWritableDatabase();
		Log.v("asdfg", " create path with type" + type);
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TYPE, type);
		values.put(MySQLiteHelper.COLUMN_PATH, path);
		values.put(MySQLiteHelper.COLUMN_TIME, time);
		database.insert(MySQLiteHelper.TABLE_SAVED_PATH, null, values);
		// Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_PATH,
		// allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
		// null, null, null);
		// cursor.moveToFirst();
		// PathRepo newPathRepo = cursorToPathRepo(cursor);
		// cursor.close();

		// if(elemList== null){
		// elemList = new ArrayList<Element>();
		// }
		Log.d("asd", "Requesting an element for type " + type + " and path "
				+ path);
		Element e = getElementFromPathType(type, path);
		Log.d("asd", "Adding element " + e);
		getAllElements().add(e);
		Log.v("asdfg", "get all elements :" + elemList);

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

	public ArrayList<Element> getAllElements() {
		SQLiteDatabase database = this.getReadableDatabase();
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
		} else if (type.equalsIgnoreCase(SINGLE)
				|| type.equalsIgnoreCase(MULTISHOT)) {
			return (new PhotoElement(path));
		} else {
			Log.d("asd", "Why am i returning a null object");
			return null;
		}
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