package com.agreeya.audiodetectionapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	private BumpMap mMap = null;
	private int line = 0;
	private int lineFreq = 0;

	private int mHeight = 0;
	/*private static int RECORDER_SAMPLERATE = 44100;
	private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static AudioRecord mAudioRecorder = null;
	private static int mBufferSizeInBytes = 0;
	private static byte mAudioBuffer[] = null;

	private ScheduledThreadPoolExecutor pool = null;*/

	private static int MSG_WHAT_ZERO_CROSSING = 1;
	private static int MSG_WHAT_FFT = 2;
	public static Handler mainHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		// pool = new ScheduledThreadPoolExecutor(3);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		mHeight = displaymetrics.heightPixels;
		Log.d("asd", " Height = " + mHeight);
		mMap = (BumpMap) findViewById(R.id.map);
		mMap.addMarker(new Marker(mHeight / 2, Color.rgb(255, 0, 0), 2));

		line = mMap.addLine(Color.rgb(255, 255, 0));
		lineFreq = mMap.addLine(Color.rgb(0, 0, 255));

		// Get the minimum buffer size required for the successful creation of
		// an AudioRecord object.
		/*mBufferSizeInBytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
		// Initialize Audio Recorder.
		mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, mBufferSizeInBytes);

		// Start Recording.
		mAudioRecorder.startRecording();
		mAudioBuffer = new byte[mNumberOfFFTPoints*2];*/

		mainHandler = new Handler() {
			int freqPoint = 0;
			int zeroCrossingPoint = 0;

			@Override
			public void handleMessage(Message msg) {
				// Log.d("asd", "Message received with what = "+msg.what);

				boolean shouldUpdateUI = false;
				//Log.d("asd", "" + System.currentTimeMillis());
				if (msg.what == MSG_WHAT_ZERO_CROSSING) {
					// Log.d("asd", "");
					// mMap.addPoint(lineFreq, msg.arg1);

					// com.agreeya.audiodetectionapp.Log.d("asd",
					// "NoOfSample = " + noOfSamples + " 0Cross="
					// + zeroCrossing + " standardCrossingValue =" +
					// standardCrossingValue);

					zeroCrossingPoint = msg.arg1;
					shouldUpdateUI = true;
				} else if (msg.what == MSG_WHAT_FFT) {
					freqPoint = msg.arg1;
					shouldUpdateUI = true;
					Log.d("asd", "Message received with freq = " + freqPoint);
				}
				if (shouldUpdateUI) {
					mMap.addPoint(lineFreq, freqPoint);
					mMap.addPoint(line, (mHeight / 2) - ((zeroCrossingPoint)));
				}
			}
		};

		// recordNow();

		mMap.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (com.agreeya.audiodetectionapp.Log.file()) {

				}
				com.agreeya.audiodetectionapp.Log.init(getApplicationContext());
			}
		});
	}

	@Override
	protected void onPause() {
		// com.agreeya.audiodetectionapp.Log.file();
		 Intent intent = new Intent(this, AudioIntelligenceService.class);
		 stopService(intent);
		super.onPause();
	}

	@Override
	protected void onResume() {
		 Intent intent = new Intent(this, AudioIntelligenceService.class);
		 startService(intent);
		super.onResume();
	}

	@Override
	public void onDestroy() {
		//mAudioRecorder.release();
		//mPool.shutdown();
		super.onDestroy();
	}

	/*
		private int standardCrossingValue = 0;
		private int crossingValueSetCounter = 0;
		private int multiplier = 1;
		
		private ZeroCrossingRunnable mRecordRunnable = null;
		private FFTRunnable mFFTRunnable  = null;
		public void recordNow() {
			
			mBufferSizeInBytes=
			mNumberOfFFTPoints*2;
			mRecordRunnable = new ZeroCrossingRunnable();
			mFFTRunnable = new FFTRunnable();
			pool.scheduleAtFixedRate(mRecordRunnable , 0, 10, TimeUnit.MILLISECONDS);
			pool.scheduleAtFixedRate(mFFTRunnable , 0, 20, TimeUnit.MILLISECONDS);
			
		}

		private class ZeroCrossingRunnable implements Runnable  {
			@Override
			public void run() {

				short sample1 = 0;
				short sample2 = 0;

				int numberOfReadBytes = mAudioRecorder.read(mAudioBuffer, 0,
						(mNumberOfFFTPoints*2 ));
				
	//			Log.d("asd", "numberOfReadBytes = "+numberOfReadBytes+", mAudioBuffer = "+(mAudioBuffer==null ?"is NULL ":mAudioBuffer));

				//			final int byteSize= 128;
				//			byte[] input = new byte[byteSize];
				//			int freqValue=0;
				//			for( int i=0; i< numberOfReadBytes/byteSize; i++ ) 
				//			{
				//				for(int j=0;j<byteSize;j++){
				//					input[j]= mAudioBuffer[(i*byteSize)+j];
				//				}
				//				Complex[] fftTempArray = doFFT(input, byteSize);
				//				
				//				for(int j = 0 ; j< fftTempArray.length ; j++){
				//					freqValue =( (int)fftTempArray[j].abs() + freqValue )/2;
				//				}
				//				
				//				
				////				double [] array = calculateFFT(input, byteSize/2);
				////				int freqValue=0;
				////				for(int j = 0 ; j< array.length ; j++){
				////					freqValue = (int)array[j];
				////					mMap.addPoint(lineFreq,  freqValue);
				////				}
				//				
				//			}
				//
				//			Log.d("asd", "freqValue = " + freqValue);
				//			mMap.addPoint(lineFreq,  freqValue);


				double temp;
				int zeroCrossing = 0;
				for (int i = 2 ,  fftIndex = 0; i < mBufferSizeInBytes; i = i + 2) {
					temp= ((mAudioBuffer[i]) | mAudioBuffer[i + 1] << 8);
					sample1 = (short)temp;
					sample2 = (short) ((mAudioBuffer[i - 2]) | mAudioBuffer[i - 1] << 8);
					if (sample1 * sample2 < 0) {
						zeroCrossing++;
					}
					if(fftIndex < mNumberOfFFTPoints){
						complexSignal[fftIndex] = new Complex(temp/ 32768.0F,0.0);
					}
					fftIndex++;
				}
				if(crossingValueSetCounter < 100) {
					standardCrossingValue = (standardCrossingValue + Math.abs(zeroCrossing))/2;
					crossingValueSetCounter++;
				}

				
				//Log.d("asd", "freqValue = " + point+",  numberOfReadBytes = "+numberOfReadBytes);
				
				
				
				//Send message to UI thread
				//mainHandler.obtainMessage(1, point, zeroCrossing).sendToTarget();
				Message m = new Message();
				m.what = 1;
				m.arg1 = zeroCrossing;
				mainHandler.sendMessage(m);
	//			mMap.addPoint(lineFreq, point);
	//
	//
	//			//			com.agreeya.audiodetectionapp.Log.d("asd", "NoOfSample = " + noOfSamples + " 0Cross="
	//			//					+ zeroCrossing + " standardCrossingValue =" + standardCrossingValue);
	//
	//			mMap.addPoint(line, (mHeight/2) - ((zeroCrossing - standardCrossingValue) * multiplier));

				
			}

		};



		public static Complex[] doFFT(byte totalByteBuffer[], int noOfBytes){
			double[] micBufferData = new double[totalByteBuffer.length];
			final int bytesPerSample = 2; // As it is 16bit PCM
			final double amplification = 100.0; // choose a number as you like


			for (int index = 0, floatIndex = 0; index < noOfBytes - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
				double sample = 0;
				for (int b = 0; b < bytesPerSample; b++) {
					int v = totalByteBuffer[index + b];
					if (b < bytesPerSample - 1 || bytesPerSample == 1) {
						v &= 0xFF;
					}
					sample += v << (b * 8);
				}
				double sample32 = amplification * (sample / 32768.0);
				micBufferData[floatIndex] = sample32;
			}


			Complex[] fftTempArray = new Complex[micBufferData.length];
			for (int i=0; i<micBufferData.length; i++)
			{
				fftTempArray[i] = new Complex(micBufferData[i], 0);
			}

			Complex[] fftArray = FFT.fft(fftTempArray);

			return fftArray;
			//	    StringBuilder str = new StringBuilder();
			//	    
			//	    str.append("\n\n Start ********FINAL*****************\n");
			//	    for (int i = 0; i < fftArray.length; i++) {
			//			str.append(" , "+fftArray[i]);
			//		}
			//	    
			//	    str.append("\n\n *****************END *************************\n");
			//	    Log.d("TAGOutput", str.toString());
		}

		final int mNumberOfFFTPoints =2048;
		Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
		double[] absSignal = new double[mNumberOfFFTPoints];
		public double calculateFFT()
		{           
			double mMaxFFTSample = 0.0;
			int mPeakPos = 0;
			int bitsPer =0;
			try{
			Log.d("asd", "signal lengh = "+mAudioBuffer.length);
			double temp;
			Complex[] y;
			
			

			for(int fftIndex = 0; fftIndex < mNumberOfFFTPoints; fftIndex++){
				temp = (double)((mAudioBuffer[2*fftIndex] & 0xFF) | (mAudioBuffer[2*fftIndex+1] << 8)) / 32768.0F;
				complexSignal[fftIndex] = new Complex(temp,0.0);
			}

			y = FFT.fft(complexSignal); // --> Here I use FFT class

			
			
			 bitsPer =RECORDER_SAMPLERATE / (y.length*2);
			
			
			
			
	//		for(int i = 0; i < (mNumberOfFFTPoints/2); i++)
	//		{
	//			absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
	//			if(absSignal[i] > mMaxFFTSample)
	//			{
	//				mMaxFFTSample = absSignal[i];
	//				mPeakPos = i;
	//			} 
	//		}
			
			
			//Normal Human speech range 85-180 Hz: male and 165-255 Female
			for(int i =50/bitsPer; i < 300/bitsPer; i++)
			{
	//			mMaxFFTSample =  ((mMaxFFTSample + y[i].abs())/2);
				
				absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
				if(absSignal[i] > mMaxFFTSample)
				{
					mMaxFFTSample = absSignal[i];
					mPeakPos = i;
				} 
				
			}

			}
			catch (Exception e){
				Log.e("asd", "Exception = "+e.getMessage() +", e = "+e);
				e.printStackTrace();
			}
			return ((absSignal[mPeakPos])*bitsPer/3);

	//		return mMaxFFTSample*4;


		}

		
		class FFTRunnable implements Runnable{

			@Override
			public void run() {
				int point = (int)calculateFFT();
				mainHandler.obtainMessage(2, point, 0).sendToTarget();
			}
			
		}*/
}
