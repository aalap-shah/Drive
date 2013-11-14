package com.agreeya.memoir.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.util.Log;

public class FFMpegService extends IntentService {

	public FFMpegService() {
		super("Service");
		// TODO Auto-generated constructor stub
	}

	private String mFfmpegPath = null;
	private MediaMetadataRetriever mMediaData;
	public static String ActionPhotoToVideo = "ActionPhotoToVideo";
	public static String ActionPhotoToVideoUpdate = "ActionPhotoToVideoUpdate";
	public static String ActionPhotosToVideo = "ActionPhotosToVideo";
	public static String ActionPhotosToVideoUpdate = "ActionPhotosToVideoUpdate";
	public static String ActionPhotoToVideoWithAudio = "ActionPhotoToVideoWithAudio";
	public static String ActionPhotoToVideoWithAudioUpdate = "ActionPhotoToVideoWithAudioUpdate";
	public static String ActionPhotosToVideoWithAudio = "ActionPhotosToVideoWithAudio";
	public static String ActionPhotosToVideoWithAudioUpdate = "ActionPhotosToVideoWithAudioUpdate";
	public static String ActionConcatVideos = "ActionConcatVideos";
	public static String ActionConcatVideosUpdate = "ActionConcatVideosUpdate";
	public static String ActionConvertWavToMp3 = "ActionConvertWavToMp3";
	public static String ActionRotatePhoto = "ActionRotatePhoto";
	public static String ActionAudioWithDefaultPhotos = "ActionAudioWithDefaultPhotos";

	private String mPhotoPath;
	private String mOutputPhotoPath;
	private String mOutputVideoPath;
	private long mTime;
	private String mSilencePath;
	private ArrayList<String> mPhotoPaths;
	private ArrayList<String> mVideoPaths;
	private ArrayList<String> mDefaultPhotoPaths;
	private String mAudioPath;
	String mDuration;
	private String mOutputMp3Path;
	
	private String PHOTO_PATH = "photo_path";
	private String PHOTO_PATHS = "photo_paths";
	private String OUTPUT_PHOTO_PATH = "output_photo_path";
	private String OUTPUT_VIDEO_PATH = "output_video_path";
	private String TIME = "time";
	private String DEFAULT_PHOTO_PATHS = "default_photo_paths";
	private String VIDEO_PATHS = "video_paths";
	private String AUDIO_PATH = "audio_path";
	private String OUTPUT_MP3_PATH = "output_mp3_path";

	// h264_mp4toannexb encoder/decoder
	// Convert an H.264 bitstream from length prefixed mode to start code
	// prefixed mode
	private String mF_VIDEO_CODEC_MP4_ANNEXB = " -vbsf h264_mp4toannexb ";
	// convert
	private String mF_AUDIO_BITSTREAM_ADTSTOASC = " -bsf:a aac_adtstoasc ";
	// copy codec
	private String mF_CODEC_COPY = " -c copy ";
	// concat the intermediate video file
	private String mF_CONCAT_INTERMEDIATE = "concat:";
	// 30 frames per second;
	private String mF_VIDEO_FRAME_RATE_30 = " -r 30 ";
	// Audio codec AAC
	private String mF_AUDIO_CODEC_AAC = " -c:a aac ";
	// Audio frequency 48000
	private String mF_AUDIO_FREQUENCY_48K = " -ar 48000 ";
	// Audio channels 2
	private String mF_AUDIO_CHANNEL_2 = " -ac 2 ";
	// Audio bit rate of 160Kb/s
	private String mF_AUDIO_BIT_RATE_160K = " -b:a 160k ";
	// Codec are strict experimental
	private String mF_STRICT_EXPERIMENTAL = " -strict experimental ";
	// Video Format type = mpegts
	private String mF_VIDEO_FORMAT_MPEGTS = " -f mpegts ";
	// Image Loop
	private String mF_IMAGE_LOOP = " -loop 1 ";
	// Image Type image2
	private String mF_IMAGE_FORMAT_IMAGE2 = " -f image2 ";
	// type mp2
	private String mF_AUDIO_FORMAT_mp2 = " -f mp2 ";
	// Video codec 2 - used for image translation
	private String mF_VIDEO_CODEC_LIBX264 = " -c:v libx264 ";
	// Video Frame Size
	private String mF_VIDEO_FRAME_SIZE = " -s 1920x1080 ";
	// Time for Video-Should be redefined as selected no of seconds.
	private String mF_VIDEO_TIME_SEC = " -t ";
	// map the stream 0
	private String mF_MAP_STREAM_0 = " -map 0 ";
	// map the stream 1
	private String mF_MAP_STREAM_1 = " -map 1 ";
	// -vf setdar=1:1
	private String mF_SET_SAR_1_1 = " -vf setsar=1:1 ";
	// -vf setdar=16:9
	private String mF_SET_DAR_16_9 = " -vf setdar=16:9 ";
	// transpose 90 deg clockwise
	private String mF_TRANSPOSE_RIGHT = " -vf transpose=1 ";
	// -shortest
	private String mF_SHORTEST = " -shortest ";

