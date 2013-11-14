package com.agreeya.memoir.util;

public class AudioChunk {
	public short[] data;
	public int length = 0;
	int used = 0;
	
	public AudioChunk(int size) {
		data = new short[size];
	}
}
