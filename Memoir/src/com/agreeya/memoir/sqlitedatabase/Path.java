package com.agreeya.memoir.sqlitedatabase;

/**
 * Getter Setter class for Media information
 *
 */
public class Path {
	private long id;
	private String path;
	private String type;
	private double time;
	private int trip_no;

	public int getTripNo(){
		return trip_no;
	}
	public void setTripNo(int trip_no){
		this.trip_no=trip_no;
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

}