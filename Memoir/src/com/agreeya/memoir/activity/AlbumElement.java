package com.agreeya.memoir.activity;

import java.util.ArrayList;

public class AlbumElement extends Element {
	public ArrayList<String> paths = null;
	
	public AlbumElement() {
		super(TYPE_ALBUM);
	}

	public AlbumElement(ArrayList<String> paths) {
		super(TYPE_ALBUM);
		this.paths = paths;
	}
}
