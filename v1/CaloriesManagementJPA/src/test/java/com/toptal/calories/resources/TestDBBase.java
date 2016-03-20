package com.toptal.calories.resources;

import java.io.PrintWriter;
import java.io.StringWriter;
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