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
import android.os.Message;
import android.util.Log;

//TODO  : Convert from byte to short for read data from AudioRecorder

public class AudioIntelligenceService extends Service {

	private ScheduledThreadPoolExecutor mPool = null;
	private static int RECORDER_SAMPLERATE = 44100;
	private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static AudioRecord mAudioRecorder = null;
	private static int mBufferSizeInBytes = 0;

	private ConcurrentLinkedQueue<AudioChunk> ZeroCrossingQueue = null;
	private ArrayDeque<AudioChunk> HumanFrequencyFFTQueue = null;
	//private ConcurrentLinkedQueue<AudioChunk> HumanFrequencyFFTQueue = null;

	private AudioChunkReaderRunnable mAudioChunkReaderRunnable = null;
	private ZeroCrossingRunnable mZeroCrossingRunnable = null;
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
				RECORDER_AUDIO_ENCODING, 10*mBufferSizeInBytes);
		
		Log.d("asd", "mBufferSizeInBytes = " + mBufferSizeInBytes);
		if (mBufferSizeInBytes < 1024) {
			mBufferSizeInBytes = 1024;
		} else if (mBufferSizeInBytes < 2048) {
			mBufferSizeInBytes = 2048;
		} else /*if (mBufferSizeInBytes < 4096)*/ {
			mBufferSizeInBytes = 4096;
		/*} else {
			mBufferSizeInBytes = 8192;*/
		}

		mAudioChunkReaderRunnable = new AudioChunkReaderRunnable();
		mZeroCrossingRunnable = new ZeroCrossingRunnable();
		mHumanFrequencyFFTRunnable = new HumanFrequencyFFTRunnable();

		// Start Recording.
		mAudioRecorder.startRecording();
		mPool.scheduleAtFixedRate(mAudioChunkReaderRunnable, 0, 20,
				TimeUnit.MILLISECONDS);
		//mPool.scheduleAtFixedRate(mZeroCrossingRunnable, 0, 20,
		//		TimeUnit.MILLISECONDS);
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
			AudioChunk chunk = new AudioChunk(mBufferSizeInBytes);
			chunk.length = mAudioRecorder.read(chunk.data, 0,
					mBufferSizeInBytes);
			//ZeroCrossingQueue.add(chunk);
			HumanFrequencyFFTQueue.add(chunk);
		}
	};

	private class ZeroCrossingRunnable implements Runnable {
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
	};

	private class HumanFrequencyFFTRunnable implements Runnable {
		
		private Complex[] ComplexSignal = null;
		private double[] AbsSignal = null;
		private double mMaxFFTSample = 0.0;
		private int mPeakPos = 0;
		
		public HumanFrequencyFFTRunnable() {
			ComplexSignal = new Complex[mBufferSizeInBytes / 2];
			AbsSignal = new double[mBufferSizeInBytes / 2];
		}

		@Override
		public void run() {

			double sample = 0;

			AudioChunk chunk = HumanFrequencyFFTQueue.poll();
			if (chunk != null) {
				for (int i = 0; i < chunk.length/2; i++) {
					sample = (double) ((chunk.data[2 * i] & 0xFF) | (chunk.data[2 * i + 1] << 8));
					ComplexSignal[i] = new Complex(sample, 0.0);
				}

				Complex[] fftSpectrum = FFT.fft(ComplexSignal);
				
				int bitsPer = RECORDER_SAMPLERATE / (fftSpectrum.length);
				double sumOfAbsSignal = 0;
				int i = 0;
				// Normal Human speech range 85-180 Hz: male and 165-255 Female
				for (i = (int) Math.floor(50 / bitsPer); i < Math.ceil(300 / bitsPer); i++) {
					//NOTE : dont need the sqrt here as anyways we compare the max.
					AbsSignal[i] = Math.sqrt(Math.pow(fftSpectrum[i].re(), 2)
							+ Math.pow(fftSpectrum[i].im(), 2));
					/*AbsSignal[i] = Math.pow(fftSpectrum[i].re(), 2)
							+ Math.pow(fftSpectrum[i].im(), 2);*/
					sumOfAbsSignal = sumOfAbsSignal + AbsSignal[i];
					/*if (AbsSignal[i] > mMaxFFTSample) {
						mMaxFFTSample = AbsSignal[i];
						mPeakPos = i;
					}*/
				}
				Log.d("asd", "FFT : " + sumOfAbsSignal/i);
				Message m = new Message();
				m.what = 1;
				m.arg1 = (int) (sumOfAbsSignal/i)/1000;
				MainActivity.mainHandler.sendMessage(m);
			}
		}
	};
}
