package com.agreeya.audiodetectionapp;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	private BumpMap mMap = null;
	private int line = 0;
	private int lineFreq = 0;
	
	private int mHeight = 0;
	private static int RECORDER_SAMPLERATE = 8000;
	private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static AudioRecord mAudioRecorder = null;
	private static int mBufferSizeInBytes = 0;
	private static byte mAudioBuffer[] = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		mHeight = displaymetrics.heightPixels;
		Log.d("asd" ," Height = " + mHeight);
		mMap = (BumpMap) findViewById(R.id.map);
		mMap.addMarker(new Marker(mHeight / 2, Color.rgb(255, 0, 0), 2));

		line = mMap.addLine(Color.rgb(255, 255, 0));
		lineFreq = mMap.addLine(Color.rgb(0, 0, 255));

		// Get the minimum buffer size required for the successful creation of
		// an AudioRecord object.
		mBufferSizeInBytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
		// Initialize Audio Recorder.
		mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, mBufferSizeInBytes);

		// Start Recording.
		mAudioRecorder.startRecording();
		mAudioBuffer = new byte[mBufferSizeInBytes];

		recordNow();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		shouldStop = true;
		super.onPause();
	}
	
	private boolean shouldStop = false;
	private int standardCrossingValue = 0;
	private int crossingValueSetCounter = 0;
	private int multiplier = 1;
	
	public void recordNow() {
		mMap.postDelayed(new Runnable() {

			@Override
			public void run() {

				short sample1 = 0;
				short sample2 = 0;

				int numberOfReadBytes = mAudioRecorder.read(mAudioBuffer, 0,
						mBufferSizeInBytes);
				
				final int byteSize= 128;
				byte[] input = new byte[byteSize];
				for( int i=0; i< numberOfReadBytes/byteSize; i++ ) 
				{
					for(int j=0;j<byteSize;j++){
						input[j]= mAudioBuffer[(i*byteSize)+j];
					}
//					Complex[] fftTempArray = doFFT(input, byteSize);
//					int freqValue=0;
//					for(int j = 0 ; j< fftTempArray.length ; j++){
//						freqValue =( (int)fftTempArray[j].abs() + freqValue )/2;
//					}
//					freqValue = freqValue/2;
//					mMap.addPoint(lineFreq,  freqValue);
					
					double [] array = calculateFFT(input, byteSize/2);
					int freqValue=0;
					for(int j = 0 ; j< array.length ; j++){
						freqValue = (int)array[j];
						mMap.addPoint(lineFreq,  freqValue);
					}
					Log.d("asd", "freqValue = " + freqValue);
					
				}
				
				
				int zeroCrossing = 0;
				int noOfSamples = 0;
				for (int i = 2; i < mBufferSizeInBytes; i = i + 2) {
					sample1 = (short) ((mAudioBuffer[i]) | mAudioBuffer[i + 1] << 8);
					sample2 = (short) ((mAudioBuffer[i - 2]) | mAudioBuffer[i - 1] << 8);
					if (sample1 * sample2 < 0) {
						zeroCrossing++;
					}
					noOfSamples++;
				}
				if(crossingValueSetCounter < 100) {
					standardCrossingValue = (standardCrossingValue + Math.abs(zeroCrossing))/2;
					crossingValueSetCounter++;
				}
				
				Log.d("asd", "NoOfSample = " + noOfSamples + " 0Cross="
						+ zeroCrossing + " standardCrossingValue =" + standardCrossingValue);
						
				mMap.addPoint(line, (mHeight/2) - ((zeroCrossing - standardCrossingValue) * multiplier));

				if(!shouldStop)
					mMap.postDelayed(this, 10);
				else
					mAudioRecorder.stop();
			}
		}, 10);
	}
	
	
	
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
	    
	    
//	    StringBuilder strmicBufferData = new StringBuilder();
//	    
//	    strmicBufferData.append("\n\n Start **********strmicBufferData***************==\n");
//	    for (int i = 0; i < micBufferData.length; i++) {
//	    	strmicBufferData.append(" , "+micBufferData[i]);
//		}
//	    strmicBufferData.append("\n\nLenght = "+strmicBufferData.length()+"\n\n End **********strmicBufferData***************\n");
//	    Log.d("TAG", strmicBufferData.toString());
//	    Log.e("TAG","strmicBufferData lenght = "+strmicBufferData.length());
//	    Log.e("TAG","micBufferData lenght = "+micBufferData.length);
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
	
	public double[] calculateFFT(byte[] signal, final int mNumberOfFFTPoints)
    {           
        double mMaxFFTSample;

        double temp;
        Complex[] y;
        Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
        double[] absSignal = new double[mNumberOfFFTPoints/2];

        for(int i = 0; i < mNumberOfFFTPoints; i++){
            temp = (double)((signal[2*i] & 0xFF) | (signal[2*i+1] << 8)) / 32768.0F;
            complexSignal[i] = new Complex(temp,0.0);
        }

        y = FFT.fft(complexSignal); // --> Here I use FFT class

        mMaxFFTSample = 0.0;
        int mPeakPos = 0;
        for(int i = 0; i < (mNumberOfFFTPoints/2); i++)
        {
             absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
             if(absSignal[i] > mMaxFFTSample)
             {
                 mMaxFFTSample = absSignal[i];
                 mPeakPos = i;
             } 
        }

        return absSignal;

    }
	
}
