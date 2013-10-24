package com.drive;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class DriveActivity extends Activity {

	private ListView mDriveView = null;
	private DriveAdapter mDriveAdapter = null;
	private ArrayList<Element> mDriveElements = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_drive);
		RelativeLayout Container = (RelativeLayout) findViewById(R.id.Container);
		// TODO : API check
		Container.setBackground(WallpaperManager.getInstance(this)
				.peekDrawable());
		Container.setBackgroundDrawable(WallpaperManager.getInstance(this)
				.peekDrawable());
		Bitmap b = drawableToBitmap(WallpaperManager.getInstance(this)
				.peekDrawable());
		ByteBuffer buff = ByteBuffer.allocate(b.getByteCount());
		b.copyPixelsToBuffer(buff);
		byte[] bytes = buff.array();
		int rC = 0, gC = 0, bC = 0;
		for (int i = 0; i < bytes.length; i = i + 4) {
			rC = rC + (bytes[i] & 0x0FF);
			gC = gC + (bytes[i + 1] & 0x0FF);
			bC = bC + (bytes[i + 2] & 0x0FF);
		}
		Log.d("asd", "rC " + rC + " gC" + gC + " bC = " + bC);
		rC = (rC % bytes.length);
		gC = (gC % bytes.length);
		bC = (bC % bytes.length);
		Log.d("asd", "rC " + rC + " gC" + gC + " bC = " + bC);
		
		int color = ((rC << 16) & 0xFF0000) | ((gC << 8) & 0x00FF00)
				| (bC & 0x0000FF);

		mDriveElements = new ArrayList<Element>();
		mDriveElements.add(new AudioElement(null));
		mDriveElements.add(new PhotoElement("/storage/sdcard0/1.jpg"));
		mDriveElements.add(new AudioElement(null));
		mDriveElements.add(new PhotoElement("/storage/sdcard0/2.png"));
		mDriveElements.add(new AudioElement(null));
		mDriveElements.add(new PhotoElement("/storage/sdcard0/3.jpg"));
		mDriveElements.add(new AudioElement(null));
		mDriveElements.add(new PhotoElement("/storage/sdcard0/1.jpg"));
		mDriveElements.add(new AudioElement(null));
		mDriveElements.add(new PhotoElement("/storage/sdcard0/2.png"));
		mDriveElements.add(new AudioElement(null));
		mDriveElements.add(new PhotoElement("/storage/sdcard0/3.jpg"));

		mDriveView = (ListView) findViewById(R.id.DriveView);
		mDriveAdapter = new DriveAdapter(this, mDriveElements, color);
		mDriveView.setAdapter(mDriveAdapter);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.drive, menu);
		return true;
	}

	public static class DrawableLayer extends LayerDrawable {

		private DrawableLayer(Drawable[] l) {
			super(l);
		}

		@SuppressLint("NewApi")
		public static DrawableLayer newInstance(int color) {
			GradientDrawable fg = new GradientDrawable();
			fg.setShape(GradientDrawable.RECTANGLE);
			/*fg.setBounds(16, 16, 16, 16);*/
			fg.setGradientType(GradientDrawable.LINEAR_GRADIENT);
			int color1 = ((color & 0x00FFFFFF) | 0x88000000);
			int color2 = ((color & 0x00FFFFFF) | 0xAA000000);
			fg.setColors(new int[] { color1, 0x00FFFFFF, color2 });

			/*GradientDrawable bg = new GradientDrawable();
			bg.setShape(GradientDrawable.RECTANGLE);
			bg.setBounds(14, 16, 12, 12);
			bg.setGradientCenter(0.3f, 0.4f);
			bg.setGradientType(GradientDrawable.LINEAR_GRADIENT);
			bg.setOrientation(GradientDrawable.Orientation.BR_TL);
			bg.setColors(new int[] { 0xAA000000, 0x00000000, 0x00000000 });*/
			Drawable[] layers = { fg };
			return new DrawableLayer(layers);
		}
	}

	public class DriveAdapter extends BaseAdapter {

		private ArrayList<Element> mElements = null;
		private Context mContext = null;
		private LayoutInflater mLayoutInflater = null;
		private int color = 0;

		private class ViewHolder {
			int type = -1;
			ImageView iv = null;
		}

		public DriveAdapter(Context context, ArrayList<Element> elements,
				int Color) {
			this.mContext = context;
			this.mElements = elements;
			this.mLayoutInflater = LayoutInflater.from(context);
			this.color = Color;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh = null;
			Element element = mElements.get(position);
			if (convertView == null) {
				vh = new ViewHolder();
				vh.type = element.type;
				if (element.type == Element.TYPE_PHOTO) {
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_photo, null);
					vh.iv = (ImageView) convertView.findViewById(R.id.PhotoIV);
				} else if (element.type == Element.TYPE_AUDIO) {
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_audio, null);
				}
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
				if (vh.type != element.type) {
					vh.type = element.type;
					if (element.type == Element.TYPE_PHOTO) {
						convertView = mLayoutInflater.inflate(
								R.layout.drive_line_item_photo, null);
						vh.iv = (ImageView) convertView
								.findViewById(R.id.PhotoIV);
					} else if (element.type == Element.TYPE_AUDIO) {
						convertView = mLayoutInflater.inflate(
								R.layout.drive_line_item_audio, null);
					}
				}
				convertView.setTag(vh);
			}

			// convertView.setBackgroundDrawable(DrawableLayer.newInstance());
			convertView.setBackground(DrawableLayer.newInstance(color));
			convertView.setPadding(15, 15, 15, 15);

			if (element.type == Element.TYPE_PHOTO) {
				PhotoElement pe = (PhotoElement) element;
				vh.iv.setImageBitmap(BitmapFactory.decodeFile(pe.path));
				vh.iv.setAlpha(1f);
			}
			return convertView;
		}

		@Override
		public int getCount() {
			return mElements.size();
		}

		@Override
		public Element getItem(int position) {
			return mElements.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}
}
