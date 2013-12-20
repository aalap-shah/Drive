package com.agreeya.memoir.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.agreeya.memoir.MemoirApp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
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

/**
 * Service to capture the photos from the front or back camera
 * 
 */
public class PhotoShotService extends Service {

	private String LOCATION = "location";
	private String TYPE = "type";
	private String TIME = "time";
	private String PHOTO_PATH = "photo_path";
	private String OUTPUT_PHOTO_PATH = "output_photo_path";
	private String SINGLE = "single";
	private String MULTISHOT = "multishot";
	private String TRIP = "trip";
	private String CAMERA = "camera";
	private String PHOTO = "photo";
	private int trip;
	private Camera mCamera = null;
	private CameraPreview mPreview = null;
	private WindowManager mWindowManager = null;
	static int mImageNumber = 1;
	private int camera_type;
	private String mFilePath;
	private int photo_type;
	private PendingIntent pi;
	private Intent mIntentFFmpeg;

	public class ImageBytes {
		public byte[] data;

		ImageBytes(byte[] data) {
			this.data = data;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v("service1", "photo service created");
		Toast.makeText(this.getApplicationContext(), "Photoshot",
				Toast.LENGTH_LONG).show();
		// mIntent = new Intent(this.getApplicationContext(),
		// InsertIntoDBService.class);
		mIntentFFmpeg = new Intent(this.getApplicationContext(),
				FFMpegService.class);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startID) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startID);

		if (intent == null)
			return Service.START_NOT_STICKY;
		Log.v("service1", "photo service started");

		pi = (PendingIntent) intent.getParcelableExtra("response");
		trip = intent.getIntExtra(TRIP, 499);
		camera_type = intent.getIntExtra(CAMERA, 99);
		Log.v("asd", "camera value : " + camera_type);
		photo_type = intent.getIntExtra(PHOTO, 0);

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
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.v("service1", "Photo Service Destroyed");
		releaseCamera();
		super.onDestroy();

	}

	/**
	 * release the camera instance
	 */
	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
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

	private PictureCallback mPicture = new PictureCallback() {

		private String mRotatedFile;
		private String mThumbnail;
		private String mThumbnailRotate;

		@SuppressLint({ "SimpleDateFormat", "NewApi" })
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Time t = new Time(System.currentTimeMillis());
			double dTime = t.getTime();
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());
			Log.v("asd", "image taken " + data.length + "\nat time : "
					+ timeStamp);
			BitmapFactory.Options option = new Options();
			option.inPurgeable = true;
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
					option);
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
			if (camera_type == 1) {
				mThumbnail = thumbFile + "/image_" + timeStamp + ".jpg";
				mFilePath = pictureFile + "/image_" + timeStamp + ".jpg";
			} else {
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
				bitmap = null;
				resized.recycle();
				resized = null;
				rImg.recycle();
				rImg = null;
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

			((MemoirApp) getApplicationContext()).il
					.loadImage(mThumbnail, 2, 0);

			// **********send response to the dispatcher**********//
			Log.v("service1", "photo service replying");
			releaseCamera();
			mWindowManager.removeView(mPreview);
			Intent i = new Intent();
			if (photo_type == 1)
				i.putExtra(TYPE, SINGLE);
			else
				i.putExtra(TYPE, MULTISHOT);
			// mIntent.putExtra(LOCATION, mFilePath);
			i.putExtra(LOCATION, mThumbnail);
			i.putExtra(TIME, dTime);
			i.putExtra("store", true);
			i.putExtra("trip", trip);
			try {
				pi.send(getApplicationContext(), 2, i);
				// pi.send(2);
			} catch (CanceledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Log.v("asd", "starting db service");
			// startService(mIntent);
			stopSelf();
		}
	};

	/**
	 * preview class to change surface view when camera is opened and ready for
	 * taking image
	 * 
	 */
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
