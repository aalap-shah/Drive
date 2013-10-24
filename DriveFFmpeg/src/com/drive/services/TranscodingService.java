package com.drive.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class TranscodingService extends IntentService {

	public TranscodingService() {
		super("TranscodingService");
	}

	public String extStorePath;
	private String mFfmpegPath = null;
	private String mSilencePath = null;
	
	
	public static String ActionConvertMp4ToTs = "ActionConvertMp4ToTs";
	public static String ActionConvertMp4ToTsUpdate = "ActionConvertMp4ToTsUpdate";
	public static String ActionCreateMyLife = "ActionCreateMyLife";
	public static String ActionTrimVideo = "ActionTrimVideo";
	public static String ActionTrimVideoUpdate = "ActionTrimVideoUpdate";
	public static String ActionCreateMp4Video = "ActionCreateMp4Video";
	public static String ActionCreateMp4VideoUpdate = "ActionCreateMp4VideoUpdate";
	public static String ActionConvertTsVideo = "ActionConvertTsVideo";

	// h264_mp4toannexb encoder/decoder
	private String mF_VIDEO_CODEC_MP4_ANNEXB = " -vbsf h264_mp4toannexb ";
	// video resolution 1920:1080 should be changed to max value of camera
	private String mF_VIDEO_FILTER_SCALE = " -vf scale=1920:1080 ";
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
	// Video codec 2 - used for image translation
	private String mF_VIDEO_CODEC_LIBX264 = " -c:v libx264 ";
	// Video Frame Size
	private String mF_VIDEO_FRAME_SIZE = " -s 1920x1080 ";
	// Video PORTRAIT Frame Size
	private String mF_VIDEO_PORTRAIT_FRAME_SIZE = " -s 1080x1920 ";
	// Time for Video-Should be redefined as selected no of seconds.
	private String mF_VIDEO_TIME_SEC = " -t ";
	// Video codec copy
	private String mF_VIDEO_CODEC_COPY = " -vcodec copy ";
	// Audio codec copy
	private String mF_AUDIO_CODEC_COPY = " -acodec copy ";
	// Start Time for trimming a video
	private String mF_TRIMMING_START_TIME = " -ss ";
	// End time for trimming a video
	private String mF_TRIMMING_END_TIME = " -t ";
	// -vf transpose= - for rotation
	private String mF_VIDEO_TRANSPOSE_0 = "-vf transpose=0 ";
	private String mF_VIDEO_TRANSPOSE_1 = "-vf transpose=1 ";
	private String mF_VIDEO_TRANSPOSE_2 = "-vf transpose=2 ";
	private String mF_VIDEO_TRANSPOSE_3 = "-vf transpose=3 ";
	// -vf setdar=1:1
	private String mF_SET_SAR_1_1 = " -vf setsar=1:1 ";
	// -vf setdar=16:9
	private String mF_SET_DAR_16_9 = " -vf setdar=16:9 ";
	// -shortest
	private String mF_SHORTEST = " -shortest ";

	private SharedPreferences mPrefs = null;
	private LocalBroadcastManager mBroadcastMngr = null;

	private interface iOutput {
		void onOutput(String output);
	}

	private interface iError {
		void onError(String error);
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
			//BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			//		process.getOutputStream()));
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
	public void onCreate() {
		super.onCreate();

		mPrefs = this.getSharedPreferences("com.krystal.memoir",
				Context.MODE_PRIVATE);

		int height = mPrefs.getInt("com.krystal.memoir.standardheight", 1080);
		int width = mPrefs.getInt("com.krystal.memoir.standardwidth", 1920);
		mF_VIDEO_FRAME_SIZE = " -s " + width + "x" + height + " ";
		mF_VIDEO_PORTRAIT_FRAME_SIZE = " -s " + height + "x" + width + " ";
		mF_VIDEO_FILTER_SCALE = " -vf scale=" + width + ":" + height + " ";

		mBroadcastMngr = LocalBroadcastManager.getInstance(this);
		validateFFmpeg();
		validateSilenceFile();
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

	public void createMp4Video(Intent intent) {
		String mimeType = intent.getStringExtra("mimeType");
		String path = intent.getStringExtra("path");
		String mp4Path = intent.getStringExtra("mp4Path");

		boolean isFileNameSame = false;

		if (path.equals(mp4Path)) {
			File f = new File(path);
			File f2 = new File(path + ".stmp" + mimeType);
			f.renameTo(f2);
			path = f2.getAbsolutePath();
			f.delete();
			isFileNameSame = true;

			if (f2.exists()) {
				Log.d("asd", "f2 file exists properly" + f2.getAbsolutePath());
			}
			if (f.exists()) {
				Log.d("asd", "f file exists wrongly " + f.getAbsolutePath());
			}
		}

		final Intent updateIntent = new Intent(
				TranscodingService.ActionCreateMp4VideoUpdate);

		Log.d("asd", "Converting " + path + " to its new mp4 file " + mp4Path
				+ " mime type =" + mimeType);
		if (mimeType.equalsIgnoreCase(".jpeg")
				|| mimeType.equalsIgnoreCase(".jpg")
				|| mimeType.equalsIgnoreCase(".png")) {

			int seconds = mPrefs.getInt("com.krystal.memoir.noofseconds", 1);
			String interMp4Path = mp4Path + ".tmp.mp4";
			// ./ffmpeg -loop 1 -f image2 -i 1.png -c:v libx264 -s 1920x1080 -vf
			// setsar=1:1 -vf setdar=16:9 -r 30 -strict experimental -t 2
			// 1.png.mp4
			updateIntent.putExtra("TAG", "START");
			updateIntent.putExtra("PASS", 2);
			updateIntent.putExtra("EACH-PASS", 30 * seconds);
			mBroadcastMngr.sendBroadcast(updateIntent);

			runCommand(mFfmpegPath + mF_IMAGE_LOOP + mF_IMAGE_FORMAT_IMAGE2
					+ " -i " + path + mF_VIDEO_CODEC_LIBX264
					+ mF_VIDEO_FRAME_SIZE + mF_SET_SAR_1_1 + mF_SET_DAR_16_9
					+ mF_VIDEO_FRAME_RATE_30 + mF_STRICT_EXPERIMENTAL
					+ mF_VIDEO_TIME_SEC + seconds + " " + interMp4Path, null,
					new iError() {

						@Override
						public void onError(String error) {
							int frame = ffmpegUpdateParser(error);
							if (frame != -1) {
								updateIntent.putExtra("TAG", "UPDATE");
								updateIntent.putExtra("PASS", 1);
								updateIntent.putExtra("UPDATE", frame);
								Log.d("asd", "Frames=" + frame);
								mBroadcastMngr.sendBroadcast(updateIntent);
							}
						}
					});

			// ./ffmpeg -i silence.mp2 -i 1.png.mp4 -c:a aac -ar 48000 -b:a 160k
			// -ac 2 -c:v libx264 -shortest -c:v copy -strict experimental
			// 1.new.png.mp4
			runCommand(mFfmpegPath + " -i " + mSilencePath + " -i "
					+ interMp4Path + mF_AUDIO_CODEC_AAC
					+ mF_AUDIO_FREQUENCY_48K + mF_AUDIO_BIT_RATE_160K
					+ mF_AUDIO_CHANNEL_2 + mF_VIDEO_CODEC_LIBX264 + mF_SHORTEST
					+ mF_VIDEO_CODEC_COPY + mF_STRICT_EXPERIMENTAL + mp4Path,
					null, new iError() {

						@Override
						public void onError(String error) {
							int frame = ffmpegUpdateParser(error);
							if (frame != -1) {
								updateIntent.putExtra("TAG", "UPDATE");
								updateIntent.putExtra("PASS", 2);
								updateIntent.putExtra("UPDATE", frame);
								Log.d("asd", "Frames=" + frame);
								mBroadcastMngr.sendBroadcast(updateIntent);
							}
						}
					});
			File file = new File(interMp4Path + ".tmp.mp4");
			file.delete();
			updateIntent.putExtra("TAG", "END");
			mBroadcastMngr.sendBroadcast(updateIntent);

		} else if (mimeType.equalsIgnoreCase(".mp4")
				|| mimeType.equalsIgnoreCase(".3gp")) {
			int rotationAngle = intent.getIntExtra("videoRotation", 0);
			double seconds = intent.getDoubleExtra("time",
					mPrefs.getInt("com.krystal.memoir.noofseconds", 1));
			Log.d("asd", "No Of seconds" + seconds);
			if (rotationAngle == 0) {
				updateIntent.putExtra("TAG", "START");
				updateIntent.putExtra("PASS", 1);
				Log.d("asd", "Each pass " + Math.ceil(30 * seconds));
				updateIntent.putExtra("EACH-PASS",
						(int) (Math.ceil(30 * seconds)));
				mBroadcastMngr.sendBroadcast(updateIntent);
				runCommand(mFfmpegPath + " -i " + path + mF_VIDEO_CODEC_LIBX264
						+ mF_VIDEO_FRAME_SIZE + mF_SET_SAR_1_1
						+ mF_SET_DAR_16_9 + mF_VIDEO_FRAME_RATE_30
						+ mF_AUDIO_CODEC_AAC + mF_AUDIO_FREQUENCY_48K
						+ mF_AUDIO_BIT_RATE_160K + mF_AUDIO_CHANNEL_2
						+ mF_STRICT_EXPERIMENTAL + mp4Path, new iOutput() {

					@Override
					public void onOutput(String output) {
						Log.d("asd", "Output :" + output);
					}

				}, new iError() {

					@Override
					public void onError(String error) {
						Log.d("asd", error);
						int frame = ffmpegUpdateParser(error);
						if (frame != -1) {
							updateIntent.putExtra("TAG", "UPDATE");
							updateIntent.putExtra("PASS", 1);
							updateIntent.putExtra("UPDATE", frame);
							Log.d("asd", "Frames=" + frame);
							mBroadcastMngr.sendBroadcast(updateIntent);
						}
					}
				});
				updateIntent.putExtra("TAG", "END");
				mBroadcastMngr.sendBroadcast(updateIntent);
			} else if (rotationAngle == 90) {
				updateIntent.putExtra("TAG", "START");
				updateIntent.putExtra("PASS", 2);
				updateIntent.putExtra("EACH-PASS",
						(int) (Math.ceil(30 * seconds)));
				mBroadcastMngr.sendBroadcast(updateIntent);
				// ./ffmpeg -i 5.mp4 -c:v libx264 -s 1080x1920 -vf transpose=0
				// -r 30 -c:a aac -ar 48000 -b:a 160k -ac 2 -strict experimental
				// 5.new.mp4
				runCommand(mFfmpegPath + " -i " + path + mF_VIDEO_CODEC_LIBX264
						+ mF_VIDEO_PORTRAIT_FRAME_SIZE + mF_VIDEO_TRANSPOSE_1
						+ mF_VIDEO_FRAME_RATE_30 + mF_AUDIO_CODEC_AAC
						+ mF_AUDIO_FREQUENCY_48K + mF_AUDIO_BIT_RATE_160K
						+ mF_AUDIO_CHANNEL_2 + mF_STRICT_EXPERIMENTAL + mp4Path
						+ ".tmp.mp4", null, new iError() {

					@Override
					public void onError(String error) {
						Log.d("asd", error);
						int frame = ffmpegUpdateParser(error);
						if (frame != -1) {
							updateIntent.putExtra("TAG", "UPDATE");
							updateIntent.putExtra("PASS", 1);
							updateIntent.putExtra("UPDATE", frame);
							Log.d("asd", "Frames=" + frame);
							mBroadcastMngr.sendBroadcast(updateIntent);
						}
					}
				});
				// ./ffmpeg -i 5.new.mp4 -c:v libx264 -vf setsar=1:1 -vf
				// setdar=16:9 -acodec copy 5.new4.mp4
				runCommand(mFfmpegPath + " -i " + mp4Path + ".tmp.mp4"
						+ mF_VIDEO_CODEC_LIBX264 + mF_SET_SAR_1_1
						+ mF_SET_DAR_16_9 + mF_AUDIO_CODEC_COPY + mp4Path,
						null, new iError() {

							@Override
							public void onError(String error) {
								Log.d("asd", error);
								int frame = ffmpegUpdateParser(error);
								if (frame != -1) {
									updateIntent.putExtra("TAG", "UPDATE");
									updateIntent.putExtra("PASS", 2);
									updateIntent.putExtra("UPDATE", frame);
									Log.d("asd", "Frames=" + frame);
									mBroadcastMngr.sendBroadcast(updateIntent);
								}
							}
						});
				File file = new File(mp4Path + ".tmp.mp4");
				file.delete();
				updateIntent.putExtra("TAG", "END");
				mBroadcastMngr.sendBroadcast(updateIntent);

			} else if (rotationAngle == 180) {
				updateIntent.putExtra("TAG", "START");
				updateIntent.putExtra("PASS", 2);
				updateIntent.putExtra("EACH-PASS",
						(int) (Math.ceil(30 * seconds)));
				mBroadcastMngr.sendBroadcast(updateIntent);

				// ./ffmpeg -i 5.mp4 -c:v libx264 -s 1080x1920 -vf transpose=0
				// -r 30 -c:a aac -ar 48000 -b:a 160k -ac 2 -strict experimental
				// 5.new.mp4
				runCommand(mFfmpegPath + " -i " + path + mF_VIDEO_CODEC_LIBX264
						+ mF_VIDEO_PORTRAIT_FRAME_SIZE + mF_VIDEO_TRANSPOSE_2
						+ mF_VIDEO_TRANSPOSE_2 + mF_VIDEO_FRAME_RATE_30
						+ mF_AUDIO_CODEC_AAC + mF_AUDIO_FREQUENCY_48K
						+ mF_AUDIO_BIT_RATE_160K + mF_AUDIO_CHANNEL_2
						+ mF_STRICT_EXPERIMENTAL + mp4Path + ".tmp.mp4", null,
						new iError() {

							@Override
							public void onError(String error) {
								int frame = ffmpegUpdateParser(error);
								if (frame != -1) {
									updateIntent.putExtra("TAG", "UPDATE");
									updateIntent.putExtra("PASS", 1);
									updateIntent.putExtra("UPDATE", frame);
									Log.d("asd", "Frames=" + frame);
									mBroadcastMngr.sendBroadcast(updateIntent);
								}
							}
						});
				// ./ffmpeg -i 5.new.mp4 -c:v libx264 -vf setsar=1:1 -vf
				// setdar=16:9 -acodec copy 5.new4.mp4
				runCommand(mFfmpegPath + " -i " + mp4Path + ".tmp.mp4"
						+ mF_VIDEO_CODEC_LIBX264 + mF_SET_SAR_1_1
						+ mF_SET_DAR_16_9 + mF_AUDIO_CODEC_COPY + mp4Path,
						null, new iError() {

							@Override
							public void onError(String error) {
								int frame = ffmpegUpdateParser(error);
								if (frame != -1) {
									updateIntent.putExtra("TAG", "UPDATE");
									updateIntent.putExtra("PASS", 2);
									updateIntent.putExtra("UPDATE", frame);
									Log.d("asd", "Frames=" + frame);
									mBroadcastMngr.sendBroadcast(updateIntent);
								}
							}
						});
				File file = new File(mp4Path + ".tmp.mp4");
				file.delete();
				updateIntent.putExtra("TAG", "END");
				mBroadcastMngr.sendBroadcast(updateIntent);

			} else if (rotationAngle == 270) {
				updateIntent.putExtra("TAG", "START");
				updateIntent.putExtra("PASS", 2);
				updateIntent.putExtra("EACH-PASS",
						(int) (Math.ceil(30 * seconds)));
				mBroadcastMngr.sendBroadcast(updateIntent);
				// ./ffmpeg -i 5.mp4 -c:v libx264 -s 1080x1920 -vf transpose=0
				// -r 30 -c:a aac -ar 48000 -b:a 160k -ac 2 -strict experimental
				// 5.new.mp4
				runCommand(mFfmpegPath + " -i " + path + mF_VIDEO_CODEC_LIBX264
						+ mF_VIDEO_PORTRAIT_FRAME_SIZE + mF_VIDEO_TRANSPOSE_2
						+ mF_VIDEO_FRAME_RATE_30 + mF_AUDIO_CODEC_AAC
						+ mF_AUDIO_FREQUENCY_48K + mF_AUDIO_BIT_RATE_160K
						+ mF_AUDIO_CHANNEL_2 + mF_STRICT_EXPERIMENTAL + mp4Path
						+ ".tmp.mp4", null, new iError() {

					@Override
					public void onError(String error) {
						int frame = ffmpegUpdateParser(error);
						if (frame != -1) {
							updateIntent.putExtra("TAG", "UPDATE");
							updateIntent.putExtra("PASS", 1);
							updateIntent.putExtra("UPDATE", frame);
							Log.d("asd", "Frames=" + frame);
							mBroadcastMngr.sendBroadcast(updateIntent);
						}
					}
				});
				// ./ffmpeg -i 5.new.mp4 -c:v libx264 -vf setsar=1:1 -vf
				// setdar=16:9 -acodec copy 5.new4.mp4
				runCommand(mFfmpegPath + " -i " + mp4Path + ".tmp.mp4"
						+ mF_VIDEO_CODEC_LIBX264 + mF_SET_SAR_1_1
						+ mF_SET_DAR_16_9 + mF_AUDIO_CODEC_COPY + mp4Path,
						null, new iError() {

							@Override
							public void onError(String error) {
								int frame = ffmpegUpdateParser(error);
								if (frame != -1) {
									updateIntent.putExtra("TAG", "UPDATE");
									updateIntent.putExtra("PASS", 2);
									updateIntent.putExtra("UPDATE", frame);
									Log.d("asd", "Frames=" + frame);
									mBroadcastMngr.sendBroadcast(updateIntent);
								}
							}
						});
				File file = new File(mp4Path + ".tmp.mp4");
				file.delete();
				updateIntent.putExtra("TAG", "END");
				mBroadcastMngr.sendBroadcast(updateIntent);
			}
		}

		if (isFileNameSame) {
			// new File(path).delete();
		}

		Intent broadcastIntent = new Intent(ActionCreateMp4Video);
		broadcastIntent.putExtra("path", path);
		broadcastIntent.putExtra("mp4Path", mp4Path);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		Log.d("asd",
				"Sending the broadcast from service for DONE ActionCreateMP4Video");
	}

	public void convertTsVideo(Intent intent) {
		String mimeType = intent.getStringExtra("mimeType");
		String path = intent.getStringExtra("path");
		String tsPath = intent.getStringExtra("tsPath");
		Log.d("asd", "Converting " + path + " to its ts file " + tsPath
				+ " mime type =" + mimeType);
		if (mimeType.equalsIgnoreCase("jpeg")
				|| mimeType.equalsIgnoreCase("jpg")) {

			String imageToMp4Path = path + ".mp4";
			runCommand(mFfmpegPath + mF_IMAGE_LOOP + mF_IMAGE_FORMAT_IMAGE2
					+ path + mF_VIDEO_CODEC_LIBX264 + mF_VIDEO_FRAME_SIZE
					+ mF_VIDEO_TIME_SEC + imageToMp4Path, null, new iError() {

				@Override
				public void onError(String error) {
					Log.d("asd", error);
				}
			});

			runCommand(mFfmpegPath + " -i " + imageToMp4Path
					+ mF_VIDEO_CODEC_MP4_ANNEXB + mF_VIDEO_FRAME_SIZE
					+ mF_VIDEO_FRAME_RATE_30 + mF_AUDIO_CODEC_AAC
					+ mF_AUDIO_FREQUENCY_48K + mF_AUDIO_BIT_RATE_160K
					+ mF_STRICT_EXPERIMENTAL + mF_VIDEO_FORMAT_MPEGTS + tsPath,
					null, new iError() {

						@Override
						public void onError(String error) {
							Log.d("asd", error);
						}
					});

		} else if (mimeType.equalsIgnoreCase("mp4")
				|| mimeType.equalsIgnoreCase("3gp")) {
			int rotationAngle = intent.getIntExtra("videoRotation", 0);

			if (rotationAngle == 0) {
				runCommand(mFfmpegPath + " -i " + path
						+ mF_VIDEO_CODEC_MP4_ANNEXB + mF_VIDEO_FILTER_SCALE
						+ mF_VIDEO_FRAME_RATE_30 + mF_AUDIO_CODEC_AAC
						+ mF_AUDIO_FREQUENCY_48K + mF_AUDIO_BIT_RATE_160K
						+ mF_STRICT_EXPERIMENTAL + mF_VIDEO_FORMAT_MPEGTS
						+ tsPath, null, new iError() {

					@Override
					public void onError(String error) {
						Log.d("asd", error);
					}
				});
			} else if (rotationAngle == 90) {
				runCommand(mFfmpegPath + " -i " + path
						+ mF_VIDEO_CODEC_MP4_ANNEXB + mF_VIDEO_TRANSPOSE_2
						+ mF_VIDEO_FRAME_SIZE + mF_VIDEO_FRAME_RATE_30
						+ mF_AUDIO_CODEC_AAC + mF_AUDIO_FREQUENCY_48K
						+ mF_AUDIO_BIT_RATE_160K + mF_STRICT_EXPERIMENTAL
						+ mF_VIDEO_FORMAT_MPEGTS + tsPath, null, new iError() {

					@Override
					public void onError(String error) {
						Log.d("asd", error);
					}
				});
			} else if (rotationAngle == 180) {

			} else if (rotationAngle == 270) {

			}
		}
		Intent broadcastIntent = new Intent(ActionConvertTsVideo);
		broadcastIntent.putExtra("path", path);
		broadcastIntent.putExtra("tsPath", tsPath);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}

	public void trimVideo(Intent intent) {
		final Intent updateIntent = new Intent(
				TranscodingService.ActionTrimVideoUpdate);

		String path = intent.getStringExtra("filePath");
		String outPath = intent.getStringExtra("outputFilePath");
		double startTimeD = intent.getDoubleExtra("startTime", 0);
		double endTimeD = intent.getDoubleExtra("endTime", 1);
		Log.d("asd", "startTimeD=" + startTimeD + " endTimeD=" + endTimeD);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS",
				Locale.getDefault());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		String startTime = sdf.format(new Date((long) (startTimeD * 1000)));
		String endTime = sdf.format(new Date(
				(long) ((endTimeD - startTimeD) * 1000)));
		Log.d("asd", "startTime=" + startTime + "endTime=" + endTime);

		updateIntent.putExtra("TAG", "START");
		updateIntent.putExtra("PASS", 1);
		updateIntent
				.putExtra("EACH-PASS", (int) (30 * (endTimeD - startTimeD)));
		mBroadcastMngr.sendBroadcast(updateIntent);

		runCommand(mFfmpegPath + " -i " + path + mF_VIDEO_CODEC_COPY
				+ mF_AUDIO_CODEC_COPY + mF_TRIMMING_START_TIME + startTime
				+ mF_TRIMMING_END_TIME + endTime + " " + outPath, null,
				new iError() {

					@Override
					public void onError(String error) {
						Log.d("asd", error);
						int frame = ffmpegUpdateParser(error);
						if (frame != -1) {
							Log.d("asd", "Frame = " + frame);
							updateIntent.putExtra("TAG", "UPDATE");
							updateIntent.putExtra("PASS", 1);
							updateIntent.putExtra("UPDATE", frame);
							mBroadcastMngr.sendBroadcast(updateIntent);
						}
					}
				});
		// ffmpeg -i input.avi -vcodec copy -acodec copy -ss 00:00:00 -t
		// 00:30:00 output1.avi
		updateIntent.putExtra("TAG", "END");
		mBroadcastMngr.sendBroadcast(updateIntent);

		Intent broadcastIntent = new Intent(
				TranscodingService.ActionTrimVideo);
		broadcastIntent.putExtra("OutputFileName", outPath);
		broadcastIntent.putExtra("time", (endTimeD - startTimeD));
		mBroadcastMngr.sendBroadcast(broadcastIntent);
	}

	public void createMyLife(Intent intent) {

	}

	public void copy(InputStream in, String dstPath) {
		try {
			OutputStream out = new FileOutputStream(new File(dstPath));

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void copy2(String srcPath, String dstPath) {
		try {
			InputStream in = new FileInputStream(new File(srcPath));
			OutputStream out = new FileOutputStream(new File(dstPath));

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (intent.getAction().equals(
				TranscodingService.ActionCreateMyLife)) {
			createMyLife(intent);
		} else if (intent.getAction().equals(
				TranscodingService.ActionTrimVideo)) {
			trimVideo(intent);
		} else if (intent.getAction().equals(
				TranscodingService.ActionCreateMp4Video)) {
			createMp4Video(intent);
		}
	}
}
