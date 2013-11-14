package com.agreeya.memoir.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;

import com.agreeya.memoir.sqlitedatabase.InsertIntoDB;

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

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Time t = new Time(System.currentTimeMillis());
			double dTime = t.getTime();
			Log.v("asd", "image taken " + data.length + "\nat time : " + dTime);
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

			File pictureFile = new File(Environment.getExternalStorageDirectory()
					.getPath(),"MemoirRepo");
			if (!pictureFile.exists()) {
				pictureFile.mkdir();
			}

			pictureFile = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/MemoirRepo","images");
			if (!pictureFile.exists()) {
				pictureFile.mkdir();
			}
			
			File thumbFile = new File(Environment.getExternalStorageDirectory()
					.getPath()+"/MemoirRepo", "thumbimages");
			if (!thumbFile.exists()) {
				thumbFile.mkdir();
			}
			mThumbnail = thumbFile + "/image" + mImageNumber + ".jpg";
			try {
				Log.v("asd", "saving image");
				if (camera_type == 1)
					mFilePath = pictureFile + "/image" + mImageNumber + ".jpg";
				else {
					mFilePath = pictureFile + "/imagetmp" + mImageNumber
							+ ".jpg";
					mRotatedFile = pictureFile + "/image" + mImageNumber
							+ ".jpg";
				}
				mImageNumber++;
				FileOutputStream fos = new FileOutputStream(mFilePath);
				rImg.compress(CompressFormat.JPEG, 90, fos);
				fos.close();
				FileOutputStream tfos = new FileOutputStream(mThumbnail);
				resized.compress(CompressFormat.JPEG, 90, tfos);
				tfos.close();
				bitmap.recycle();
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
				mIntentFFmpeg.putExtra(PHOTO_PATH, mFilePath);
				mIntentFFmpeg.putExtra(OUTPUT_PHOTO_PATH, mRotatedFile);
				startService(mIntentFFmpeg);
				mFilePath = mRotatedFile;
			}

			// store the file location, type and time in database
			if (photo_type == 1)
				mIntent.putExtra(TYPE, "single");
			else
				mIntent.putExtra(TYPE, "multiShot");
			mIntent.putExtra(LOCATION, mFilePath);
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
