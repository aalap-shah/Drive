package com.agreeya.memoir.activity;

public class AudioElement extends Element {
	public String path = null;
	
	public AudioElement(String path) {
		super(TYPE_AUDIO);
		this.path = path;
	}
}