	private interface iOutput {
		void onOutput(String output);
	}

	private interface iError {
		void onError(String error);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v("Service", "onCreate");
		validateFFmpeg();
		validateSilenceFile();
	}

	// function to rotate images 90 deg clockwise
	public void rotateImage(String photoPath, String outputPhotoPath) {

		// ./ffmpeg -y -i <path> -vf "transpose=1" <output>

		runCommand(mFfmpegPath + " -y " + " -i " + photoPath
				+ mF_TRANSPOSE_RIGHT + outputPhotoPath, null, new iError() {

			@Override
			public void onError(String error) {
				int frame = ffmpegUpdateParser(error);
				if (frame != -1) {
					Log.d("asd", "Frames=" + frame);

				}
			}
		});

	}

	// make the video of given time duration from a single image
	public void photoToVideo(String photoPath, String outputVideoPath,
			long time, String silenceAudioPath) {

		// ./ffmpeg -y -i silence.mp2 -f image2 -loop 1 -i 1.png -c:v libx264 -s
		// 1920x1080 -vf setsar=1:1 -vf setdar=16:9 -r 30 -c:a aac -ar 48000
		// -b:a 160k -ac 2 -strict experimental -t 2 1.png.mp4

		runCommand(mFfmpegPath + " -y " + " -i " + silenceAudioPath
				+ mF_IMAGE_FORMAT_IMAGE2 + mF_IMAGE_LOOP + " -i " + photoPath
				+ mF_VIDEO_CODEC_LIBX264 + mF_VIDEO_FRAME_SIZE + mF_SET_SAR_1_1
				+ mF_SET_DAR_16_9 + mF_VIDEO_FRAME_RATE_30 + mF_AUDIO_CODEC_AAC
				+ mF_AUDIO_FREQUENCY_48K + mF_AUDIO_BIT_RATE_160K
				+ mF_AUDIO_CHANNEL_2 + mF_STRICT_EXPERIMENTAL
				+ mF_VIDEO_TIME_SEC + time + " " + outputVideoPath, null,
				new iError() {

					@Override
					public void onError(String error) {
						int frame = ffmpegUpdateParser(error);
						if (frame != -1) {
							Log.d("asd", "Frames=" + frame);

						}
					}
				});

	}

	// Take bunch of photo and creates the video of given time duration
	public void photosToVideo(ArrayList<String> photoPaths,
			String outputVideoPath, long time, String silenceAudioPath) {

		int index = 1; // for sequencing the images

		String tempVideoPath = outputVideoPath + ".tmp.mp4";
		File direct = new File(getApplicationContext().getFilesDir()
				.getAbsolutePath(), "img");
		if (!direct.exists()) {
			direct.mkdir();
		}

		// copying all the images to temporary folder
		// ./ffmpeg -y -i <path> -s 1920x1080 /<pkg path>/img@@@.jpg
		for (String path : photoPaths) {
			runCommand(
					mFfmpegPath + " -y " + " -i " + path + mF_VIDEO_FRAME_SIZE
							+ direct.getAbsoluteFile()
							+ String.format("%03d", index) + ".jpg", null,
					new iError() {
						@Override
						public void onError(String error) {
							Log.d("asd", error);
						}
					});
			index++;
		}

		// creating the video having the given delay time interval between the
		// images
		// ./ffmpeg -y -loop 0 -f image2 -r 1/<time> -i img%03d.jpg -vcodec
		// libx264 -s 1920x1080 -r 30 outcheck.mp4
		runCommand(mFfmpegPath + " -y " + " -loop 0 " + mF_IMAGE_FORMAT_IMAGE2
				+ " -r 1/" + time + " -i " + direct.getAbsolutePath()
				+ "%03d.jpg" + mF_STRICT_EXPERIMENTAL + mF_VIDEO_CODEC_LIBX264
				+ mF_VIDEO_FRAME_SIZE + mF_VIDEO_FRAME_RATE_30 + tempVideoPath,
				null, new iError() {
					@Override
					public void onError(String error) {
						Log.d("asd", error);
					}
				});

		// mixing silence.mp2 audio with the temporary audio to get final output
		// ./ffmpeg -y -i video.mp4 -i audio.mp3 -shortest -map 0 -map 1 -c copy
		// output.mp4
		runCommand(mFfmpegPath + " -y " + " -i " + tempVideoPath + " -i "
				+ silenceAudioPath + mF_SHORTEST + mF_MAP_STREAM_0
				+ mF_MAP_STREAM_1 + mF_CODEC_COPY + outputVideoPath, null,
				new iError() {

					@Override
					public void onError(String error) {
						int frame = ffmpegUpdateParser(error);
						if (frame != -1) {
							Log.d("asd", "Frames=" + frame);

						}
					}
				});
	}

