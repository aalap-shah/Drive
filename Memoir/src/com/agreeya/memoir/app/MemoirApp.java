package com.agreeya.memoir.app;


import com.agreeya.memoir.sqlitedatabase.MySQLiteHelper;

import android.app.Application;

public class MemoirApp extends Application{
	
	public MySQLiteHelper mDBHelper = null; 
	
	@Override
	public void onCreate() {
		
		mDBHelper = new MySQLiteHelper(this);
		
	}

}
