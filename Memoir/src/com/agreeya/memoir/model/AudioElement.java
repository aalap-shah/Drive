package com.agreeya.memoir.model;

/**
 * for configuring the audio element UI 
 *
 */
public class AudioElement extends Element {
	public String path = null;
	
	public AudioElement(String path) {
		super(TYPE_AUDIO);
		this.path = path;
	}
}