	// make a video from a single image and audio, with the duration equals the
	// duration of audio
	public void photoToVideoWithAudio(String photoPath, String audioPath,
			String outputVideoPath) {

		// ./ffmpeg -y -i audiomp3.mp3 -f image2 -loop 1 -i 1.png -shortest -c:v
		// libx264 -s 1920x1080 -vf setsar=1:1 -vf setdar=16:9 -r 30 -c:a aac
		// -ar 48000 -b:a 160k -ac 2 -strict experimental 1.png.mp4

		runCommand(mFfmpegPath + " -y " + " -i " + audioPath
				+ mF_IMAGE_FORMAT_IMAGE2 + mF_IMAGE_LOOP + " -i " + photoPath
				+ mF_SHORTEST + mF_VIDEO_CODEC_LIBX264 + mF_VIDEO_FRAME_SIZE
				+ mF_SET_SAR_1_1 + mF_SET_DAR_16_9 + mF_VIDEO_FRAME_RATE_30
				+ mF_AUDIO_CODEC_AAC + mF_AUDIO_FREQUENCY_48K
				+ mF_AUDIO_BIT_RATE_160K + mF_AUDIO_CHANNEL_2
				+ mF_STRICT_EXPERIMENTAL + " " + outputVideoPath, null,
				new iError() {

					@Override
					public void onError(String error) {
						int frame = ffmpegUpdateParser(error);
						if (frame != -1) {
							Log.d("asd", "Frames=" + frame);

						}
					}
				});

	}

	// convert wav audio to mp3 audio
	public void convertWavToMp3(String wavPath, String mp3Path) {

		// ./ffmpeg -y -i audio.wav -f mp2 output.mp3

		runCommand(mFfmpegPath + " -y " + " -i " + wavPath
				+ mF_AUDIO_FORMAT_mp2 + mp3Path, null, new iError() {

			@Override
			public void onError(String error) {
				int frame = ffmpegUpdateParser(error);
				if (frame != -1) {
					Log.d("asd", "Frames=" + frame);

				}
			}
		});

	}

	// Take bunch of photos and creates the video of given time duration with
	// audio mixed.
	@SuppressLint("NewApi")
	public void photosToVideoWithAudio(ArrayList<String> photoPaths,
			String audioPath, String outputVideoPath) {
		int index = 1; // for sequencing the images

		mMediaData = new MediaMetadataRetriever();
		mMediaData.setDataSource(audioPath);
		mDuration = mMediaData
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		long dur = Integer.parseInt(mDuration);
		long timePerImage = dur / (photoPaths.size() * 1000);

		String tempVideoPath = outputVideoPath + ".tmp.mp4";
		File direct = new File(getApplicationContext().getFilesDir()
				.getAbsolutePath(), "img");
		if (!direct.exists()) {
			direct.mkdir();
		}

		// copying all the images to temporary folder
		// ./ffmpeg -y -i <path> -s 1920x1080 /data/local/temp/img@@@.jpg
		for (String path : photoPaths) {
			runCommand(
					mFfmpegPath + " -y " + " -i " + path + mF_VIDEO_FRAME_SIZE
							+ direct.getAbsoluteFile()
							+ String.format("%03d", index) + ".jpg", null,
					new iError() {
						@Override
						public void onError(String error) {
							Log.d("asd", error);
						}
					});
		}

		// creating the video having the delay time interval between the images
		// on basis of audio duration
		// ./ffmpeg -y -loop 0 -f image2 -r 1/<duration> -i img%03d.jpg -vcodec
		// libx264 -s 1920x1080 -r 30 outcheck.mp4
		runCommand(mFfmpegPath + " -y " + " -loop 0 " + mF_IMAGE_FORMAT_IMAGE2
				+ " -r 1/" + timePerImage + " -i " + direct.getAbsolutePath()
				+ "%03d.jpg" + mF_STRICT_EXPERIMENTAL + mF_VIDEO_CODEC_LIBX264
				+ mF_VIDEO_FRAME_SIZE + mF_VIDEO_FRAME_RATE_30 + tempVideoPath,
				null, new iError() {
					@Override
					public void onError(String error) {
						Log.d("asd", error);
					}
				});

		// mixing audio with the temporary video created from images
		// ./ffmpeg -y -i video.mp4 -i audio.mp3 -map 0 -map 1 -c copy
		// output.mp4
		runCommand(mFfmpegPath + " -y " + " -i " + tempVideoPath + " -i "
				+ audioPath + mF_MAP_STREAM_0 + mF_MAP_STREAM_1 + mF_CODEC_COPY
				+ outputVideoPath, null, new iError() {

			@Override
			public void onError(String error) {
				int frame = ffmpegUpdateParser(error);
				if (frame != -1) {
					Log.d("asd", "Frames=" + frame);

				}
			}
		});

	}

