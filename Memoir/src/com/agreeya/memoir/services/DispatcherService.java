package com.agreeya.memoir.services;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.agreeya.memoir.MemoirApp;
import com.agreeya.memoir.model.QueueElement;
import com.agreeya.memoir.receivers.AlarmReceiver;

/**
 * Dispatcher Service listen to the controller service for the command
 * (start/stop) and based on the command it randomly starts the audio,video and
 * photo service and stop the running trips
 * 
 */
public class DispatcherService extends Service {

	private static PendingIntent resultIntent;
	private static String CAMERA = "camera";
	private static String PHOTO = "photo";
	private static String WhatToDo = "what_to_do";
	private static String LOCATION = "location";
	private static String TYPE = "type";
	private static String TIME = "time";
	private static String RETURN_RESPONSE = "ReturnResponse";
	private static String RESPONSE = "response";
	private static String ROUT = "rout";
	private static String TRIP = "trip";
	private static String STORE = "store";

	private int[] mCamera = { 0, 1 };
	private int[] mShotmode = { 1, 5 };
	private Intent mIntent;
	private Intent mResult;
	private String mCommand;
	private QueueElement mQElem;
	private ArrayBlockingQueue<QueueElement> mDispatcherQueue;
	private int mRout, mTrip;
	private PendingIntent mPi;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("Dispatcher", "On Create of Dispatcher");
		mResult = new Intent(getApplicationContext(), DispatcherService.class);
		resultIntent = PendingIntent.getService(getApplicationContext(), 0,
				mResult, 0);
		mDispatcherQueue = new ArrayBlockingQueue<QueueElement>(50);
	}

	@Override
	public void onDestroy() {
		Log.d("Dispatcher", "OnDestroy of Dispatcher");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Boolean flag = false;
		Log.d("Dispatcher", "OnSTartCommand of Dispatcher");
		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		mRout = intent.getIntExtra(ROUT, 99);
		mCommand = "continue";
		Log.v("Dispatcher", "rout " + mRout);
		if (mRout == 1) {
			mCommand = intent.getStringExtra(WhatToDo);
			mPi = (PendingIntent) intent.getParcelableExtra(RETURN_RESPONSE);
			mTrip = intent.getIntExtra(TRIP, 499);
			Iterator<QueueElement> it = mDispatcherQueue.iterator();
			while (it.hasNext()) {
				QueueElement qlem = it.next();
				if (!qlem.command.equalsIgnoreCase(mCommand)) {
					mDispatcherQueue.remove(qlem);
					flag = true;
				}
			}
			if (flag == false) {
				mDispatcherQueue.add(new QueueElement(mTrip, mCommand, mPi));
				Log.d("Dispatcher", "Queue size after adding "
						+ mDispatcherQueue.size());
			}
		}
		Intent i = new Intent();
		i.putExtra(TYPE, intent.getStringExtra(TYPE));
		i.putExtra(LOCATION, intent.getStringExtra(LOCATION));
		i.putExtra(TIME, intent.getDoubleExtra(TIME, 00000));
		i.putExtra(STORE, intent.getBooleanExtra(STORE, false));
		i.putExtra(TRIP, intent.getIntExtra(TRIP, 399));
		boolean check = intent.getBooleanExtra(STORE, false);
		if (check) {
			Log.v("Dispatcher", "value of check " + check);
			try {
				mPi.send(getApplicationContext(), 2, i);
			} catch (CanceledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// if (check || (mDispatcherQueue.size() == 1)) {
		if (check == true || ((MemoirApp)getApplication()).poll ==1 ) {
			Log.v("Dispatcher", "value of check polling " + check);
			((MemoirApp)getApplication()).poll = 2; 
			mQElem = mDispatcherQueue.poll();
			if (mQElem!= null) {
				if (mQElem.command.equalsIgnoreCase("start")) {
					Log.v("Dispatcher", "start command");
					int funcMemoir = new Random().nextInt(3);
					int cameraType = new Random().nextInt(2);
					int photoType = new Random().nextInt(2);
					switch (funcMemoir) {

					case 0:
						mIntent = new Intent(this.getApplicationContext(),
								VideoRecorderService.class);
						mIntent.putExtra(RESPONSE, resultIntent);
						mIntent.putExtra(TRIP, mTrip);
						mIntent.putExtra(CAMERA, mCamera[cameraType]);
						Log.v("Dispatcher", "Starting Video Recording ");
						startService(mIntent);
						break;

					case 1:
						mIntent = new Intent(this.getApplicationContext(),
								AudioRecorderService.class);
						mIntent.putExtra(RESPONSE, resultIntent);
						mIntent.putExtra(TRIP, mTrip);
						Log.v("Dispatcher", "Starting Audio Recording");
						startService(mIntent);
						break;

					case 2:
						mIntent = new Intent(this.getApplicationContext(),
								PhotoShotService.class);
						mIntent.putExtra(CAMERA, mCamera[cameraType]);
						mIntent.putExtra(PHOTO, mShotmode[photoType]);
						mIntent.putExtra(RESPONSE, resultIntent);
						mIntent.putExtra(TRIP, mTrip);
						Log.v("Dispatcher", "Starting photoshot");
						startService(mIntent);
						break;

					default:
						Toast.makeText(getApplicationContext(), "Error",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Log.v("Dispatcher","stop command");
					((MemoirApp)getApplication()).poll = 1;
					Intent intentAlarm = new Intent(this, AlarmReceiver.class);
					AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					alarmManager.cancel(PendingIntent.getBroadcast(this, 1,
							intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
				}
			}
			else{
				((MemoirApp)getApplication()).poll = 1;
			}
		}
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
