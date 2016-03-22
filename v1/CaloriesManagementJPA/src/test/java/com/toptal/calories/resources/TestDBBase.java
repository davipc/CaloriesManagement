package com.toptal.calories.resources;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDBBase {
	public static Pattern CAUSE_BY_PATTERN = Pattern.compile("Caused by: .*");
	
	public String getCurrentMethodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();	
	}
	
	public Date addToDate(Date d, int unit, int amount) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		cal.add(unit, amount);
		return  cal.getTime();
	}
	
	public Date getDateDaysAgoAtTime(int daysAgo, int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -daysAgo);
		
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTime();
	}
	
	public Date getTime(int hours, int minutes) 
	throws InvalidParameterException {
		if (hours < 0 || hours > 23)
			throw new InvalidParameterException("Invalid hour: " + hours);
		if (minutes < 0 || minutes > 59)
			throw new InvalidParameterException("Invalid minute: " + minutes);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.HOUR_OF_DAY, hours);
		cal.set(Calendar.MINUTE, minutes);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTime();
	}
	
	
	
	public String getCauseMessages(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		Matcher matcher = CAUSE_BY_PATTERN.matcher(sw.toString());
		StringBuffer causeStr = new StringBuffer();
		int n = 0;
		while ( matcher.find() ) {
			if ( n++ > 0 )
				causeStr.append("; ");
			causeStr.append(matcher.group(0));
		}
		return causeStr.toString();
	}

	public void printException(Exception e) {
		e.printStackTrace();
		
		// go down to the root cause
		Throwable rootCause = e;
		while (rootCause.getCause() != null) {
			rootCause = rootCause.getCause();
		}
		
		if (rootCause instanceof java.sql.BatchUpdateException) {
			Exception next = ((java.sql.BatchUpdateException)rootCause).getNextException();
			System.out.println("getNextException() :\n");
			if (next != null) {
				next.printStackTrace();
			}
		}
		
		System.out.println();
	}
}