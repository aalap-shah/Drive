package com.agreeya.memoir.services;

import java.io.File;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.widget.Toast;

public class VideoRecorder extends Service {

	private String LOCATION = "location";
	private String TYPE = "type";
	private String TIME = "time";
	private Camera mCamera = null;
	private CameraPreview mPreview = null;
	private WindowManager mWindowManager = null;
	static int mVideoNumber = 1;
	private int camera_type;
	private String mFilePath;
	private Intent mIntent;
	private MediaRecorder mVideoRecorder = null;
	double dTime;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Toast.makeText(this.getApplicationContext(), "Video Recorder",
				Toast.LENGTH_LONG).show();
		mIntent = new Intent(this.getApplicationContext(), InsertIntoDB.class);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		camera_type = intent.getIntExtra("camera", 99);
		Log.v("asd", "camera value : " + camera_type);
		mWindowManager = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		LayoutParams params = new WindowManager.LayoutParams(1, 1,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);

		mCamera = getCameraInstance();
		mPreview = new CameraPreview(this.getApplicationContext(), mCamera);
		mPreview.setZOrderOnTop(true);
		mPreview.mHolder.setFormat(PixelFormat.TRANSPARENT);
		try {
			mWindowManager.addView(mPreview, params);
		} catch (BadTokenException e) {
			Log.e("asd", "Catching the BAdTOKEn Exception");
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		try {
			stopRecording();
		} catch (Exception e) {
			Log.e("asd", e.toString());
			e.printStackTrace();
		}

		super.onDestroy();
	}

	@SuppressLint("NewApi")
	public Camera getCameraInstance() {
		Camera c = null;
		Log.v("asd", "opening camera");
		Camera.CameraInfo info = new Camera.CameraInfo();
		try {
			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
				Camera.getCameraInfo(i, info);

				if (info.facing == camera_type) {
					if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
						Log.v("asd", "Front camera opened " + info.facing);
						c = Camera.open(i);
					} else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
						Log.v("asd", "Back camera opened " + info.facing);
						c = Camera.open(i);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	private void stopRecording() throws Exception {
		if (mVideoRecorder != null) {
			mVideoRecorder.stop();
			mVideoRecorder.release();
			mVideoRecorder = null;
		}
		if (mCamera != null) {
			mCamera.reconnect();
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	public class CameraPreview extends SurfaceView implements
			SurfaceHolder.Callback {
		private SurfaceHolder mHolder;

		// private Camera mCamera;

		public CameraPreview(Context context, Camera camera) {
			super(context);
			Log.v("asd", "camera preview");
			// mCamera = camera;
			mHolder = getHolder();
			mHolder.addCallback(this);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			try {
				beginRecording(holder);
			} catch (Exception e) {
				Log.e("asd", e.toString());
				e.printStackTrace();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			Log.v("asd", "camera preview surface change");
		}
	}

	@SuppressLint({ "NewApi", "SimpleDateFormat" })
	private void beginRecording(SurfaceHolder holder) throws Exception {

		Time t = new Time(System.currentTimeMillis());
		dTime = t.getTime();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
		.format(new Date());
		File videoFile = new File(Environment.getExternalStorageDirectory()
				.getPath(),"MemoirRepo");
		if (!videoFile.exists()) {
			videoFile.mkdir();
		}

		videoFile = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/MemoirRepo","videos");
		if (!videoFile.exists()) {
			videoFile.mkdir();
		}

		mFilePath = videoFile + "/video_" + timeStamp + ".mp4";
		try {
			// Camera setup is based on the API Camera Preview demo

			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(640, 480);
			mCamera.setParameters(parameters);
			mCamera.startPreview();
			mCamera.unlock();

			mVideoRecorder = new MediaRecorder();
			mVideoRecorder.setCamera(mCamera);
			mVideoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mVideoRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			mVideoRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			if (camera_type == 1) {
				mVideoRecorder.setOrientationHint(270);
			} else {
				mVideoRecorder.setOrientationHint(90);
			}
			mVideoRecorder.setVideoFrameRate(20);
			mVideoRecorder.setVideoEncodingBitRate(3000000);
			mVideoRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			mVideoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			mVideoRecorder.setAudioSamplingRate(16000);
			mVideoRecorder.setMaxDuration(10000); // limit to 10 seconds
			mVideoRecorder.setPreviewDisplay(holder.getSurface());
			mVideoRecorder.setOutputFile(mFilePath);
			mVideoRecorder.prepare();
			mVideoRecorder.start();

			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						stopRecording();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					mIntent.putExtra(TYPE, "video");
					mIntent.putExtra(LOCATION, mFilePath);
					mIntent.putExtra(TIME, dTime);
					Log.v("asd", "starting db service");
					startService(mIntent);
					mWindowManager.removeView(mPreview);
					stopSelf();
				}
			};
			Handler h = new Handler();
			h.postDelayed(r, 15000);
		} catch (Exception e) {
			Log.e("asd", e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
