package com.agreeya.memoir.receivers;

import com.agreeya.memoir.services.ControllerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	Intent mStartController;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.v("Memoir", "onReceive");
//		Toast.makeText(context, "Receiver", Toast.LENGTH_LONG).show();
		mStartController = new Intent(context, ControllerService.class);
		context.startService(mStartController);
	}
}