	// concat the videos into the single video
	@SuppressLint("NewApi")
	public void concatVideos(ArrayList<String> videoPaths,
			String outputVideoPath) {
		int index = 1;
		// int totalDuration = 0;
		mMediaData = new MediaMetadataRetriever();

		StringBuffer intermediate = new StringBuffer();

		// convert each video into the intermediate form for concatenating
		for (String path : videoPaths) {

			mMediaData.setDataSource(path);
			mDuration = mMediaData
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			// totalDuration += Integer.parseInt(mDuration);
			// ./ffmpeg -y -i <path> -c copy -vbsf h264_mp4toannexb
			// -f mpegts <path>-001.ts
			runCommand(
					mFfmpegPath + " -y " + " -i " + path
							+ mF_VIDEO_CODEC_MP4_ANNEXB
							+ mF_STRICT_EXPERIMENTAL
							+ "-vf fade=in:0:60,fade=out:"
							+ ((Integer.parseInt(mDuration) * 30 / 1000) - 60)
							+ ":60" + mF_VIDEO_FORMAT_MPEGTS + outputVideoPath
							+ "-" + String.format("%03d", index++) + ".ts",
					null, new iError() {
						@Override
						public void onError(String error) {
							Log.d("asd", error);
						}
					});

		}

		for (int j = 1; j < index; j++) {
			intermediate.append(outputVideoPath + "-"
					+ String.format("%03d", j) + ".ts");
			if (j != index - 1) {
				intermediate.append("|");
			}
		}

		// ffmpeg -y -i concat:inter1.ts|inter2.ts -c:v libx264 -strict
		// experimental -bsf:a aac_adtstoasc output.mp4
		runCommand(mFfmpegPath + " -y" + " -i " + mF_CONCAT_INTERMEDIATE
				+ intermediate + mF_VIDEO_CODEC_LIBX264
				+ mF_STRICT_EXPERIMENTAL
				// + "-vf fade=in:0:100,fade=out:" +
				// ((totalDuration*30/1000)-100) +":100"
				+ mF_AUDIO_BITSTREAM_ADTSTOASC + outputVideoPath, null,
				new iError() {
					@Override
					public void onError(String error) {
						Log.d("asd", error);
					}
				});
	}

