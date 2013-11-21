package com.agreeya.memoir.services;

import java.util.Random;

import android.app.Service;
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

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.v("Memoir", "Controller Serivce Created");
		int funcMemoir = new Random().nextInt(3);
		int cameraType = new Random().nextInt(2);
		int photoType = new Random().nextInt(2);
		switch (funcMemoir) {

		case 0:
			mIntent = new Intent(this.getApplicationContext(),
					VideoRecorder.class);
			mIntent.putExtra(CAMERA, camera[cameraType]);
			Log.v("Memoir", "Starting Video Recording ");
			startService(mIntent);
			break;

		case 1:
			mIntent = new Intent(this.getApplicationContext(),
					AudioRecorder.class);
			Log.v("Memoir", "Starting Audio Recording");
			startService(mIntent);
			break;

		case 2:
			mIntent = new Intent(this.getApplicationContext(),
					SilentPhotoShot.class);
			mIntent.putExtra(CAMERA, camera[cameraType]);
			mIntent.putExtra(PHOTO, shotmode[photoType]);
			Log.v("Memoir", "Starting photoshot");
			startService(mIntent);
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

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
