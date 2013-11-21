package com.agreeya.memoir.services;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.agreeya.memoir.util.AudioChunk;
import com.agreeya.memoir.util.Complex;
import com.agreeya.memoir.util.FFT;
import com.agreeya.memoir.util.WavAudioFormat;
import com.agreeya.memoir.util.WavFileWriter;

//TODO  : Convert from byte to short for read data from AudioRecorder

public class AudioRecorder extends Service {

	private Intent mIntent;
	private Intent mIntentFFmpeg;
	private String AUDIO_PATH = "audio_path";
	private String OUTPUT_MP3_PATH = "output_mp3_path";
	private ScheduledThreadPoolExecutor mPool = null;
	private static int RECORDER_SAMPLERATE = 8000;
	private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static AudioRecord mAudioRecorder = null;
	private static int mBufferSizeInBytes = 0;
	private static int mReadSizeInShort = 1024;
//	private static int mAudioNumber = 1;
	private String LOCATION = "location";
	private String TYPE = "type";
	private String TIME = "time";
	// private ConcurrentLinkedQueue<AudioChunk> ZeroCrossingQueue = null;
	private ArrayDeque<AudioChunk> HumanFrequencyFFTQueue = null;
	private ArrayDeque<Double> SpeechDetectorQueue = null;
	private ArrayDeque<AudioChunk> RecordingBufferQueue = null;
	// private ConcurrentLinkedQueue<AudioChunk> HumanFrequencyFFTQueue = null;

	private AudioChunkReaderRunnable mAudioChunkReaderRunnable = null;
	// private ZeroCrossingRunnable mZeroCrossingRunnable = null;
	private HumanFrequencyFFTRunnable mHumanFrequencyFFTRunnable1 = null;
	private HumanFrequencyFFTRunnable mHumanFrequencyFFTRunnable2 = null;
	private HumanFrequencyFFTRunnable mHumanFrequencyFFTRunnable3 = null;
	// private HumanFrequencyFFTRunnable mHumanFrequencyFFTRunnable4 = null;
	private SpeechDetectorRunnable mSpeechDetectorRunnable = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		mPool = new ScheduledThreadPoolExecutor(5);
		// ZeroCrossingQueue = new ConcurrentLinkedQueue<AudioChunk>();
		HumanFrequencyFFTQueue = new ArrayDeque<AudioChunk>();
		SpeechDetectorQueue = new ArrayDeque<Double>();
		RecordingBufferQueue = new ArrayDeque<AudioChunk>();

		// Get the minimum buffer size required for the successful creation of
		// an AudioRecord object.
		mBufferSizeInBytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

