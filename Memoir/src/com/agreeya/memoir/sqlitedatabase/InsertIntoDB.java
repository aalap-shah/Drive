package com.agreeya.memoir.sqlitedatabase;

import java.sql.Time;

import com.agreeya.memoir.receivers.AlarmReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class InsertIntoDB extends Service{

	private String LOCATION = "location";
	private String TYPE = "type";
	private String TIME = "time";
    private String mLocation;
    private String mType;
    private double mTime;
	private DataSource datasource;
	PathRepo pathObj = null;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
		Log.v("DB","onCreate");
		super.onCreate();
		datasource = new DataSource(this.getApplicationContext());
		datasource.open();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		mLocation = intent.getStringExtra(LOCATION);
		mType = intent.getStringExtra(TYPE);
		mTime = intent.getDoubleExtra(TIME, 000);
		Log.v("DB Storing"," location : " + mLocation);
		Log.v("DB Storing"," type : "+ mType);
		Log.v("DB Storing"," time : "+mTime);
		pathObj = datasource.createPath(mType,mLocation,mTime);
		scheduleAlarm();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void scheduleAlarm() {
		Time t = new Time(System.currentTimeMillis());
		Long l = t.getTime() + 5000;
		Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, l, PendingIntent
				.getBroadcast(this, 1, intentAlarm,
						PendingIntent.FLAG_UPDATE_CURRENT));
		Toast.makeText(this, "Alarm Scheduled for : " + l.toString(),
				Toast.LENGTH_LONG).show();
		Log.v("time", l.toString());

	}
}