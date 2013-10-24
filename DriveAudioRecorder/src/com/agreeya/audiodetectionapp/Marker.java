package com.agreeya.audiodetectionapp;

import android.graphics.Color;
import android.graphics.Paint;

public class Marker {
	float marker = 0;
	Paint paint = null;
	int markerColor = Color.rgb(0, 0, 0);
	int markerWidth = 1;
	
	public Marker(float value, int color, int width) {
		this.marker = value;
		this.markerColor = color;
		this.markerWidth = width;
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(color);
		paint.setStrokeWidth(width);
	}
}
