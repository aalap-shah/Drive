package com.agreeya.memoir;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * Class for playing the Final compiled Video
 *
 */
public class VideoPlayer extends Activity {

	VideoView video_view;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		Log.v("asd", "1");
		video_view = (VideoView) findViewById(R.id.videoView1);
		String path = getIntent().getStringExtra("file");
		if(path!=null){
		Uri video = Uri.parse(path);
	    video_view.setVideoURI(video);
	    video_view.setMediaController(new MediaController(this));
	    video_view.requestFocus();
		}
		else{
			Toast.makeText(getApplicationContext(), "Error Opening Video", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
