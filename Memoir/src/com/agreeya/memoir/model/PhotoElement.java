package com.agreeya.memoir.model;

/**
 * for configuring the photo element UI
 *
 */
public class PhotoElement extends Element{
	public String path = null;
	
	public PhotoElement(String path) {
		super(TYPE_PHOTO);
		this.path = path;
	}
}
