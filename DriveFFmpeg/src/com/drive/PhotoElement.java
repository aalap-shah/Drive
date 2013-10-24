package com.drive;

public class PhotoElement extends Element{
	public String path = null;
	
	public PhotoElement(String path) {
		super(TYPE_PHOTO);
		this.path = path;
	}
}