		// Initialize Audio Recorder.
		mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, 10 * mBufferSizeInBytes);

		Log.d("asd", "mBufferSizeInBytes = " + mBufferSizeInBytes);

		mAudioChunkReaderRunnable = new AudioChunkReaderRunnable();
		// mZeroCrossingRunnable = new ZeroCrossingRunnable();
		mHumanFrequencyFFTRunnable1 = new HumanFrequencyFFTRunnable();
		mHumanFrequencyFFTRunnable2 = new HumanFrequencyFFTRunnable();
		mHumanFrequencyFFTRunnable3 = new HumanFrequencyFFTRunnable();
		// mHumanFrequencyFFTRunnable4 = new HumanFrequencyFFTRunnable();
		mSpeechDetectorRunnable = new SpeechDetectorRunnable();

		// Start Recording.
		mAudioRecorder.startRecording();
		mPool.scheduleAtFixedRate(mAudioChunkReaderRunnable, 0, 10,
				TimeUnit.MILLISECONDS);
		// mPool.scheduleAtFixedRate(mZeroCrossingRunnable, 0, 20,
		// TimeUnit.MILLISECONDS);
		mPool.scheduleAtFixedRate(mHumanFrequencyFFTRunnable1, 40, 10,
				TimeUnit.MILLISECONDS);
		mPool.scheduleAtFixedRate(mHumanFrequencyFFTRunnable2, 40, 10,
				TimeUnit.MILLISECONDS);
		mPool.scheduleAtFixedRate(mHumanFrequencyFFTRunnable3, 40, 10,
				TimeUnit.MILLISECONDS);
		/*mPool.scheduleAtFixedRate(mHumanFrequencyFFTRunnable4, 40, 10,
				TimeUnit.MILLISECONDS);*/
		mPool.scheduleAtFixedRate(mSpeechDetectorRunnable, 40, 10,
				TimeUnit.MILLISECONDS);

		mIntent = new Intent(this.getApplicationContext(), InsertIntoDB.class);
		mIntentFFmpeg = new Intent(this.getApplicationContext(), FFMpegService.class);
		Toast.makeText(this.getApplicationContext(), "Audio Recorder", Toast.LENGTH_LONG).show();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mAudioRecorder.release();
		mPool.shutdown();
		super.onDestroy();
	}
	private class AudioChunkReaderRunnable implements Runnable {
		@SuppressLint("NewApi")
		@Override
		public void run() {

			AudioChunk chunk = new AudioChunk(mReadSizeInShort);
			chunk.length = mAudioRecorder.read(chunk.data, 0, mReadSizeInShort);
			if (chunk.length == mReadSizeInShort) {
				// ZeroCrossingQueue.add(chunk);
				HumanFrequencyFFTQueue.add(chunk);
				RecordingBufferQueue.add(chunk);
			}
		}
	};

	private class SpeechDetectorRunnable implements Runnable {
		private int LOOKING_FOR_START = 0;
		private int RECORDING = 1;
		private int LOOKING_FOR_STOP = 2;

		private double[] SOASignals = new double[5];
		private long startTime = 0;
		private int status = LOOKING_FOR_START;
		private boolean flag = false;
		private String mAudioPath;
		private String mAudioMp3Path;

		public boolean StatusOfSpeech(double currentSOAS) {
			double SOfSOAS = 0;
			for (int i = 4; i > 0; i--) {
				SOASignals[i] = SOASignals[i - 1];
				SOfSOAS = SOfSOAS + SOASignals[i];
			}
			SOfSOAS = SOfSOAS + currentSOAS;
			if (SOfSOAS >= 70)
				return true;
			return false;
		}

		@SuppressLint({ "NewApi", "SimpleDateFormat" })
		public void saveRecording() {
			try {
				Log.d("asd", "Writing to file");
			
				Time t = new Time(System.currentTimeMillis());
				double dTime = t.getTime();
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
				
				File audioFile = new File(Environment.getExternalStorageDirectory()
						.getPath(),"MemoirRepo");
				if (!audioFile.exists()) {
					audioFile.mkdir();
				}

				audioFile = new File(Environment.getExternalStorageDirectory()
						.getPath() + "/MemoirRepo","audios");
				if (!audioFile.exists()) {
					audioFile.mkdir();
				}
				
				mAudioPath = audioFile + "/audio_" + timeStamp + ".wav";
				mAudioMp3Path = audioFile + "/audio_" + timeStamp + ".mp3";
				
				File f = new File(mAudioPath);
				f.createNewFile();
				WavAudioFormat waf = new WavAudioFormat(8000, 16, 2, true);
				@SuppressWarnings("resource")
				WavFileWriter WavWriter = new WavFileWriter(waf, f);
				int size = RecordingBufferQueue.size();
				Log.d("asd", "RecordingBufferQueue size =" + size);
				for (int i = 0; i < size; i++) {
					AudioChunk chunk = RecordingBufferQueue.poll();
					WavWriter.write(chunk.data);
				}
				Log.d("asd", "File Saved at " + f.getAbsolutePath());
				
				// *********converting audio wav to audio mp3**********
				mIntentFFmpeg.setAction(FFMpegService.ActionConvertWavToMp3);
				mIntentFFmpeg.putExtra(AUDIO_PATH, mAudioPath);
				mIntentFFmpeg.putExtra(OUTPUT_MP3_PATH,mAudioMp3Path);
				startService(mIntentFFmpeg);
				
				//**********storing details in database**************//
				
				mIntent.putExtra(TYPE, "audio");
				mIntent.putExtra(LOCATION, mAudioMp3Path);
				mIntent.putExtra(TIME, dTime);
				Log.v("asd", "starting db service");
				startService(mIntent);			
				
				stopSelf();
			} catch (IOException e) {
				Log.e("asd", "Caught in exception");
				e.printStackTrace();
			}

		}

		@SuppressLint("NewApi")
		@Override
		public void run() {

			Double d = SpeechDetectorQueue.poll();
			if (d != null) {
				double sumOfAbsSignal = d.doubleValue();
				if (status == LOOKING_FOR_START) {
					if (StatusOfSpeech(sumOfAbsSignal)) {
						Log.d("asd", "Start Recording");
						status = RECORDING;
						flag = true;
						startTime = System.currentTimeMillis() / 1000;
					}
				} else if (status == RECORDING) {
					if (StatusOfSpeech(sumOfAbsSignal)) {
						startTime = System.currentTimeMillis() / 1000;
						Log.d("asd", "Start Time " + startTime);
					} else {
						if ((System.currentTimeMillis() / 1000 - startTime) > 5) {
							status = LOOKING_FOR_STOP;
						}
					}
				} else if (status == LOOKING_FOR_STOP) {
					if (!StatusOfSpeech(sumOfAbsSignal)) {
						Log.d("asd", "Stop Recording");
						status = LOOKING_FOR_START;
						flag = false;
						saveRecording();
						RecordingBufferQueue.clear();
					}
				}
				if (flag) {
					// MainActivity.mainHandler.obtainMessage(1,
					// (int) sumOfAbsSignal, 0).sendToTarget();
				} else {
					// MainActivity.mainHandler.obtainMessage(1, 0, 0)
					// .sendToTarget();
					RecordingBufferQueue.poll();
				}
			}
		}
	};

	private class HumanFrequencyFFTRunnable implements Runnable {

		private Complex[] ComplexSignal = null;

		public HumanFrequencyFFTRunnable() {
			ComplexSignal = new Complex[mReadSizeInShort];
		}

		@SuppressLint("NewApi")
		@Override
		public void run() {

			AudioChunk chunk = HumanFrequencyFFTQueue.poll();
			if (chunk != null) {
				for (int i = 0; i < chunk.length; i++) {
					ComplexSignal[i] = new Complex(chunk.data[i], 0.0);
				}

				Complex[] fftSpectrum = FFT.fft(ComplexSignal);

				int bitsPer = RECORDER_SAMPLERATE / (fftSpectrum.length);
				double sumOfAbsSignal = 0;
				int i = 0;
				// Normal Human speech range 85-180 Hz: male and 165-255 Female
				for (i = (int) Math.floor(50 / bitsPer); i < Math
						.ceil(300 / bitsPer); i++) {

					sumOfAbsSignal = sumOfAbsSignal + fftSpectrum[i].abs();
				}
				SpeechDetectorQueue.add(Double.valueOf(sumOfAbsSignal / i
						/ 1000));
				// Log.d("asd", "FFT : " + sumOfAbsSignal / i);
			}
		}
	};
}