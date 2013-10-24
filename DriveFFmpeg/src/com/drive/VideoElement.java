package com.drive;

public class VideoElement extends Element{
	public String path = null;
	
	public VideoElement(String path) {
		super(TYPE_VIDEO);
		this.path = path;
	}
}
