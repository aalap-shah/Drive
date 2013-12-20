package com.agreeya.memoir.services;

import java.sql.Time;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.agreeya.memoir.MemoirApp;
import com.agreeya.memoir.receivers.AlarmReceiver;
import com.agreeya.memoir.sqlitedatabase.Path;

/**
 * Service to store the captured media information into the database
 *
 */
public class InsertIntoDBService extends Service {

	private String LOCATION = "location";
	private String TYPE = "type";
	private String TIME = "time";
	private String mLocation;
	private String mType;
	private double mTime;
	private int trip;
	Path pathObj = null;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		Log.v("DB", "onCreate");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent != null) {
			mLocation = intent.getStringExtra(LOCATION);
			mType = intent.getStringExtra(TYPE);
			mTime = intent.getDoubleExtra(TIME, 000);
			trip = intent.getIntExtra("trip", 0);
			Log.v("DB Storing", " location : " + mLocation);
			Log.v("DB Storing", " type : " + mType);
			Log.v("DB Storing", " time : " + mTime);
			((MemoirApp) this.getApplication()).mDBHelper.createPath(trip,
					mType, mLocation, mTime);
			if (((MemoirApp) this.getApplication()).continueSchedule) {

				scheduleAlarm();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * once the information is stored schedule the alarm
	 */
	public void scheduleAlarm() {
		Time t = new Time(System.currentTimeMillis());
		Long l = t.getTime() + 5000;
		Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, l, PendingIntent
				.getBroadcast(this, 1, intentAlarm,
						PendingIntent.FLAG_UPDATE_CURRENT));
		((MemoirApp) this.getApplication()).beginning = false;
		Toast.makeText(this, "Alarm Scheduled ", Toast.LENGTH_LONG).show();
		Log.v("time", l.toString());

	}
}
