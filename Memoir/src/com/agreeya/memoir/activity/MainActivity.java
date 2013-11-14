package com.agreeya.memoir.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.agreeya.memoir.R;
import com.agreeya.memoir.services.ControllerService;

public class MainActivity extends Activity {

	Intent mIntent;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("Memoir", "onCreate MainActivity");
		setContentView(R.layout.activity_main);
		mIntent = new Intent(this,ControllerService.class);
		startService(mIntent);		
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}
	
}
