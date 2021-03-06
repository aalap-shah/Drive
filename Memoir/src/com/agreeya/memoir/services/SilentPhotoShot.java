package com.agreeya.memoir.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.widget.Toast;

public class SilentPhotoShot extends Service {

	private String LOCATION = "location";
	private String TYPE = "type";
	private String TIME = "time";
	private Camera mCamera = null;
	private CameraPreview mPreview = null;
	private WindowManager mWindowManager = null;
	static int mImageNumber = 1;
	private int camera_type;
	private String mFilePath;
	private Intent mIntent;
	private int photo_type;
	private Intent mIntentFFmpeg;
	private String PHOTO_PATH = "photo_path";
	private String OUTPUT_PHOTO_PATH = "output_photo_path";
	private String SINGLE = "single";
	private String MULTISHOT = "multishot";

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v("asd", "onCreate2");
		Toast.makeText(this.getApplicationContext(), "Photoshot",
				Toast.LENGTH_LONG).show();
		mIntent = new Intent(this.getApplicationContext(), InsertIntoDB.class);
		mIntentFFmpeg = new Intent(this.getApplicationContext(),
				FFMpegService.class);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startID) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startID);
		Log.v("asd", "onStart");

		camera_type = intent.getIntExtra("camera", 99);
		Log.v("asd", "camera value : " + camera_type);
		photo_type = intent.getIntExtra("photo", 0);

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

		return super.onStartCommand(intent, flags, startID);
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

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
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

	private PictureCallback mPicture = new PictureCallback() {

		private String mRotatedFile;
		private String mThumbnail;
		private String mThumbnailRotate;

		@SuppressLint("SimpleDateFormat")
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Time t = new Time(System.currentTimeMillis());
			double dTime = t.getTime();
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());

			Log.v("asd", "image taken " + data.length + "\nat time : "
					+ timeStamp);
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			Log.v("asd", "1");
			Matrix rotateRight = new Matrix();
			rotateRight.preRotate(90);

			if (android.os.Build.VERSION.SDK_INT > 13 && camera_type == 1) {
				float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
				Log.v("asd", "2");
				rotateRight = new Matrix();
				Matrix matrixMirrorY = new Matrix();
				Log.v("asd", "3");
				matrixMirrorY.setValues(mirrorY);
				rotateRight.postConcat(matrixMirrorY);
				rotateRight.preRotate(270);

			}
			Log.v("asd", "4 " + bitmap.getWidth() + " " + bitmap.getHeight());
			Bitmap rImg;
			if (camera_type == 1)
				rImg = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
						bitmap.getHeight(), rotateRight, true);
			else
				rImg = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
						bitmap.getHeight(), null, true);

			Bitmap resized = ThumbnailUtils.extractThumbnail(rImg, 320, 240);
			Log.v("asd", "5");

			File pictureFile = new File(Environment
					.getExternalStorageDirectory().getPath(), "MemoirRepo");
			if (!pictureFile.exists()) {
				pictureFile.mkdir();
			}

			pictureFile = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/MemoirRepo", "images");
			if (!pictureFile.exists()) {
				pictureFile.mkdir();
			}

			File thumbFile = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/MemoirRepo", "thumbimages");
			if (!thumbFile.exists()) {
				thumbFile.mkdir();
			}
			if (camera_type == 1){
				mThumbnail = thumbFile + "/image_" + timeStamp + ".jpg";
				mFilePath = pictureFile + "/image_" + timeStamp + ".jpg";
			}
			else{
				mThumbnail = thumbFile + "/imagetmp_" + timeStamp + ".jpg";
				mThumbnailRotate = thumbFile + "/image_" + timeStamp + ".jpg";
				mFilePath = pictureFile + "/imagetmp_" + timeStamp + ".jpg";
				mRotatedFile = pictureFile + "/image_" + timeStamp + ".jpg";				
			}

			try {
				Log.v("asd", "saving image");
				FileOutputStream fos = new FileOutputStream(mFilePath);
				rImg.compress(CompressFormat.JPEG, 90, fos);
				fos.close();
				FileOutputStream tfos = new FileOutputStream(mThumbnail);
				resized.compress(CompressFormat.JPEG, 90, tfos);
				tfos.close();
				bitmap.recycle();
				resized.recycle();
				rImg.recycle();
				System.gc();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Log.d("asd", "Image Captured at " + pictureFile.getAbsolutePath());

			// rotating photo when clicked with from back camera
			if (camera_type == 0) {
				
				mIntentFFmpeg.setAction(FFMpegService.ActionRotatePhoto);
				mIntentFFmpeg.putExtra(PHOTO_PATH, mThumbnail);
				mIntentFFmpeg.putExtra(OUTPUT_PHOTO_PATH, mThumbnailRotate);
				startService(mIntentFFmpeg);
				mThumbnail = mThumbnailRotate;
				mIntentFFmpeg.setAction(FFMpegService.ActionRotatePhoto);
				mIntentFFmpeg.putExtra(PHOTO_PATH, mFilePath);
				mIntentFFmpeg.putExtra(OUTPUT_PHOTO_PATH, mRotatedFile);
				startService(mIntentFFmpeg);
				
			}

			// store the file location, type and time in database
			if (photo_type == 1)
				mIntent.putExtra(TYPE, SINGLE);
			else
				mIntent.putExtra(TYPE, MULTISHOT);
			// mIntent.putExtra(LOCATION, mFilePath);
			mIntent.putExtra(LOCATION, mThumbnail);
			mIntent.putExtra(TIME, dTime);
			Log.v("asd", "starting db service");
			startService(mIntent);
			releaseCamera();
			mWindowManager.removeView(mPreview);
			stopSelf();
		}
	};

	public class CameraPreview extends SurfaceView implements
			SurfaceHolder.Callback {
		private SurfaceHolder mHolder;
		private Camera mCamera;

		public CameraPreview(Context context, Camera camera) {
			super(context);
			Log.v("asd", "camera preview");
			mCamera = camera;
			mHolder = getHolder();
			mHolder.addCallback(this);
		}

		public void surfaceCreated(SurfaceHolder holder) {

		}

		public void surfaceDestroyed(SurfaceHolder holder) {
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			Log.v("asd", "camera preview surface change");
			try {
				if (mCamera != null) {
					Log.v("asd", "taking image");
					mCamera.setPreviewDisplay(holder);
					mCamera.setDisplayOrientation(90);
					mCamera.startPreview();
					mCamera.autoFocus(new AutoFocusCallback() {

						@Override
						public void onAutoFocus(boolean arg0, Camera arg1) {
							mCamera.takePicture(null, null, mPicture);

						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
