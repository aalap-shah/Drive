package com.agreeya.memoir.activity;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.agreeya.memoir.R;
import com.agreeya.memoir.receivers.AlarmReceiver;
import com.agreeya.memoir.services.AudioRecorder;
import com.agreeya.memoir.services.ControllerService;
import com.agreeya.memoir.services.SilentPhotoShot;
import com.agreeya.memoir.services.VideoRecorder;
import com.agreeya.memoir.sqlitedatabase.DataSource;
import com.agreeya.memoir.sqlitedatabase.InsertIntoDB;

public class DriveActivity extends Activity {

	private ListView mDriveView = null;
	private DriveAdapter mDriveAdapter = null;
	private static ArrayList<Element> mDriveElements = null;
	// private DataSource datasource;
	private Intent mIntent;
	int color;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		DataSource.init(this, new PathChangeListner() {
			//
			@Override
			public void onPathChanged() {
				Log.v("asdfg", " onPathChanged ");
				// if (type.equalsIgnoreCase("audio")) {
				// Log.v("asd audio", path);
				// mDriveElements.add(new AudioElement(path));
				// }
				// if (type.equalsIgnoreCase("video")) {
				// Log.v("asd audio", path);
				// mDriveElements.add(new VideoElement(path));
				// }
				// if (type.equalsIgnoreCase("single")
				// || type.equalsIgnoreCase("multishot")) {
				// Log.v("asd audio", path);
				// mDriveElements.add(new PhotoElement(path));
				// }
				mDriveAdapter.notifyDataSetChanged();

			}
		});
		DataSource.open();
		// List<PathRepo> values = DataSource.getAllPaths();
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

		int avg = (rC + gC + bC) / 3;
		Log.v("asd", "shade" + avg);
		// color = ((rC << 16) & 0xFF0000) | ((gC << 8) & 0x00FF00)
		// | (bC & 0x0000FF);

		if (avg > 3200000) {
			// lighter shade
			color = 0x0000000; // for darker shade on light background
		} else {
			// darker shade
			color = 0xfffffff; // for lighter shade on darker background
		}

		mDriveElements = new ArrayList<Element>();
		mDriveElements = DataSource.getAllElements();
		// Log.v("asd list", ":" + values.size());
		// for (int i = 0; i < values.size(); i++) {
		// path = values.get(i);
		// Log.v("asd adding", path.getType());
		// if (path.getType().equalsIgnoreCase("audio")) {
		// Log.v("asd audio", path.getPath());
		// mDriveElements.add(new AudioElement(path.getPath()));
		// }
		// if (path.getType().equalsIgnoreCase("video")) {
		// Log.v("asd audio", path.getPath());
		// mDriveElements.add(new VideoElement(path.getPath()));
		// }
		// if (path.getType().equalsIgnoreCase("single")
		// || path.getType().equalsIgnoreCase("multishot")) {
		// Log.v("asd audio", path.getPath());
		// mDriveElements.add(new PhotoElement(path.getPath()));
		// }
		// }
		mDriveView = (ListView) findViewById(R.id.DriveView);
		mDriveAdapter = new DriveAdapter(this, mDriveElements, color);
		mDriveView.setAdapter(mDriveAdapter);

