package com.agreeya.memoir.activity;

public class Element {
	public static int TYPE_AUDIO = 0;
	public static int TYPE_ALBUM = 1;
	public static int TYPE_VIDEO = 2;
	public static int TYPE_PHOTO = 3;
	
	public int type = -1;
	
	public Element(int type) {
		this.type = type;
	}
}
