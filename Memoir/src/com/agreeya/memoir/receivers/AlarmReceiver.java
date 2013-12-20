package com.agreeya.memoir.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.agreeya.memoir.services.ControllerService;

/**
 * Alarm receiver for starting the controller service
 *
 */
public class AlarmReceiver extends BroadcastReceiver {

	Intent mStartController;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.v("Alarm Receiver", "onReceive");
//		Toast.makeText(context, "Receiver", Toast.LENGTH_LONG).show();
		mStartController = new Intent(context, ControllerService.class);
		mStartController.putExtra("command", "start");
		mStartController.putExtra("rout", 1);
		context.startService(mStartController);
	}
}
