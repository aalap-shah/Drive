package com.agreeya.memoir;

import android.app.Application;
import android.util.Log;

import com.agreeya.memoir.sqlitedatabase.MySQLiteHelper;


/**
 * Class working as Singleton 
 *
 */
public class MemoirApp extends Application {

	public MySQLiteHelper mDBHelper = null;
	public boolean continueSchedule;
	public boolean beginning;
	public int startHit;
	public ImageLoader il;
	public int compileCheck;
	public int poll;
	public int trip;
	@Override
	public void onCreate() {

		Log.v("app", "hello");
		mDBHelper = new MySQLiteHelper(this);
		beginning = true;
		startHit = 1;
		compileCheck =1 ;
		poll = 1;
		trip=0;
		il = ImageLoader.initialize(this);
	}

}
