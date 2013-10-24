package com.agreeya.audiodetectionapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;

public class Log {

	private static File bumpsFile = null;
	private static OutputStreamWriter bumpsWriter = null;
	private static String path = null;
	private static boolean enabled = false;

	private static StringBuffer mStrBuffer = null;

	public static String init(Context c) {
		String fileName = null;
		try {
			android.util.Log.d("asd", "inside init");
			path = Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES).getAbsolutePath();
			fileName = "AudioDetectorDump" + System.currentTimeMillis()
					+ ".txt";
			bumpsFile = new File(path, fileName);
			if (bumpsFile.exists()) {
				bumpsFile.delete();
				android.util.Log.d("asd", "Creating new file");
			}
			bumpsFile.createNewFile();
			android.util.Log.d("asd", "Creating bump writer");
			bumpsWriter = new OutputStreamWriter(new FileOutputStream(
					bumpsFile, true));
			mStrBuffer = new StringBuffer();
			enabled = true;
		} catch (IOException e) {
			android.util.Log.d("asd", "Exception");
			e.printStackTrace();
		}
		return fileName;
	}

	public static void d(String tag, String message) {
		android.util.Log.d(tag, message);
		if (enabled) {
			mStrBuffer
					.append("TAG:" + tag + " Time:"
							+ System.currentTimeMillis() + " Message:"
							+ message + "\n");
		}
	}

	public static void e(String tag, String message) {
		try {
			android.util.Log.e(tag, message);
			if (enabled) {
				bumpsWriter.append("TAG:" + tag + " Time:"
						+ System.currentTimeMillis() + " Message:" + message
						+ "\n");
				bumpsWriter.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean file() {
		if (enabled) {
			try {
				Log.d("asd", "Inside deinit");
				bumpsWriter.append(mStrBuffer.toString());
				bumpsWriter.flush();
				bumpsWriter.close();
				enabled = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else {
			return false;
		}
	}
}
