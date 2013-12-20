package com.agreeya.memoir.sqlitedatabase;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.agreeya.memoir.DriveActivity.PathChangeListner;
import com.agreeya.memoir.model.AudioElement;
import com.agreeya.memoir.model.Element;
import com.agreeya.memoir.model.PhotoElement;
import com.agreeya.memoir.model.VideoElement;

/**
 *Helper class for database
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

	public Context context;
	public static final String TABLE_SAVED_PATH = "paths";
	public static final String COLUMN_PATH_ID = "path_id";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_PATH = "path";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_TRIP_NO = "trip_no";

	public static final String COLUMN_TRIP_ID = "trip_id";
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
	private static String[] allColumns = { MySQLiteHelper.COLUMN_PATH_ID,
			MySQLiteHelper.COLUMN_TYPE, MySQLiteHelper.COLUMN_PATH,
			MySQLiteHelper.COLUMN_TIME, MySQLiteHelper.COLUMN_TRIP_NO };

	// Database creation sql statement
	private static final String DATABASE_CREATE_PATH = "create table "
			+ TABLE_SAVED_PATH + "(" + COLUMN_PATH_ID
			+ " integer primary key autoincrement, " + COLUMN_TYPE + " text ,"
			+ COLUMN_PATH + " text ," + COLUMN_TIME + " real ,"
			+ COLUMN_TRIP_NO + " integer " + ", FOREIGN KEY(" + COLUMN_TRIP_NO
			+ ") REFERENCES " + TABLE_SAVED_TRIP + "(" + COLUMN_TRIP_ID + ")"
			+ ");";

	private static final String DATABASE_CREATE_TRIP = "create table "
			+ TABLE_SAVED_TRIP + "(" + COLUMN_TRIP_ID
			+ " integer primary key autoincrement, " + COLUMN_TRIP_NAME
			+ " text ," + COLUMN_TRIP_DESC + " text ," + COLUMN_TRIP_SOURCE
			+ " text ," + COLUMN_TRIP_DEST + " text ," + COLUMN_TOTAL_TIME
			+ " real );";

	private static final String GET_TRIP_ID = "select MAX( " + COLUMN_TRIP_ID
			+ " ) from " + TABLE_SAVED_TRIP;

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;

	}

	public void setListener(PathChangeListner listner) {
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

	@SuppressWarnings("unused")
	public int getTripId() {

		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery(GET_TRIP_ID, null);
		cursor.moveToFirst();
		// Log.v("asdf", " " + cursor.getInt(0));
		if (cursor != null)
			return cursor.getInt(0);
		else
			return 999;
	}

	/**
	 * This function is used to store the newly created media's information in the database
	 *  
	 * @param trip_no : trip number of the media
	 * @param type : photo/video/audio
	 * @param path : Storage location of the media
	 * @param time : instance of time at which media is captured
	 */
	public void createPath(int trip_no, String type, String path, double time) {

		SQLiteDatabase database = this.getWritableDatabase();
		Log.v("asdfg", " create path with type" + type);
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TRIP_NO, trip_no);
		values.put(MySQLiteHelper.COLUMN_TYPE, type);
		values.put(MySQLiteHelper.COLUMN_PATH, path);
		values.put(MySQLiteHelper.COLUMN_TIME, time);
		database.insert(MySQLiteHelper.TABLE_SAVED_PATH, null, values);

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

	/**
	 * For storing the information about the newly created trip
	 * 
	 * @param trip_name : Trip name
	 * @param trip_description : Description
	 * @param trip_source : Source of trip
	 * @param trip_destination : Destination of trip
	 * @param total_time : Trip Duration
	 */
	public void createTrip(String trip_name, String trip_description,
			String trip_source, String trip_destination, double total_time) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TRIP_NAME, trip_name);
		values.put(MySQLiteHelper.COLUMN_TRIP_DESC, trip_description);
		values.put(MySQLiteHelper.COLUMN_TRIP_SOURCE, trip_source);
		values.put(MySQLiteHelper.COLUMN_TRIP_DEST, trip_destination);
		values.put(MySQLiteHelper.COLUMN_TOTAL_TIME, total_time);
		database.insert(MySQLiteHelper.TABLE_SAVED_TRIP, null, values);
		// Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_TRIP,
		// allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
		// null, null, null);
		// cursor.moveToFirst();
		// Trip newTripRepo = cursorToTripRepo(cursor);
		// cursor.close();
		// return newTripRepo;
	}

	// public void deletePath(PathRepo path) {
	// long id = path.getId();
	// System.out.println("Comment deleted with id: " + id);
	// database.delete(MySQLiteHelper.TABLE_SAVED_PATH, MySQLiteHelper.COLUMN_ID
	// + " = " + id, null);
	// }

	
	/**
	 * 
	 * function for getting all the media paths of all the trips 
	 * 
	 * @return : List
	 */
	public List<Path> getAllPaths() {
		List<Path> pathList = new ArrayList<Path>();
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_PATH,
				allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Path path = cursorToPathRepo(cursor);
			pathList.add(path);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return pathList;
	}

	/**
	 * 
	 * function for getting all the media paths for a specific trip
	 * 
	 * @param trip_no : trip number 
	 * @return List
	 */
	public List<Path> getAllPaths(int trip_no) {
		List<Path> pathList = new ArrayList<Path>();
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SAVED_PATH,
				allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Path path = cursorToPathRepo(cursor);
			if (path.getTripNo() == trip_no)
				pathList.add(path);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return pathList;
	}

	private static ArrayList<Element> elemList = null;

	/**
	 * 
	 * function for getting all the media elements
	 * 
	 * @return ArrayList
	 */
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
		Log.v("asdfg", "returned all elements :" + elemList);
		return elemList;
	}

	
	/**
	 * 
	 * function for getting the specific media element 
	 * 
	 * @param type : type of element
	 * @param path : storage location of the media
	 * @return Element
	 */
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

	/**
	 * Function for retrieving the path information from the cursor
	 * 
	 * @param cursor
	 * @return Path object 
	 */
	private static Path cursorToPathRepo(Cursor cursor) {
		Path path = new Path();
		path.setId(cursor.getLong(0));
		path.setType(cursor.getString(1));
		path.setPath(cursor.getString(2));
		path.setTime(cursor.getDouble(3));
		path.setTripNo(cursor.getInt(4));
		return path;
	}

	// private Trip cursorToTripRepo(Cursor cursor) {
	// Trip path = new Trip();
	// path.setId(cursor.getLong(0));
	// path.setTripName(cursor.getString(1));
	// path.setTripDescription(cursor.getString(2));
	// path.setTripSource(cursor.getString(3));
	// path.setTripDestination(cursor.getString(4));
	// path.setTripTime(cursor.getDouble(5));
	// return path;
	// }

}