package com.agreeya.memoir.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Compiler extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
