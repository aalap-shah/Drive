package com.agreeya.memoir.activity;


public class VideoElement extends Element {
	public String path = null;

	public VideoElement(String path) {
		super(TYPE_VIDEO);
		this.path = path;
	}
	// final VideoView video_view;
	// //MediaController media_control = new MediaController(this);
	// //media_control.setAnchorView(video_view);
	// Uri video = Uri.parse("/storage/sdcard0/videooutput1384344331673.mp4");
	// video_view.setVideoURI(video);
	// video_view.setMediaController(new MediaController(this));
	// //video_view.setMediaController(media_control);
	// video_view.requestFocus();
}
