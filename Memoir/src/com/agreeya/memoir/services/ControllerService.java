package com.agreeya.memoir.services;

import java.util.Random;

import com.agreeya.memoir.receivers.AlarmReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ControllerService extends Service {

	private int[] camera = { 0, 1 };
	private int[] shotmode = { 1, 5 };
	private Intent mIntent;
	private String CAMERA = "camera";
	private String PHOTO = "photo";
	private AlarmReceiver mAlarmReceiver;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.v("Memoir", "Controller Serivce Created");
		int funcMemoir = new Random().nextInt(4);
		int cameraType = new Random().nextInt(2);
		int photoType = new Random().nextInt(2);
		switch (funcMemoir) {

		case 0:
			mIntent = new Intent(this.getApplicationContext(),
					SilentPhotoShot.class);
			mIntent.putExtra(CAMERA, 1);
			mIntent.putExtra(PHOTO, shotmode[photoType]);
			Log.v("Memoir", "Starting photoshot");
			startService(mIntent);
			break;

		case 1:
			mIntent = new Intent(this.getApplicationContext(),
					VideoRecorder.class);
			mIntent.putExtra(CAMERA, 1);
			Log.v("Memoir", "Starting Video Recording ");
			startService(mIntent);
			break;

		case 2:
			mIntent = new Intent(this.getApplicationContext(),
					AudioRecorder.class);
			Log.v("Memoir", "Starting Audio Recording");
			startService(mIntent);
			break;

		case 3:
			Intent intentAlarm = new Intent(this, AlarmReceiver.class);
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(PendingIntent.getBroadcast(this, 1,
					intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
			Toast.makeText(this.getApplicationContext(),
					"Stopping Controller Service", Toast.LENGTH_LONG).show();
			break;
		default:
			Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG)
					.show();
		}
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
