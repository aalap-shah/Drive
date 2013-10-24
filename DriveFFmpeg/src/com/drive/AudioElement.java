package com.drive;

public class AudioElement extends Element {
	public String path = null;
	
	public AudioElement(String path) {
		super(TYPE_AUDIO);
		this.path = path;
	}
}
