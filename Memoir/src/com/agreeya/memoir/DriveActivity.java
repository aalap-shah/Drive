package com.agreeya.memoir;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.RecyclerListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.agreeya.memoir.DriveActivity.DriveAdapter.AudioViewHolder;
import com.agreeya.memoir.DriveActivity.DriveAdapter.Seek;
import com.agreeya.memoir.DriveActivity.DriveAdapter.VideoViewHolder;
import com.agreeya.memoir.model.AudioElement;
import com.agreeya.memoir.model.Element;
import com.agreeya.memoir.model.PhotoElement;
import com.agreeya.memoir.model.VideoElement;
import com.agreeya.memoir.services.CompilerService;
import com.agreeya.memoir.services.ControllerService;

/**
 * This class is responsible for the user interface.Deals with the generation of
 * media elements and placing them into the list in chronological order.
 */
public class DriveActivity extends Activity {

	private static String COMMAND = "command";
	private static String STOP_TRIP = "Stop Trip";
	private static String START_TRIP = "Start Trip";
	private static String PAUSE_TRIP = "Pause Trip";
	private static String RESUME_TRIP = "Resume Trip";

	private ListView mDriveView = null;
	// private ListView mDriveView = null;
	// private MediaMetadataRetriever mMediaData;
	private DriveAdapter mDriveAdapter = null;
	// private DragNDropAdapter mDriveAdapter = null;
	private static ArrayList<Element> mDriveElements = null;
	private Intent mIntent;
	int color;
	String mFrame;
	int flag = 0;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		((MemoirApp) this.getApplication()).mDBHelper
				.setListener(new PathChangeListner() {
					//
					@Override
					public void onPathChanged() {
						Log.v("asdfg", " onPathChanged ");
						((BaseAdapter) mDriveAdapter).notifyDataSetChanged();

					}
				});

