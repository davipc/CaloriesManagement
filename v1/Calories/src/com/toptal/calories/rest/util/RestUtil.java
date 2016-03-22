package com.toptal.calories.rest.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RestUtil {

	public static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); 
	public static DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm"); 

	public static Date DEFAULT_DATE_MIN;  
	public static Date DEFAULT_DATE_MAX;  
	public static Date DEFAULT_TIME_MIN;  
	public static Date DEFAULT_TIME_MAX;  
	
	static {
		try {
			DEFAULT_DATE_MIN = DATE_FORMAT.parse("1970-01-01");
			// will be 365 days from now
			DEFAULT_DATE_MAX = new Date(System.currentTimeMillis() + 1000*60*60*24*365);
			DEFAULT_TIME_MIN = TIME_FORMAT.parse("00:00");
			DEFAULT_TIME_MAX = TIME_FORMAT.parse("23:59");
		} catch (ParseException e) {
			// will never happen - just keep the print in any case
			e.printStackTrace();
		}
		
	}
	
	public static Date getDateFromJSON(String inputDate, Date defaultDate) 
	throws IllegalArgumentException {
		Date d = null;
		if (inputDate == null) {
			d = defaultDate;
		} else {
			try {
				d = DATE_FORMAT.parse(inputDate);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Invalid format on input date: " + inputDate, e);
			}
		}
		
		return d;
	}

	public static Date getTimeFromJSON(String inputTime, Date defaultTime) 
	throws IllegalArgumentException {
		Date d = null;
		if (inputTime == null) {
			d = defaultTime;
		} else {
			try {
				d = TIME_FORMAT.parse(inputTime);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Invalid format on input time: " + inputTime, e);
			}
		}
		
		return d;
	}

	
}