	// creates the video by mixing the audio with the default images
	@SuppressLint("NewApi")
	public void audioToVideo(String audioPath,
			ArrayList<String> defaultPhotosPath, String outputVideoPath) {

		String tempVideoPath = outputVideoPath + ".tmp.mp4";

		int index = 1; // for sequencing the images

		File direct = new File(getApplicationContext().getFilesDir()
				.getAbsolutePath(), "img");
		if (!direct.exists()) {
			direct.mkdir();
		}

		mMediaData = new MediaMetadataRetriever();
		mMediaData.setDataSource(audioPath);
		mDuration = mMediaData
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		long dur = Integer.parseInt(mDuration);
		long timePerImage = dur / (defaultPhotosPath.size() * 1000);

		// copying all the images to temporary folder
		// ./ffmpeg -y -i <path> -s 1920x1080 /data/local/img@@@.jpg
		for (String path : defaultPhotosPath) {
			runCommand(
					mFfmpegPath + " -y " + " -i " + path + mF_VIDEO_FRAME_SIZE
							+ direct.getAbsoluteFile()
							+ String.format("%03d", index) + ".jpg", null,
					new iError() {
						@Override
						public void onError(String error) {
							Log.d("asd", error);
						}
					});
			index++;
		}

		// creating the temporary video from the sequenced images
		// ./ffmpeg -y -loop 0 -f image2 -r 1/<timePerImage> -i img%03d.jpg
		// -vcodec libx264 -s 1920x1080 -r 30 outcheck.mp4
		runCommand(mFfmpegPath + " -y " + " -loop 0 " + mF_IMAGE_FORMAT_IMAGE2
				+ " -r 1/" + timePerImage + " -i " + direct.getAbsolutePath()
				+ "%03d.jpg" + mF_STRICT_EXPERIMENTAL + mF_VIDEO_CODEC_LIBX264
				+ mF_VIDEO_FRAME_SIZE + mF_VIDEO_FRAME_RATE_30 + tempVideoPath,
				null, new iError() {
					@Override
					public void onError(String error) {
						Log.d("asd", error);
					}
				});

		// mixing audio with the video created from default images
		// ./ffmpeg -y -i video.mp4 -i audio.mp3 -map 0 -map 1 -c copy
		// output.mp4
		runCommand(mFfmpegPath + " -y " + " -i " + tempVideoPath + " -i "
				+ audioPath + mF_MAP_STREAM_0 + mF_MAP_STREAM_1 + mF_CODEC_COPY
				+ outputVideoPath, null, new iError() {

			@Override
			public void onError(String error) {
				int frame = ffmpegUpdateParser(error);
				if (frame != -1) {
					Log.d("asd", "Frames=" + frame);

				}
			}
		});

		for (int j = 1; j < index; j++) {
			File file = new File(direct.getAbsolutePath() + "-"
					+ String.format("%03d", j) + ".jpg");
			file.delete();
		}

	}

	private int ffmpegUpdateParser(String str) {
		if (str.contains("frame=") && str.contains("fps=")
				&& str.contains("time=")) {
			int inxOfFrame = str.indexOf("frame=");
			String noOfFrames = str.substring(inxOfFrame + 6, inxOfFrame + 12);
			return Integer.parseInt(noOfFrames.trim());
		}
		return -1;
	}