		setContentView(R.layout.activity_drive);
		// RelativeLayout Container = (RelativeLayout)
		// findViewById(R.id.Container);
		// // TODO : API check
		// Container.setBackground(WallpaperManager.getInstance(this)
		// .peekDrawable());
		// // Container.setBackgroundDrawable(WallpaperManager.getInstance(this)
		// // .peekDrawable());
		// Bitmap b = drawableToBitmap(WallpaperManager.getInstance(this)
		// .peekDrawable());
		// ByteBuffer buff = ByteBuffer.allocate(b.getByteCount());
		// b.copyPixelsToBuffer(buff);
		// byte[] bytes = buff.array();
		// int rC = 0, gC = 0, bC = 0;
		// for (int i = 0; i < bytes.length; i = i + 4) {
		// rC = rC + (bytes[i] & 0x0FF);
		// gC = gC + (bytes[i + 1] & 0x0FF);
		// bC = bC + (bytes[i + 2] & 0x0FF);
		// }
		// Log.d("asd", "rC " + rC + " gC" + gC + " bC = " + bC);
		// rC = (rC % bytes.length);
		// gC = (gC % bytes.length);
		// bC = (bC % bytes.length);
		// Log.d("asd", "rC " + rC + " gC" + gC + " bC = " + bC);
		//
		// int avg = (rC + gC + bC) / 3;
		// Log.v("asd", "shade" + avg);
		// // color = ((rC << 16) & 0xFF0000) | ((gC << 8) & 0x00FF00)
		// // | (bC & 0x0000FF);
		//
		// if (avg > 3000000) {
		// // lighter shade
		// color = 0x0000000; // for darker shade on light background
		// } else {
		// // darker shade
		// color = 0xfffffff; // for lighter shade on darker background
		// }
		//color = 0xFFD700; //Golden
		//color = 0xBD68C0;
		//color = 0xAE37D3;
		color = 0x8e564c;
		mDriveElements = ((MemoirApp) this.getApplication()).mDBHelper
				.getAllElements();
		mDriveView = (ListView) findViewById(R.id.DriveView);
		mDriveView.setFadingEdgeLength(0);
		mDriveAdapter = new DriveAdapter(this, mDriveElements, color,R.id.drag);
		mDriveView.setAdapter(mDriveAdapter);
		mDriveView.setRecyclerListener(new RecyclerListener() {
			
			@Override
			public void onMovedToScrapHeap(View view) {
				// TODO Auto-generated method stub
				if(view.getTag() instanceof AudioViewHolder){
					AudioViewHolder avh = (AudioViewHolder) view.getTag();
					Seek sk = (Seek)avh.play_button.getTag(R.string.Seek_Bar);
					sk.player.stop();
					sk.imgbtn.setImageResource(android.R.drawable.ic_media_play);
					sk.skbar.setProgress(0);
				}
				if(view.getTag() instanceof VideoViewHolder){
					VideoViewHolder vvh = (VideoViewHolder) view.getTag();
					vvh.vv = (VideoView) vvh.pv
							.getTag(R.string.PlayPauseVideo);
					vvh.vv.stopPlayback();
				}
				
			}
		});

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Log.v("save instance ", "here I come :) ");
	}

	/**
	 * Interface for defining abstract method for listening change made in
	 * database
	 */
	public interface PathChangeListner {
		public void onPathChanged();
	}

	/**
	 * function to convert drawable to bitmap
	 * 
	 * @param drawable
	 * @return : Bitmap with RGB_565 configuration
	 */
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
		if (((MemoirApp) this.getApplication()).beginning == false) {
			menu.findItem(R.id.action_start_stop_trip).setTitle(STOP_TRIP);
			menu.findItem(R.id.action_compile).setVisible(true);
		}
		getMenuInflater().inflate(R.menu.drive, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem register = menu.findItem(R.id.action_pause_resume);
		if (((MemoirApp) this.getApplication()).startHit == 2) {
			// hide compile option when trip is started
			menu.findItem(R.id.action_compile).setVisible(false);
			register.setVisible(true);
		} else {
			register.setVisible(false);

		}
		if (((MemoirApp) this.getApplication()).startHit == 1) {
			// show compile option when trip is stopped
			menu.findItem(R.id.action_compile).setVisible(true);
			register.setTitle(R.string.action_pause_trip);
		}

		if (((MemoirApp) this.getApplication()).compileCheck == 2) {
			((MemoirApp) this.getApplication()).compileCheck = 1;
			menu.findItem(R.id.action_compile).setEnabled(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_start_stop_trip) {

			Log.v("test", item.getTitle().toString());
			if (item.getTitle().toString().equalsIgnoreCase(START_TRIP)) {
				item.setTitle(R.string.action_stop_trip);
				((MemoirApp) this.getApplication()).startHit = 2;
				((MemoirApp) this.getApplication()).continueSchedule = true;
				((MemoirApp) this.getApplication()).mDBHelper.createTrip(
						"first trip", "test", "a", "z", 10991);

				mIntent = new Intent(this, ControllerService.class);
				mIntent.putExtra(COMMAND, "start");
				mIntent.putExtra("rout", 1);
				((MemoirApp) this.getApplication()).trip = ((MemoirApp) this
						.getApplication()).mDBHelper.getTripId();

				startService(mIntent);
			} else if (item.getTitle().toString().equalsIgnoreCase(STOP_TRIP)) {

				item.setTitle(R.string.action_start_trip);
				((MemoirApp) this.getApplication()).startHit = 1;
				((MemoirApp) this.getApplication()).continueSchedule = false;
				mIntent = new Intent(this, ControllerService.class);
				mIntent.putExtra("rout", 1);
				mIntent.putExtra("trip",
						((MemoirApp) this.getApplication()).mDBHelper
								.getTripId());
				mIntent.putExtra(COMMAND, "stop");
				startService(mIntent);
			}
		}

		if (item.getItemId() == R.id.action_pause_resume) {

			if (item.getTitle().toString().equalsIgnoreCase(PAUSE_TRIP)) {

				item.setTitle(R.string.action_resume_trip);
				((MemoirApp) this.getApplication()).continueSchedule = false;
				mIntent = new Intent(this, ControllerService.class);
				mIntent.putExtra("rout", 1);
				mIntent.putExtra("trip",
						((MemoirApp) this.getApplication()).mDBHelper
								.getTripId());
				mIntent.putExtra(COMMAND, "stop");
				startService(mIntent);
			} else if (item.getTitle().toString().equalsIgnoreCase(RESUME_TRIP)) {
				item.setTitle(R.string.action_pause_trip);
				((MemoirApp) this.getApplication()).startHit = 2;
				((MemoirApp) this.getApplication()).continueSchedule = true;
				mIntent = new Intent(this, ControllerService.class);
				mIntent.putExtra(COMMAND, "start");
				mIntent.putExtra("rout", 1);
				startService(mIntent);
			}

		}
		if (item.getItemId() == R.id.action_clear_screen) {
			mDriveElements.clear();
			mDriveAdapter.clearElements(mDriveElements);
			mDriveView.setAdapter(mDriveAdapter);
		}

		if (item.getItemId() == R.id.action_compile) {
			mIntent = new Intent(this, CompilerService.class);
			startService(mIntent);
			item.setEnabled(false);
		}
		return super.onOptionsItemSelected(item);

	}

	/**
	 * Class for designing the UI for the media element
	 * 
	 */
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
			fg.setColors(new int[] { color1, color & 0x00FFFFFF, color2 });

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

	/**
	 * Class extending BaseAdapter, responsible for the configuring different
	 * media element UI with the actual media captured
	 */
	public class DriveAdapter extends BaseAdapter {

		private ArrayList<Element> mElements = null;
		private Context mContext = null;
		private LayoutInflater mLayoutInflater = null;
		private int color = 0;
		private MediaPlayer player = null;
		//private Handler mHandler;
		//Handler seekHandler = null;

		/**
		 * Holder class for image element
		 * 
		 */
		public class ImageViewHolder {
			ImageView iv = null;
		}

		/**
		 * Holder class for audio element
		 * 
		 */
		public class AudioViewHolder {
			SeekBar seek_bar = null;
			ImageView play_button = null;
		}

		/**
		 * Holder class for video element
		 * 
		 */
		public class VideoViewHolder {
			VideoView vv = null;
			ImageView pv = null;
			ImageView fv = null;
		}

		/**
		 * class extending Runnable, responsible for synchronizing the seek bar
		 * with the audio
		 * 
		 */
		public class Seek implements Runnable {
			private SeekBar skbar = null;
			private MediaPlayer player = null;
			private ImageView imgbtn;
			//private Handler seekHandler = null;

			/**
			 * Constructor for initializing the member of Seek class
			 * 
			 * @param skbar
			 *            : seekbar for audio
			 * @param player
			 *            : player responsible for controlling media functioning
			 * @param seekHandler
			 *            : Handler for synchronizing the seekbar with the audio
			 * @param imgbtn
			 *            : image button for play/pause option
			 */
			Seek(SeekBar skbar, MediaPlayer player, /*Handler seekHandler,*/
					ImageView imgbtn) {
				this.skbar = skbar;
				this.player = player;
				//this.seekHandler = seekHandler;
				this.imgbtn = imgbtn;
			}

			void config(SeekBar skbar, MediaPlayer player, /*Handler seekHandler,*/
					ImageView imgbtn) {
				this.skbar.removeCallbacks(this);
				this.skbar = skbar;
				this.player = player;
				//this.seekHandler = seekHandler;
				this.imgbtn = imgbtn;
				this.skbar.post(this);
			}

			@Override
			public void run() {
				if (player.isPlaying()) {
					skbar.setProgress(player.getCurrentPosition());
					skbar.postDelayed(this, 100);
					//seekHandler.postDelayed(this, 100);
				}
			}
		}

		/**
		 * Constructor for initializing the class members
		 * 
		 * @param context
		 *            : context
		 * @param elements
		 *            : ArrayList of all the existing elements
		 * @param Color
		 *            : color of element
		 */
		public DriveAdapter(Context context, ArrayList<Element> elements,
				int Color,int handler) {
			this.mContext = context;
			this.mElements = elements;
			this.mLayoutInflater = LayoutInflater.from(context);
			this.color = Color;
		}

		/**
		 * Function for clearing the screen
		 * 
		 * @param elements
		 *            : ArrayList of elements currently being displayed to user
		 */
		public void clearElements(ArrayList<Element> elements) {
			this.mElements = elements;
		}


		@Override
		public int getItemViewType(int position) {
			return super.getItemViewType(position);
		}
		
		@Override
		public boolean isEnabled(int position) {
			return super.isEnabled(position);
		}

		@SuppressLint({ "NewApi", "ResourceAsColor" })
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// ViewHolder vh = null;

			Log.v("asdfg", " getView " + mElements.size() + ", position="
					+ position + " View " + convertView);
			Element element = mElements.get(position);
			Log.d("asd", "Element " + element.type);
			// int view = this.getItemViewType(position);
			switch (element.type) {
			case Element.TYPE_PHOTO:
				ImageViewHolder ivh = null;
				if (convertView == null
						|| !(convertView.getTag() instanceof ImageViewHolder)) {
					Log.v("asd", "Photo View Fresh");
					ivh = new ImageViewHolder();
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_photo, null);
					ivh.iv = (ImageView) convertView.findViewById(R.id.PhotoIV);
					convertView.setTag(ivh);
				} else {
					Log.v("asd", "Photo View Recycled");
					ivh = (ImageViewHolder) convertView.getTag();
				}

				PhotoElement pe = (PhotoElement) element;

				((MemoirApp) getApplicationContext()).il.loadImage(pe.path,
						ivh.iv, new ImageLoader.ImageLoaderCallback() {

							@Override
							void OnDownload(String imageUrl, ImageView iv,
									Bitmap b) {
								if (iv != null) {
									iv.setImageBitmap(b);
								}
							}
						}, 3, 2, "default");

				break;

			case Element.TYPE_AUDIO:
				AudioViewHolder avh = null;
				Seek seekRun = null;
				if (convertView == null
						|| !(convertView.getTag() instanceof AudioViewHolder)) {
					Log.v("asd", "Audio View Fresh");
					avh = new AudioViewHolder();
					//mHandler = new Handler();
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_audio, null);
					avh.play_button = (ImageView) convertView
							.findViewById(R.id.PlayIV);
					avh.seek_bar = (SeekBar) convertView
							.findViewById(R.id.ProgressSB);
					flag = 1;
					convertView.setTag(avh);
				} else {
					flag = 2;
					Log.v("asd", "Audio View Recycled");
					avh = (AudioViewHolder) convertView.getTag();
					seekRun = (Seek) avh.play_button.getTag(R.string.Seek_Bar);

				}

				AudioElement ae = (AudioElement) element;
				Uri audio = Uri.parse(ae.path);
				player = MediaPlayer.create(mContext, audio);

				if (mContext != null && audio != null && player != null) {
					Log.v("asd", "Configuring audio element");

					if (flag == 1) {
						seekRun = new Seek(avh.seek_bar, player, /*mHandler,*/
								avh.play_button);
					} else {
						seekRun.config(avh.seek_bar, player, /*mHandler,*/
								avh.play_button);
					}

					seekRun.skbar.setMax(player.getDuration());
					avh.play_button.setTag(R.string.PlayPauseKey, "Stopped");
					avh.play_button.setTag(R.string.Seek_Bar, seekRun);
					avh.play_button
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									final ImageView imgvw = (ImageView) v;
									if (imgvw.getTag(R.string.PlayPauseKey)
											.equals("Stopped")) {
										imgvw.setTag(R.string.PlayPauseKey,
												"Playing");
										imgvw.setImageResource(android.R.drawable.ic_media_pause);
										imgvw.post((Seek) imgvw
												.getTag(R.string.Seek_Bar));
										((Seek) imgvw.getTag(R.string.Seek_Bar)).player
												.start();
									} else if (imgvw.getTag(
											R.string.PlayPauseKey).equals(
											"Playing")) {
										imgvw.setTag(R.string.PlayPauseKey,
												"Stopped");
										imgvw.setImageResource(android.R.drawable.ic_media_play);
										((Seek) imgvw.getTag(R.string.Seek_Bar)).player
												.pause();
									}

									((Seek) imgvw.getTag(R.string.Seek_Bar)).player
											.setOnCompletionListener(new OnCompletionListener() {
												// When audio is done will
												// change pause to
												// play
												public void onCompletion(
														MediaPlayer mp) {
													((Seek) imgvw
															.getTag(R.string.Seek_Bar)).imgbtn
															.setImageResource(android.R.drawable.ic_media_play);
													((Seek) imgvw
															.getTag(R.string.Seek_Bar)).skbar
															.setProgress(0);
													imgvw.setTag(
															R.string.PlayPauseKey,
															"Stopped");

												}
											});

								}
							});
				}

				break;

			case Element.TYPE_VIDEO:
				VideoViewHolder vvh = null;
				if (convertView == null
						|| !(convertView.getTag() instanceof VideoViewHolder)) {
					Log.v("asd", "Video View Fresh");
					vvh = new VideoViewHolder();
					convertView = mLayoutInflater.inflate(
							R.layout.drive_line_item_video, null);
					vvh.vv = (VideoView) convertView.findViewById(R.id.VideoIV);
					vvh.pv = (ImageView) convertView
							.findViewById(R.id.PlayVideo);
					vvh.fv = (ImageView) convertView.findViewById(R.id.Frame);
					convertView.setTag(vvh);
				} else {
					Log.v("asd", "Video View Recycled");
					vvh = (VideoViewHolder) convertView.getTag();
					vvh.vv = (VideoView) vvh.pv
							.getTag(R.string.PlayPauseVideo);
					vvh.vv.stopPlayback();
					vvh.vv.setVisibility(View.GONE);
					vvh.pv.setVisibility(View.VISIBLE);
					vvh.fv.setVisibility(View.VISIBLE);
					vvh.pv.setImageResource(R.drawable.play);
					vvh.pv.setTag(R.string.PlayPauseKey, "Stopped");

				}

				VideoElement ve = (VideoElement) element;
				Uri video = Uri.parse(ve.path);
				mFrame = ve.path.replace(".mp4", ".jpg");
				((MemoirApp) getApplicationContext()).il.loadImage(mFrame,
						vvh.fv, new ImageLoader.ImageLoaderCallback() {

							@Override
							void OnDownload(String imageUrl, ImageView iv,
									Bitmap b) {
								if (iv != null) {
									iv.setImageBitmap(b);
								}
							}
						}, 3, 2, "default");

				vvh.pv.setTag(R.string.PlayPauseKey, "Stopped");
				vvh.pv.setTag(R.string.PlayPauseVideo, vvh.vv);
				vvh.pv.setTag(R.string.frame, vvh.fv);
				vvh.pv.setTag(R.string.videouri, video);
				vvh.pv.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						final ImageView imgvw = (ImageView) v;
						final ImageView frame = (ImageView) imgvw
								.getTag(R.string.frame);
						VideoView vv = (VideoView) imgvw
								.getTag(R.string.PlayPauseVideo);
						if (imgvw.getTag(R.string.PlayPauseKey).equals(
								"Stopped")) {
							imgvw.setTag(R.string.PlayPauseKey, "Playing");
							imgvw.setImageResource(View.INVISIBLE);
							frame.setVisibility(View.INVISIBLE);

							vv.setVisibility(View.VISIBLE);
							vv.setZOrderOnTop(false);
							vv.setBackgroundColor(android.R.color.transparent);
							vv.setVideoURI((Uri) imgvw
									.getTag(R.string.videouri));
							vv.start();
						} else if (imgvw.getTag(R.string.PlayPauseKey).equals(
								"Playing")) {
							imgvw.setTag(R.string.PlayPauseKey, "Stopped");
							imgvw.setImageResource(R.drawable.play);
							vv.pause();
						}

						((VideoView) imgvw.getTag(R.string.PlayPauseVideo))
								.setOnCompletionListener(new OnCompletionListener() {

									@Override
									public void onCompletion(MediaPlayer mp) {
										// TODO Auto-generated method stub
										imgvw.setImageResource(R.drawable.ic_menu_play_clip);
										frame.setVisibility(View.VISIBLE);
										imgvw.setTag(R.string.PlayPauseKey,
												"Stopped");
										((VideoView) imgvw
												.getTag(R.string.PlayPauseVideo))
												.setVisibility(View.INVISIBLE);
									}
								});

					}
				});

				break;
			}
			Animation animation = null;
			if (position % 2 == 0)
				animation = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.leftin);
			else
				animation = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.rightin);
			animation.setDuration(300);
			convertView.startAnimation(animation);
			animation = null;
			convertView.setBackground(DrawableLayer.newInstance(color));
			convertView.setPadding(15, 15, 15, 15);
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
			// TODO Auto-generated method stub
			return position;
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		// super.onBackPressed();
		moveTaskToBack(true);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// ListView lv = (ListView) findViewById(R.id.DriveView);
		// // here animations start
		// int first = lv.getFirstVisiblePosition();
		// int last = lv.getLastVisiblePosition();
		// for (int k = 0; k < last - first + 1; k++) {
		// View child = lv.getChildAt(k);
		// // int pos = lv.getPositionForView(child);
		// child.startAnimation(applyRotation(0, 0, 360));
		// }
		// lv.invalidate();
	}

	@Override
	protected void onDestroy() {
		Log.d("asd", "OnDestroy of Drive Activity");
		super.onDestroy();

	}
//
//	private Rotation applyRotation(int position, float start, float end) {
//		// Find the center of the container ,here i have hardcoded the values
//		final float centerX = 0.0f;// view.getWidth()/2.0f;
//		final float centerY = 50.0f;// view.getHeight()/2.0f;
//		// Create a new 3D rotation with the supplied parameter
//		// The animation listener is used to trigger the next animation
//		final Rotation rotation = new Rotation(start, end, centerX, centerY,
//				0.0f, true);
//		rotation.setDuration(500);
//		rotation.setFillAfter(true);
//		rotation.setRepeatCount(3);
//		rotation.setInterpolator(new AccelerateInterpolator());
//		rotation.setAnimationListener(new DisplayNextView(position));
//		return rotation;
//	}
//
//	private final class DisplayNextView implements Animation.AnimationListener {
//		private final int mPosition;
//
//		private DisplayNextView(int position) {
//			mPosition = position;
//		}
//
//		public void onAnimationStart(Animation animation) {
//		}
//
//		public void onAnimationEnd(Animation animation) {
//		}
//
//		public void onAnimationRepeat(Animation animation) {
//		}
//	}
}
