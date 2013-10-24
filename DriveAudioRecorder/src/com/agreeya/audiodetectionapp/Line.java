package com.agreeya.audiodetectionapp;

import android.graphics.Paint;

public class Line {
	double[] points = null;
	double bump = -1;
	int color;
	public Paint paint;
	int mSize = 0;

	public Line(int size, int color) {
		mSize = size;
		points = new double[mSize];
		int i = 0;
		for (i = 0; i < mSize; i++) {
			points[i] = 0;
		}
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(color);
		paint.setStrokeWidth(2);
	}

	public void shift(int point) {
		if (mSize > 0) {
			int i = 0;
			for (i = 1; i < mSize; i++) {
				points[i - 1] = points[i];
			}
			points[mSize - 1] = point;
			if(bump != -1) {
				bump = bump - 1;
			}
		}
	}

	public void resize(int newSize) {
		double[] tmpPoints = new double[newSize];
		int i = 0;
		for (i = 0; i < mSize; i++) {
			tmpPoints[i] = points[i];
		}
		mSize = newSize;
		points = tmpPoints;
	}
}