	public void runCommand(String Command, iOutput outputCb, iError errorCb) {
		Log.d("asd", "Running : " + Command);
		try {
			int read;
			char[] buffer = new char[4096];

			// Executes the command.
			Process process = Runtime.getRuntime().exec(Command);
			// NOTE: You can write to stdin of the command using
			// process.getOutputStream().
			// BufferedWriter writer = new BufferedWriter(new
			// OutputStreamWriter(
			// process.getOutputStream()));
			// writer.write("y");
			// writer.flush();

			if (errorCb != null) {
				BufferedReader errorReader = new BufferedReader(
						new InputStreamReader(process.getErrorStream()));
				while ((read = errorReader.read(buffer)) > 0) {
					errorCb.onError(String.copyValueOf(buffer, 0, read));
				}
				errorReader.close();
			}

			// Waits for the command to finish.
			process.waitFor();
			Log.d("asd", "Command ended");
			if (outputCb != null) {
				BufferedReader outputReader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				StringBuffer output = new StringBuffer();
				while ((read = outputReader.read(buffer)) > 0) {
					output.append(buffer, 0, read);
				}
				outputReader.close();
				outputCb.onOutput(output.toString());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressLint("NewApi")
	private void validateFFmpeg() {
		File ffmpegFile = new File(this.getFilesDir().getAbsolutePath(),
				"ffmpeg");
		if (ffmpegFile.exists()) {
			if (ffmpegFile.canExecute()) {
				mFfmpegPath = ffmpegFile.getAbsolutePath();
				return;
			}
		}
		try {
			ffmpegFile.delete();

			// NOTE : FFmpeg needs to be in private and internal memory. Other
			// media files can be in external private memory.
			InputStream ffmpegIS = this.getAssets().open("ffmpeg");
			OutputStream ffmpegOS = new FileOutputStream(ffmpegFile);

			byte[] buf = new byte[4086];
			int len;
			while ((len = ffmpegIS.read(buf)) > 0) {
				ffmpegOS.write(buf, 0, len);
			}
			ffmpegIS.close();
			ffmpegOS.close();

			runCommand("/system/bin/chmod 744 " + ffmpegFile.getAbsolutePath(),
					null, null);
			if (ffmpegFile.canExecute()) {
				mFfmpegPath = ffmpegFile.getAbsolutePath();
				return;
			}
			Log.e("asd", "FFmpeg not valid");
			stopSelf();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void validateSilenceFile() {
		File silenceFile = new File(this.getFilesDir().getAbsolutePath(),
				"silence.mp2");
		Log.d("asd", "Silence File Path = " + silenceFile);
		if (silenceFile.exists()) {
			mSilencePath = silenceFile.getAbsolutePath();
			return;
		}
		try {
			silenceFile.delete();

			InputStream silenceIS = this.getAssets().open("silence.mp2");
			OutputStream silenceOS = new FileOutputStream(silenceFile);

			byte[] buf = new byte[4086];
			int len;
			while ((len = silenceIS.read(buf)) > 0) {
				silenceOS.write(buf, 0, len);
			}
			silenceIS.close();
			silenceOS.close();
			mSilencePath = silenceFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

		Log.v("Service", "onHandleIntent");
		Log.v("check Action", intent.getAction());

		if (intent.getAction().equals(FFMpegService.ActionPhotoToVideo)) {
			mPhotoPath = intent.getStringExtra(PHOTO_PATH);
			mOutputVideoPath = intent.getStringExtra(OUTPUT_VIDEO_PATH);
			mTime = intent.getIntExtra(TIME, 10);
			photoToVideo(mPhotoPath, mOutputVideoPath, mTime, mSilencePath);

		} else if (intent.getAction().equals(FFMpegService.ActionPhotosToVideo)) {
			mPhotoPaths = intent.getStringArrayListExtra(PHOTO_PATHS);
			mOutputVideoPath = intent.getStringExtra(OUTPUT_VIDEO_PATH);
			mTime = intent.getIntExtra(TIME, 3);
			photosToVideo(mPhotoPaths, mOutputVideoPath, mTime, mSilencePath);

		} else if (intent.getAction().equals(
				FFMpegService.ActionPhotoToVideoWithAudio)) {
			mPhotoPath = intent.getStringExtra(PHOTO_PATH);
			mAudioPath = intent.getStringExtra(AUDIO_PATH);
			mOutputVideoPath = intent.getStringExtra(OUTPUT_VIDEO_PATH);
			photoToVideoWithAudio(mPhotoPath, mAudioPath, mOutputVideoPath);

		} else if (intent.getAction().equals(
				FFMpegService.ActionPhotosToVideoWithAudio)) {
			mPhotoPaths = intent.getStringArrayListExtra(PHOTO_PATHS);
			mAudioPath = intent.getStringExtra(AUDIO_PATH);
			mOutputVideoPath = intent.getStringExtra(OUTPUT_VIDEO_PATH);
			photosToVideoWithAudio(mPhotoPaths, mAudioPath, mOutputVideoPath);

		} else if (intent.getAction().equals(FFMpegService.ActionConcatVideos)) {
			mVideoPaths = intent.getStringArrayListExtra(VIDEO_PATHS);
			mOutputVideoPath = intent.getStringExtra(OUTPUT_VIDEO_PATH);
			concatVideos(mVideoPaths, mOutputVideoPath);

		} else if (intent.getAction().equals(
				FFMpegService.ActionAudioWithDefaultPhotos)) {
			mDefaultPhotoPaths = intent
					.getStringArrayListExtra(DEFAULT_PHOTO_PATHS);
			mAudioPath = intent.getStringExtra(AUDIO_PATH);
			mOutputVideoPath = intent.getStringExtra(OUTPUT_VIDEO_PATH);
			audioToVideo(mAudioPath, mDefaultPhotoPaths, mOutputVideoPath);

		}
		else if(intent.getAction().equals(FFMpegService.ActionConvertWavToMp3)){
			mAudioPath = intent.getStringExtra(AUDIO_PATH);
			mOutputMp3Path = intent.getStringExtra(OUTPUT_MP3_PATH);
			convertWavToMp3(mAudioPath, mOutputMp3Path);
		}
		else if(intent.getAction().equals(FFMpegService.ActionRotatePhoto)){
			mPhotoPath = intent.getStringExtra(PHOTO_PATH);
			mOutputPhotoPath = intent.getStringExtra(OUTPUT_PHOTO_PATH);
			rotateImage(mPhotoPath, mOutputPhotoPath);
		}

	}
}
