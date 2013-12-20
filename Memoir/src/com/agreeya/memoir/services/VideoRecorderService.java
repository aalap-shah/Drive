package com.agreeya.memoir.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
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

import com.agreeya.memoir.MemoirApp;

/**
 * Service to capture the video from front or rear camera
 *
 */
public class VideoRecorderService extends Service {

	private String LOCATION = "location";
	private String TYPE = "type";
	private String TIME = "time";
	private Camera mCamera = null;
	private CameraPreview mPreview = null;
	private WindowManager mWindowManager = null;
	private int camera_type;
	private int trip;
	private String mFilePath,mFramePath;
	private MediaRecorder mVideoRecorder = null;
	double dTime;
	private PendingIntent pi;
	private MediaMetadataRetriever mMediaData;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d("asd", "onCreate of VideoRecorderService");
		super.onCreate();

		Toast.makeText(this.getApplicationContext(), "Video Recorder",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("asd", "onStat of VideoRecorderService");
		if (intent == null)
			return super.onStartCommand(intent, flags, startId);
		Log.v("asd", "video recorder start");
		pi = (PendingIntent) intent.getParcelableExtra("response");
		camera_type = intent.getIntExtra("camera", 99);
		trip = intent.getIntExtra("trip", 699);
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
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d("asd", "onDestroy of VideoRecorderService");
		try {
			stopRecording();
		} catch (Exception e) {
			Log.e("asd", e.toString());
			e.printStackTrace();
		}

		super.onDestroy();
	}

	/**
	 * function to open the camera
	 * 
	 * @return camera instance
	 */
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

	/**
	 * function to stop video recording and releasing the camera instance
	 * @throws Exception
	 */
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

	/**
	 * preview class to change surface view when camera is opened and ready for
	 * capturing videos
	 * 
	 */
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
				.getPath(), "MemoirRepo");
		if (!videoFile.exists()) {
			videoFile.mkdir();
		}

		videoFile = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/MemoirRepo", "videos");
		if (!videoFile.exists()) {
			videoFile.mkdir();
		}

		if (camera_type == 1) {
			mFilePath = videoFile + "/fvideo_" + timeStamp + ".mp4";
			mFramePath = videoFile + "/fvideo_" + timeStamp + ".jpg";
		} else {
			mFilePath = videoFile + "/bvideo_" + timeStamp + ".mp4";
			mFramePath = videoFile + "/bvideo_" + timeStamp + ".jpg";
		}
		try {
			// Camera setup is based on the API Camera Preview demo
			Log.v("asd", "video recording begins");
			Camera.Parameters parameters = mCamera.getParameters();
			Log.v("1", "1");
			parameters.setPreviewSize(640, 480);
			Log.v("2", "2");
			mCamera.setParameters(parameters);
			Log.v("3", "3");
			mCamera.startPreview();
			Log.v("4", "4");
			mCamera.unlock();
			Log.v("5", "5");
//			mVideoRecorder = new MediaRecorder();**************
//			mVideoRecorder.setCamera(mCamera);
//			Log.v("6", "6");
//			mVideoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//			Log.v("7", "7");
//			mVideoRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//			Log.v("8", "8");
//			mVideoRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//			Log.v("9", "9");
//			// mVideoRecorder.setVideoSize(640, 480);
//			mVideoRecorder.setVideoSize(1280, 720);
//			if (camera_type == 1) {
//				mVideoRecorder.setOrientationHint(270);
//			} else {
//				mVideoRecorder.setOrientationHint(90);
//			}
//			mVideoRecorder.setVideoFrameRate(20);
//			Log.v("10", "10");
//			mVideoRecorder.setVideoEncodingBitRate(3000000);
//			Log.v("11", "11");
//			mVideoRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//			Log.v("12", "12");
//			mVideoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//			Log.v("13", "13");
//			mVideoRecorder.setAudioSamplingRate(16000);
//
//			mVideoRecorder.setMaxDuration(10000); // limit to 10 seconds
//			Log.v("14", "14");
//			mVideoRecorder.setPreviewDisplay(holder.getSurface());
//			Log.v("15", "15");
//			mVideoRecorder.setOutputFile(mFilePath);
//			// if(camera_type ==1)
//			// mVideoRecorder.setProfile(CamcorderProfile
//			// .get(1,CamcorderProfile.QUALITY_HIGH));
//			// else
//			// mVideoRecorder.setProfile(CamcorderProfile
//			// .get(CamcorderProfile.QUALITY_HIGH));
//
//			Log.v("16", "16");
//			mVideoRecorder.prepare();
//			Log.v("17", "17");*************************
			
			
			
//			   mCamera = getCameraInstance();
			   mVideoRecorder = new MediaRecorder();

			    // Step 1: Unlock and set camera to MediaRecorder
//			    mCamera.unlock();
			    mVideoRecorder.setCamera(mCamera);

			    // Step 2: Set sources
			    mVideoRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			    mVideoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

			    // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
			    if(camera_type ==1){
			    	mVideoRecorder.setOrientationHint(270);
			    mVideoRecorder.setProfile(CamcorderProfile.get(1,CamcorderProfile.QUALITY_HIGH));
			    }
			    else{
			    	mVideoRecorder.setOrientationHint(90);
			    	mVideoRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
			    }
			    // Step 4: Set output file
			    mVideoRecorder.setOutputFile(mFilePath);

			    // Step 5: Set the preview output
			    mVideoRecorder.setPreviewDisplay(holder.getSurface());
			    
			    try {
			    	mVideoRecorder.prepare();
			    } catch (IllegalStateException e) {
			        Log.d("asd", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
			        stopRecording();
			          } catch (IOException e) {
			        Log.d("asd", "IOException preparing MediaRecorder: " + e.getMessage());
			        stopRecording();
			    
			    }

			try {
				mVideoRecorder.start();
			} catch (Exception e) {
				Log.v("VIDEO RECORDING", e + "");
				if (mCamera != null) {
					mCamera.reconnect();
					mCamera.stopPreview();
					mCamera.release();
					mCamera = null;
				}
			}

			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						stopRecording();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// **********send response to the dispatcher**********//
					
					mMediaData = new MediaMetadataRetriever();
					mMediaData.setDataSource(mFilePath);
					Bitmap bmp = Bitmap.createScaledBitmap(mMediaData.getFrameAtTime(1), 320, 240, true); //Bitmap(320,240,Config.RGB_565); 

					try {
					       FileOutputStream out = new FileOutputStream(mFramePath);
					       bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
					       out.close();
					} catch (Exception e) {
					       e.printStackTrace();
					}
					
					((MemoirApp)getApplicationContext()).il.loadImage(mFramePath, 2, 0);
					Log.v("asd", "video recorder replying");
					Intent i = new Intent();
					i.putExtra("store", true);
					i.putExtra(TYPE, "video");
					i.putExtra(LOCATION, mFilePath);
					i.putExtra(TIME, dTime);
					i.putExtra("trip", trip);
					try {
						pi.send(getApplicationContext(), 3, i);
						// pi.send(2);
					} catch (CanceledException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					mWindowManager.removeView(mPreview);
					bmp.recycle();
					bmp=null;
					System.gc();
					stopSelf();
				}
			};
			Handler h = new Handler();
			h.postDelayed(r, 11000);
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
