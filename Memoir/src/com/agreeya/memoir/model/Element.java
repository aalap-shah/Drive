package com.agreeya.memoir.model;

/**
 *For determining the type of element.
 */
public class Element {
	public static final int TYPE_AUDIO = 0;
	public static final int TYPE_ALBUM = 1;
	public static final int TYPE_VIDEO = 2;
	public static final int TYPE_PHOTO = 3;
	
	public int type = -1;
	
	public Element(int type) {
		this.type = type;
	}
}
