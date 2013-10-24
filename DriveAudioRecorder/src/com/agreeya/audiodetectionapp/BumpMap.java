package com.agreeya.audiodetectionapp;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BumpMap extends View {

	private ArrayList<Line> lines = null;
	private ArrayList<Marker> markers = null;
	private Paint paint = new Paint();
	private int mWidth = 0;
	private int mHeight = 0;

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
		mHeight = h;
		Log.d("asd", "Width =" + mWidth + " Height=" + mHeight);
		for (Line line : lines) {
			line.resize(mWidth);
		}

		super.onSizeChanged(w, h, oldw, oldh);
	}

	public BumpMap(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setDrawingCacheEnabled(true);
		lines = new ArrayList<Line>();
		markers = new ArrayList<Marker>();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.rgb(50, 50, 50));
		paint.setStrokeWidth(2);
	}

	public BumpMap(Context context) {
		super(context);
		this.setDrawingCacheEnabled(true);
	}

	@Override
	synchronized protected void onDraw(Canvas canvas) {
		int i = 0;
		for (Line line : lines) {
			for (i = 0; i < mWidth - 20; i++) {
				canvas.drawLine((float) i, (float) line.points[i],
						(float) (i + 1), (float) line.points[i + 1], line.paint);
			}
			if(line.bump != -1) {
				line.paint.setStrokeWidth(2);
				canvas.drawLine((float)line.bump, (float)0, (float)line.bump, (float)720, line.paint);
				line.paint.setStrokeWidth(1);
			}
		}

		for (Marker m : markers) {
			canvas.drawLine(0, m.marker, mWidth, m.marker, m.paint);
		}
		// canvas.
		super.onDraw(canvas);
	}

	public int addLine(int color) {
		Line l = new Line(mWidth, color);
		lines.add(l);
		return lines.indexOf(l);
	}

	public void addPoint(int lineNo, int point) {
		Line l = lines.get(lineNo);
		l.shift(point);
		invalidate();
	}
	
	public void addBump(int lineNo) {
		Line l = lines.get(lineNo);
		l.bump = mWidth;
	}

	public void addMarker(Marker m) {
		markers.add(m);
	}
	
	public void removeAllMarker() {
		markers.clear();
	}

	public Bitmap getBitmap() {
		return this.getDrawingCache();
	}
}
