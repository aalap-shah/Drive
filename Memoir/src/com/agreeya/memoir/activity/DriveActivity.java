package com.agreeya.memoir.activity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.agreeya.memoir.R;
import com.agreeya.memoir.services.ControllerService;
import com.agreeya.memoir.sqlitedatabase.DataSource;
import com.agreeya.memoir.sqlitedatabase.PathRepo;

public class DriveActivity extends Activity {

	private ListView mDriveView = null;
	private DriveAdapter mDriveAdapter = null;
	private ArrayList<Element> mDriveElements = null;
	private DataSource datasource;
	private PathRepo path;
	private Intent mIntent;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		datasource = new DataSource(this);
		datasource.open();
		mIntent = new Intent(this,ControllerService.class);
		startService(mIntent);
		List<PathRepo> values = datasource.getAllPaths();
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
		Log.v("asd list",":"+values.size());
		for(int i=0;i<values.size();i++){
			path=values.get(i);
			Log.v("asd adding",path.getType());
			if(path.getType().equalsIgnoreCase("audio")){
				Log.v("asd audio",path.getPath());
				mDriveElements.add(new AudioElement(path.getPath()));
			}
			if(path.getType().equalsIgnoreCase("video")){
				Log.v("asd audio",path.getPath());
				mDriveElements.add(new VideoElement(path.getPath()));
			}
			if(path.getType().equalsIgnoreCase("single") || path.getType().equalsIgnoreCase("multishot")){
				Log.v("asd audio",path.getPath());
				mDriveElements.add(new PhotoElement(path.getPath()));
			}
		}
		

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
			VideoView vv = null;
			public SeekBar seek_bar = null;
			public ImageButton play_button;
			public ImageButton pause_button;
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
					vh.play_button = (ImageButton) findViewById(R.id.PlayIV);
					vh.pause_button =(ImageButton) findViewById(R.id.PlayIV); 
					vh.seek_bar = (SeekBar) findViewById(R.id.ProgressSB);

				} else if (element.type == Element.TYPE_VIDEO) {
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_video, null);
					vh.vv = (VideoView) convertView.findViewById(R.id.VideoIV);
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

						vh.play_button = (ImageButton) findViewById(R.id.PlayIV);
						vh.pause_button =(ImageButton) findViewById(R.id.PlayIV); 
						vh.seek_bar = (SeekBar) findViewById(R.id.ProgressSB);

					} else if (element.type == Element.TYPE_VIDEO) {
						convertView = mLayoutInflater.inflate(
								R.layout.drive_line_item_video, null);
						vh.vv = (VideoView) convertView
								.findViewById(R.id.VideoIV);
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

			if (element.type == Element.TYPE_VIDEO) {
				VideoElement ve = (VideoElement) element;
				Uri video = Uri.parse(ve.path);
				vh.vv.setVideoURI(video);
				vh.vv.setMediaController(new MediaController(this.mContext));
				vh.vv.requestFocus();
			}

			if (element.type == Element.TYPE_AUDIO) {
				AudioElement ae = (AudioElement)element;
				Uri audio = Uri.parse(ae.path);
				final MediaPlayer player = MediaPlayer.create(mContext, audio);
				vh.play_button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
							player.start();
//							vh.play_button.setVisibility(View.GONE);
					}
				});

//				vh.pause_button.setOnClickListener(new View.OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						// TODO Auto-generated method stub
//							player.pause();
//							vh.play_button.setVisibility(View.GONE);
//					}
//				});
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
}
