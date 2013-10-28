package com.agreeya.audiodetectionapp;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

//TODO  : Convert from byte to short for read data from AudioRecorder

public class AudioIntelligenceService extends Service {

	private ScheduledThreadPoolExecutor mPool = null;
	private static int RECORDER_SAMPLERATE = 48000;
	private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static AudioRecord mAudioRecorder = null;
	private static int mBufferSizeInBytes = 0;
	private static int mReadSizeInShort = 1024;

	private ConcurrentLinkedQueue<AudioChunk> ZeroCrossingQueue = null;
	private ArrayDeque<AudioChunk> HumanFrequencyFFTQueue = null;
	// private ConcurrentLinkedQueue<AudioChunk> HumanFrequencyFFTQueue = null;

	private AudioChunkReaderRunnable mAudioChunkReaderRunnable = null;
	// private ZeroCrossingRunnable mZeroCrossingRunnable = null;
	private HumanFrequencyFFTRunnable mHumanFrequencyFFTRunnable = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		mPool = new ScheduledThreadPoolExecutor(3);
		ZeroCrossingQueue = new ConcurrentLinkedQueue<AudioChunk>();
		HumanFrequencyFFTQueue = new ArrayDeque<AudioChunk>();

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
		mHumanFrequencyFFTRunnable = new HumanFrequencyFFTRunnable();

		// Start Recording.
		mAudioRecorder.startRecording();
		mPool.scheduleAtFixedRate(mAudioChunkReaderRunnable, 0, 10,
				TimeUnit.MILLISECONDS);
		// mPool.scheduleAtFixedRate(mZeroCrossingRunnable, 0, 20,
		// TimeUnit.MILLISECONDS);
		mPool.scheduleAtFixedRate(mHumanFrequencyFFTRunnable, 0, 10,
				TimeUnit.MILLISECONDS);

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		mAudioRecorder.release();
		mPool.shutdown();
		super.onDestroy();
	}

	private class AudioChunkReaderRunnable implements Runnable {
		@Override
		public void run() {

			AudioChunk chunk = new AudioChunk(mReadSizeInShort);
			chunk.length = mAudioRecorder.read(chunk.data, 0, mReadSizeInShort);
			if (chunk.length == mReadSizeInShort) {
				// ZeroCrossingQueue.add(chunk);
				HumanFrequencyFFTQueue.add(chunk);
			}
		}
	};

	/*	private class ZeroCrossingRunnable implements Runnable {
			int crossingValueSetCounter = 0;
			int standardCrossingValue = 0;

			@Override
			public void run() {

				short sample1 = 0;
				short sample2 = 0;

				AudioChunk chunk = ZeroCrossingQueue.poll();
				if (chunk != null) {
					int zeroCrossing = 0;
					for (int i = 2; i < chunk.length; i = i + 2) {
						sample1 = (short) ((chunk.data[i]) | chunk.data[i + 1] << 8);
						sample2 = (short) ((chunk.data[i - 2]) | chunk.data[i - 1] << 8);
						if (sample1 * sample2 < 0) {
							zeroCrossing++;
						}
					}
					if (crossingValueSetCounter < 100) {
						standardCrossingValue = (standardCrossingValue + Math
								.abs(zeroCrossing)) / 2;
						crossingValueSetCounter++;
					}
					// Log.d("asd", "ZeroCrossing : "
					// + (Math.abs(zeroCrossing) - standardCrossingValue));
				}
			}
		};*/

	private class HumanFrequencyFFTRunnable implements Runnable {

		private Complex[] ComplexSignal = null;

		public HumanFrequencyFFTRunnable() {
			ComplexSignal = new Complex[mReadSizeInShort];
		}

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
				//Log.d("asd", "FFT : " + sumOfAbsSignal / i);
				MainActivity.mainHandler.obtainMessage(1, (int) (sumOfAbsSignal / i) / 1000,
						0).sendToTarget();
			}
		}
	};
}
