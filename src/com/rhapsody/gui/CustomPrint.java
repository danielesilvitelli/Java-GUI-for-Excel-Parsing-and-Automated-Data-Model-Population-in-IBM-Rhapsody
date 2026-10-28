package com.rhapsody.gui;

import java.time.LocalTime;



/*
	Utility class which returns current time in hours-minutes-seconds.
*/
public class CustomPrint {
	
	private static String buffer = "";
	
	public static String getTimeForGUI() {
		int hour = LocalTime.now().getHour();
		int minutes = LocalTime.now().getMinute();
		int seconds = LocalTime.now().getSecond();
		
		String h = String.valueOf(hour);
		String m = String.valueOf(minutes);
		String s = String.valueOf(seconds);
		
		if(hour < 10)
			h = "0" + h;
		if(minutes < 10)
			m = "0" + m;
		if(seconds < 10)
			s = "0" + s;
		
		return h + ":" + m + ":" + s + " - ";
	}
	
	/*
		Utility function which updates a string "buffer" that contains all info to display.
	*/
	public static void printOnJTextPane(String current) {
		try {
			buffer += getTimeForGUI() + current + "\n";
		}catch(StringIndexOutOfBoundsException sioobe) {}
	}
	
	
	public static String getBufferContent() {
		return buffer;
	}
	
	
	public static void emptyBuffer() {
		buffer = "";
	}
	
}
