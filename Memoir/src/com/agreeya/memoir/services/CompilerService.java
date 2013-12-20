package com.agreeya.memoir.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.agreeya.memoir.MemoirApp;
import com.agreeya.memoir.R;
import com.agreeya.memoir.sqlitedatabase.Path;

/**
 * This service compile all the medias(photo,video and audio) of the trip into the Movie
 *
 */
public class CompilerService extends Service {

	private List<Path> pathList;
	private Path path;
	private ArrayList<String> videoPaths;
	private ArrayList<String> audioPaths;
	private ArrayList<String> photoPaths;
	private ArrayList<String> defaultPhotos;
	String videointerpath;
	String audiointerpath;
	// private ArrayList<String> outputVideopaths;
	private int trip_no = 99;
	private int audio_no = 0;
	private int video_no = 0;
	Intent mIntent;
	int compileResult;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		videoPaths = new ArrayList<String>();
		photoPaths = new ArrayList<String>();
		audioPaths = new ArrayList<String>();
		defaultPhotos = new ArrayList<String>();
		defaultPhotos.add("/data/local/img001.jpg");

		// outputVideopaths = new ArrayList<String>();
		mIntent = new Intent(this, FFMpegService.class);
		createNotification();

	}

	@SuppressLint("NewApi")
	private void createNotification() {
		Log.v("asd", "creating notification");
		Notification noti = new Notification.Builder(this)
				.setContentTitle("Compiling Video....")
				.setContentText("Please Wait")
				.setSmallIcon(R.drawable.ic_launcher).build();
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		noti.flags |= Notification.FLAG_NO_CLEAR;
		notificationManager.notify(0, noti);

	}

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		trip_no = ((MemoirApp) this.getApplication()).mDBHelper.getTripId();
		Log.v("compiler", trip_no + "");
		pathList = ((MemoirApp) this.getApplication()).mDBHelper
				.getAllPaths(trip_no);
		if (pathList.size() == 0) {
			Toast.makeText(getApplicationContext(), "Invalid Trip ",
					Toast.LENGTH_LONG).show();
			return Service.START_NOT_STICKY;
		}
		File file = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/MemoirRepo", "trip" + trip_no);
		if (!file.exists()) {
			file.mkdir();
		}

		audiointerpath = file + "/audiointer" + audio_no + ".aac";
		videointerpath = file + "/videointer" + video_no + ".mp4";

		Log.v("path", pathList.size() + "");
		for (int i = 0; i < pathList.size(); i++) {
			path = pathList.get(i);

			if (path.getType().equalsIgnoreCase("single")
					|| path.getType().equalsIgnoreCase("multishot")) {
				if (audioPaths.isEmpty() == false) {

					audiointerpath = file + "/audiointer" + ++audio_no + ".aac";
					mIntent.setAction(FFMpegService.ActionConcatAudios);
					mIntent.putExtra(FFMpegService.AUDIO_PATHS, audioPaths);
					mIntent.putExtra(FFMpegService.OUTPUT_MP3_PATH,
							audiointerpath);
					startService(mIntent);
					videointerpath = file + "/videointer" + ++video_no + ".mp4";
					if (photoPaths.isEmpty()) {
						// merge the concatenated audio with the defaultImages

						mIntent.setAction(FFMpegService.ActionAudioWithDefaultPhotos);
						mIntent.putExtra(FFMpegService.DEFAULT_PHOTO_PATHS,
								defaultPhotos);
						mIntent.putExtra(FFMpegService.AUDIO_PATH,
								audiointerpath);
						mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH,
								videointerpath);
						startService(mIntent);

					} else {
						// merge the concatenated audio with the photoPaths
						mIntent.setAction(FFMpegService.ActionPhotosToVideoWithAudio);
						mIntent.putExtra(FFMpegService.PHOTO_PATHS, photoPaths);
						mIntent.putExtra(FFMpegService.AUDIO_PATH,
								audiointerpath);
						mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH,
								videointerpath);
						startService(mIntent);
					}
					videoPaths.add(videointerpath);
					photoPaths.clear();
					audioPaths.clear();
				}
				photoPaths.add(path.getPath());
			}
			if (path.getType().equalsIgnoreCase("audio")) {
				audioPaths.add(path.getPath());
			}
			if (path.getType().equalsIgnoreCase("video")) {
				if (audioPaths.isEmpty() == false) {
					// Concat all audio
					// make video with concatenated audio and default images
					audiointerpath = file + "/audiointer" + ++audio_no + ".aac";
					mIntent.setAction(FFMpegService.ActionConcatAudios);
					mIntent.putExtra(FFMpegService.AUDIO_PATHS, audioPaths);
					mIntent.putExtra(FFMpegService.OUTPUT_MP3_PATH,
							audiointerpath);
					startService(mIntent);
					videointerpath = file + "/videointer" + ++video_no + ".mp4";
					if (photoPaths.isEmpty()) {
						// merge the concatenated audio with the defaultImages
						mIntent.setAction(FFMpegService.ActionAudioWithDefaultPhotos);
						mIntent.putExtra(FFMpegService.DEFAULT_PHOTO_PATHS,
								defaultPhotos);
						mIntent.putExtra(FFMpegService.AUDIO_PATH,
								audiointerpath);
						mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH,
								videointerpath);
						startService(mIntent);

					} else {
						// merge the concatenated audio with the photoPaths
						mIntent.setAction(FFMpegService.ActionPhotosToVideoWithAudio);
						mIntent.putExtra(FFMpegService.PHOTO_PATHS, photoPaths);
						mIntent.putExtra(FFMpegService.AUDIO_PATH,
								audiointerpath);
						mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH,
								videointerpath);
						startService(mIntent);
						photoPaths.clear();
					}
					videoPaths.add(videointerpath);
				}
				if (photoPaths.isEmpty() == false) {
					// make video with photos and silence
					videointerpath = file + "/videointer" + ++video_no + ".mp4";
					mIntent.setAction(FFMpegService.ActionPhotosToVideo);
					mIntent.putExtra(FFMpegService.PHOTO_PATHS, photoPaths);
					mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH,
							videointerpath);
					startService(mIntent);
					videoPaths.add(videointerpath);
				}
				if (path.getPath().contains("fvideo")) {
					// rotate video by 270 degree
					videointerpath = file + "/videointer" + ++video_no + ".mp4";
					mIntent.setAction(FFMpegService.ActionRotateVideo);
					mIntent.putExtra("transpose", 2);
					mIntent.putExtra(FFMpegService.VIDEO_PATH, path.getPath());
					mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH,
							videointerpath);
					startService(mIntent);
				}
				if (path.getPath().contains("bvideo")) {
					// rotate video by 90 degree
					videointerpath = file + "/videointer" + ++video_no + ".mp4";
					mIntent.setAction(FFMpegService.ActionRotateVideo);
					mIntent.putExtra("transpose", 1);
					mIntent.putExtra(FFMpegService.VIDEO_PATH, path.getPath());
					mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH,
							videointerpath);
					startService(mIntent);
				}
				videoPaths.add(videointerpath);
				// videoPaths.add(path.getPath());
				audioPaths.clear();
				photoPaths.clear();
			}
		}
		if (photoPaths.isEmpty() == false && audioPaths.isEmpty() == false) {
			videointerpath = file + "/videointer" + ++video_no + ".mp4";
			mIntent.setAction(FFMpegService.ActionPhotosToVideoWithAudio);
			mIntent.putExtra(FFMpegService.PHOTO_PATHS, photoPaths);
			mIntent.putExtra(FFMpegService.AUDIO_PATH, audiointerpath);
			mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH, videointerpath);
			videoPaths.add(videointerpath);
			startService(mIntent);

		} else if (photoPaths.isEmpty() == false) {
			// make video with photos and silence
			videointerpath = file + "/videointer" + ++video_no + ".mp4";
			mIntent.setAction(FFMpegService.ActionPhotosToVideo);
			mIntent.putExtra(FFMpegService.PHOTO_PATHS, photoPaths);
			mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH, videointerpath);
			videoPaths.add(videointerpath);
			startService(mIntent);
		} else if (audioPaths.isEmpty() == false) {
			// Concat all audio
			// make video with concatenated audio and default images
			audiointerpath = file + "/audiointer" + ++audio_no + ".aac";
			mIntent.setAction(FFMpegService.ActionConcatAudios);
			mIntent.putExtra(FFMpegService.AUDIO_PATHS, audioPaths);
			mIntent.putExtra(FFMpegService.OUTPUT_MP3_PATH, audiointerpath);
			startService(mIntent);
			audioPaths.add(audiointerpath);

			videointerpath = file + "/videointer" + ++video_no + ".mp4";
			mIntent.setAction(FFMpegService.ActionAudioWithDefaultPhotos);
			mIntent.putExtra(FFMpegService.DEFAULT_PHOTO_PATHS, defaultPhotos);
			mIntent.putExtra(FFMpegService.AUDIO_PATH, audiointerpath);
			mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH, videointerpath);
			videoPaths.add(videointerpath);
			startService(mIntent);

		}
		if (videoPaths.isEmpty() == false) {
			// Concat all videos
			videointerpath = file + "/final" + ".mp4";
			mIntent.setAction(FFMpegService.ActionConcatVideos);
			mIntent.putExtra(FFMpegService.VIDEO_PATHS, videoPaths);
			mIntent.putExtra(FFMpegService.OUTPUT_VIDEO_PATH, videointerpath);
			mIntent.putExtra("last", 1);
			startService(mIntent);
		}

		return Service.START_NOT_STICKY;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
