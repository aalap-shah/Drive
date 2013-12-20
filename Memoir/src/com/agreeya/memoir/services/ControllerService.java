package com.agreeya.memoir.services;

import com.agreeya.memoir.MemoirApp;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Controller Service for handling the user command(start/stop and pause/resume
 * trip) and starting the following two services
 *  1) Dispatcher service for dispatching the audio/video/photo service randomly
 *  2) Database service for storing the captured media information into the DB
 * 
 */
public class ControllerService extends Service {

	private Intent mIntent;
	private Intent mStoreIntoDB;
	private static String COMMAND = "command";
	private static String WhatToDo = "what_to_do";
	private static String LOCATION = "location";
	private static String TYPE = "type";
	private static String TIME = "time";
	private static String RETURN_RESPONSE = "ReturnResponse";
	private static String ROUT = "rout";
	private static String TRIP = "trip";

	private PendingIntent mPendingLocal;
	private int mRout;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("asd", "OnCreate of Controller Service");
		Intent local = new Intent(getApplicationContext(),
				ControllerService.class);
		mPendingLocal = PendingIntent.getService(getApplicationContext(), 0,
				local, 0);
		mIntent = new Intent(this, DispatcherService.class);
		mStoreIntoDB = new Intent(this, InsertIntoDBService.class);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d("asd", "OnStart of Controller Service");
		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		Log.v("asd", "controller Service on start command");
		mRout = intent.getIntExtra(ROUT, 99);
		Log.v("Controller", "mRout " + mRout);
		if (mRout == 1) {
			mIntent.putExtra(WhatToDo, intent.getStringExtra(COMMAND));
			mIntent.putExtra(RETURN_RESPONSE, mPendingLocal);
			mIntent.putExtra(ROUT, mRout);
			mIntent.putExtra(TRIP, ((MemoirApp) this.getApplication()).trip);
			startService(mIntent);
		}

		boolean check = intent.getBooleanExtra("store", false);
		if (check) {
			mStoreIntoDB.putExtra(TYPE, intent.getStringExtra(TYPE));
			mStoreIntoDB.putExtra(LOCATION, intent.getStringExtra(LOCATION));
			mStoreIntoDB.putExtra(TIME, intent.getDoubleExtra(TIME, 0000));
			mStoreIntoDB.putExtra(TRIP, intent.getIntExtra(TRIP, 9900));
			Log.v("asd", "starting db service");
			startService(mStoreIntoDB);
		}

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d("asd", "OnDestroy of Controller Service");
		super.onDestroy();
	}

}
