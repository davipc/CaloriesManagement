package com.toptal.calories.rest.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RestUtil {

	public static String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}";
	public static String TIME_REGEX = "\\d{2}:\\d{2}";
	
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
			DEFAULT_DATE_MAX = new Date(System.currentTimeMillis() + (long) 1000*60*60*24*365);
			DEFAULT_TIME_MIN = TIME_FORMAT.parse("00:00");
			DEFAULT_TIME_MAX = TIME_FORMAT.parse("23:59");
		} catch (ParseException e) {
			// will never happen - just keep the print in any case
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Formats the input Date from JSON format into a valid Date object.
	 * Returns the default date parameter if the input date is null.
	 * @param inputDate
	 * @param defaultDate
	 * @return
	 * @throws IllegalArgumentException If input is not null and doesn't have a valid format 
	 */
	public static Date getDateFromJSON(String inputDate, Date defaultDate) 
	throws IllegalArgumentException {
		Date d = null;
		if (inputDate == null) {
			d = defaultDate;
		} else {
			if (!inputDate.matches(DATE_REGEX)) {
				throw new IllegalArgumentException("Invalid format on input date: " + inputDate);
			}
			try {
				 
				d = DATE_FORMAT.parse(inputDate);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Invalid format on input date: " + inputDate, e);
			}
		}
		
		return d;
	}

	/**
	 * Formats the input time from JSON format into a valid Date object.
	 * Returns the default time parameter if the input date is null.
	 * @param inputTime
	 * @param defaultTime
	 * @return
	 * @throws IllegalArgumentException If input is not null and doesn't have a valid format 
	 */
	public static Date getTimeFromJSON(String inputTime, Date defaultTime) 
	throws IllegalArgumentException {
		Date d = null;
		if (inputTime == null) {
			d = defaultTime;
		} else {
			if (!inputTime.matches(TIME_REGEX)) {
				throw new IllegalArgumentException("Invalid format on input time: " + inputTime);
			}
			
			try {
				d = TIME_FORMAT.parse(inputTime);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Invalid format on input time: " + inputTime, e);
			}
		}
		
		return d;
	}

	/**
	 * Creates a collection from an iterable and returns it 
	 * @param iter
	 * @return
	 */
	public static <E> List<E> makeList(Iterable<E> iter) {
	    List<E> list = null;
	    
	    if (iter != null) {
		    list = new ArrayList<E>();
		    for (E item : iter) {
		        list.add(item);
		    }
	    }
	    
	    return list;
	}	
}