		// mIntent = new Intent(this, ControllerService.class);
		// startService(mIntent);
	}

	public interface PathChangeListner {
		public void onPathChanged();
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Config.RGB_565);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// if (item.getItemId() == R.id.action_settings) {
		// Toast.makeText(this.getApplicationContext(),
		// "Displaying the text from onOptionItemSelected",
		// Toast.LENGTH_LONG).show();
		// }

		if (item.getItemId() == R.id.action_start_trip) {
			mIntent = new Intent(this, ControllerService.class);
			startService(mIntent);
		}

		if (item.getItemId() == R.id.action_stop_trip) {

			Intent intentAlarm = new Intent(this, AlarmReceiver.class);
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(PendingIntent.getBroadcast(this, 1,
					intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
			mIntent = new Intent(this, AudioRecorder.class);
			stopService(mIntent);
			mIntent = new Intent(this, ControllerService.class);
			stopService(mIntent);
			mIntent = new Intent(this, SilentPhotoShot.class);
			stopService(mIntent);
			mIntent = new Intent(this, VideoRecorder.class);
			stopService(mIntent);
			mIntent = new Intent(this, InsertIntoDB.class);
			stopService(mIntent);

		}
		return super.onOptionsItemSelected(item);

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
		private MediaPlayer player;
		Handler seekHandler = null;

		public class ViewHolder {
			int type = -1;
			ImageView iv = null;
			VideoView vv = null;
			ImageView pv = null;
			SeekBar seek_bar = null;
			ImageView play_button = null;
			ImageView pause_button = null;

		}

		public class Seek implements Runnable {
			private SeekBar skbar = null;
			private MediaPlayer player = null;
			private ImageView imgbtn;
			private Handler seekHandler = null;

			Seek(SeekBar skbar, MediaPlayer player, Handler seekHandler,
					ImageView imgbtn) {
				this.skbar = skbar;
				this.player = player;
				this.skbar.setMax(this.player.getDuration() / 2);
				this.seekHandler = seekHandler;
				this.imgbtn = imgbtn;
			}

			@Override
			public void run() {
				if (player.isPlaying()) {
					skbar.setProgress(player.getCurrentPosition());
					seekHandler.postDelayed(this, 100);
				}
			}
		}

		public DriveAdapter(Context context, ArrayList<Element> elements,
				int Color) {
			this.mContext = context;
			this.mElements = elements;
			this.mLayoutInflater = LayoutInflater.from(context);
			this.color = Color;
		}

		@SuppressLint({ "NewApi", "ResourceAsColor" })
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh = null;
			Log.v("asdfg", " getView " + mElements.size() + ", position="
					+ position);
			Element element = mElements.get(position);

			if (convertView == null) {
				vh = new ViewHolder();
				Log.v("type", "" + vh.type);
				if (element.type == Element.TYPE_PHOTO) {
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_photo, null);
					vh.iv = (ImageView) convertView.findViewById(R.id.PhotoIV);
				} else if (element.type == Element.TYPE_AUDIO) {
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_audio, null);
					vh.play_button = (ImageView) convertView
							.findViewById(R.id.PlayIV);
					vh.seek_bar = (SeekBar) convertView
							.findViewById(R.id.ProgressSB);

				} else if (element.type == Element.TYPE_VIDEO) {
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_video, null);
					vh.vv = (VideoView) convertView.findViewById(R.id.VideoIV);
					vh.pv = (ImageView) convertView
							.findViewById(R.id.PlayVideo);
				}
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
				// if (vh.type != element.type) {
				// vh.type = element.type;
				// }
				if (element.type == Element.TYPE_PHOTO) {
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_photo, null);
					vh.iv = (ImageView) convertView.findViewById(R.id.PhotoIV);
				} else if (element.type == Element.TYPE_AUDIO) {
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_audio, null);

					vh.play_button = (ImageView) convertView
							.findViewById(R.id.PlayIV);
					// vh.pause_button = (ImageView) convertView
					// .findViewById(R.id.PauseIV);
					vh.seek_bar = (SeekBar) convertView
							.findViewById(R.id.ProgressSB);

				} else if (element.type == Element.TYPE_VIDEO) {
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_video, null);
					vh.vv = (VideoView) convertView.findViewById(R.id.VideoIV);
					vh.pv = (ImageView) convertView
							.findViewById(R.id.PlayVideo);
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
				// mediaController = new MediaController(this.mContext);
				Uri video = Uri.parse(ve.path);
				vh.vv.setVideoURI(video);
				// vh.vv.start();
				// mediaController.setAnchorView(vh.vv);
				vh.vv.setBackgroundColor(android.R.color.transparent);
				vh.pv.setTag(R.string.PlayPauseKey, "Stopped");
				vh.pv.setTag(R.string.PlayPauseVideo, vh.vv);
				vh.pv.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						final ImageView imgvw = (ImageView) v;
						if (imgvw.getTag(R.string.PlayPauseKey).equals(
								"Stopped")) {
							imgvw.setTag(R.string.PlayPauseKey, "Playing");
							imgvw.setImageResource(View.INVISIBLE);
							((VideoView) imgvw.getTag(R.string.PlayPauseVideo))
									.start();
						} else if (imgvw.getTag(R.string.PlayPauseKey).equals(
								"Playing")) {
							imgvw.setTag(R.string.PlayPauseKey, "Stopped");
							imgvw.setImageResource(R.drawable.ic_menu_play_clip);
							// seekRun.player.pause();
							((VideoView) imgvw.getTag(R.string.PlayPauseVideo))
									.pause();
						}

						((VideoView) imgvw.getTag(R.string.PlayPauseVideo))
								.setOnCompletionListener(new OnCompletionListener() {

									@Override
									public void onCompletion(MediaPlayer mp) {
										// TODO Auto-generated method stub
										imgvw.setImageResource(R.drawable.ic_menu_play_clip);
										imgvw.setTag(R.string.PlayPauseKey,
												"Stopped");
									}
								});

					}
				});

				// vh.vv.setMediaController(mediaController);
				// vh.vv.setZOrderOnTop(true);
				// vh.vv.requestFocus();
			}

			if (element.type == Element.TYPE_AUDIO) {
				AudioElement ae = (AudioElement) element;
				Uri audio = Uri.parse(ae.path);
				player = MediaPlayer.create(mContext, audio);
				// vh.seek_bar.setMax(player.getDuration());
				Seek seekRun = new Seek(vh.seek_bar, player, mHandler,
						vh.play_button);
				vh.play_button.setTag(R.string.PlayPauseKey, "Stopped");
				vh.play_button.setTag(R.string.Seek_Bar, seekRun);
				vh.play_button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final ImageView imgvw = (ImageView) v;
						if (imgvw.getTag(R.string.PlayPauseKey).equals(
								"Stopped")) {
							imgvw.setTag(R.string.PlayPauseKey, "Playing");
							imgvw.setImageResource(android.R.drawable.ic_media_pause);
							mHandler.post((Seek) imgvw
									.getTag(R.string.Seek_Bar));
							// mHandler.post(seekRun);
							// seekRun.player.start();
							((Seek) imgvw.getTag(R.string.Seek_Bar)).player
									.start();
						} else if (imgvw.getTag(R.string.PlayPauseKey).equals(
								"Playing")) {
							imgvw.setTag(R.string.PlayPauseKey, "Stopped");
							imgvw.setImageResource(android.R.drawable.ic_media_play);
							// seekRun.player.pause();
							((Seek) imgvw.getTag(R.string.Seek_Bar)).player
									.pause();
						}

						((Seek) imgvw.getTag(R.string.Seek_Bar)).player
								.setOnCompletionListener(new OnCompletionListener() {
									// When audio is done will change pause to
									// play
									public void onCompletion(MediaPlayer mp) {
										// seekRun.imgbtn.setImageResource(android.R.drawable.ic_media_play);
										// seekRun.skbar.setProgress(0);
										((Seek) imgvw.getTag(R.string.Seek_Bar)).imgbtn
												.setImageResource(android.R.drawable.ic_media_play);
										((Seek) imgvw.getTag(R.string.Seek_Bar)).skbar
												.setProgress(0);
										imgvw.setTag(R.string.PlayPauseKey,
												"Stopped");

									}
								});

					}
				});

				// player.setOnCompletionListener(new OnCompletionListener() {
				// //When audio is done will change pause to play
				// public void onCompletion(MediaPlayer mp) {
				// //
				// seekRun.imgbtn.setImageResource(android.R.drawable.ic_media_play);
				// // seekRun.skbar.setProgress(0);
				//
				// }
				// });

			}
			return convertView;
		}

		private Handler mHandler = new Handler();

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

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent stopIntent = new Intent(this, ControllerService.class);
		Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(PendingIntent.getBroadcast(this, 1, intentAlarm,
				PendingIntent.FLAG_UPDATE_CURRENT));
		stopService(stopIntent);
		super.onBackPressed();
	}

}
