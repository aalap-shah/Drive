package com.agreeya.memoir.model;

import android.app.PendingIntent;

/**
 *For Structuring the element to be inserted in the dispatcher queue  
 */
public class QueueElement {
	
	public int trip_no;
	public String command;
	PendingIntent pIntent;

	/**
	 * for initializing the QueueElement members 
	 * @param trip_no : trip number of the command
	 * @param command : start/stop
	 * @param pIntent : pending intent to controller service
	 */
	public QueueElement(int trip_no,String command,PendingIntent pIntent){
		this.trip_no = trip_no;
		this.command = command;
		this.pIntent = pIntent;
	}
}
