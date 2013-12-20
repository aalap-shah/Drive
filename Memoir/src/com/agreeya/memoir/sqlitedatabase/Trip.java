package com.agreeya.memoir.sqlitedatabase;

/**
 * Getter Setter class for Trip information
 *
 */
public class Trip {
	private long id;
	private String trip_name;
	private String trip_description;
	private String trip_source;
	private String trip_destination;
	private double trip_time;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTripName() {
		return trip_name;
	}

	public void setTripName(String trip_name) {
		this.trip_name = trip_name;
	}

	public String getTripDescription() {
		return trip_description;
	}

	public void setTripDescription(String trip_description) {
		this.trip_description= trip_description;
	}
	
	public String getTripSource() {
		return trip_source;
	}

	public void setTripSource(String trip_source) {
		this.trip_source= trip_source;
	}

	public String getTripDestination() {
		return trip_destination;
	}

	public void setTripDestination(String trip_destination) {
		this.trip_destination= trip_destination;
	}

	public double getTripTime() {
		return trip_time;
	}

	public void setTripTime(double trip_time) {
		this.trip_time = trip_time;
	}

}