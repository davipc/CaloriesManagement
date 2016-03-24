package com.toptal.calories.resources.util;

import org.slf4j.Logger;

public class LogUtil {
	
	// Max time for DB operation to be performed. If an operation takes more than that, a WARN message will be sent to the log file.
	public static int MAX_TIME_DB_OPER_MS = 200;
	
	/**
	 * Logs the end of method message using the appropriate level - if it took too long, as a warning, otherwise using standardLevel.
	 * @param msgTemplate The end of method message
	 * @param startTime The time the method started being executed
	 * @param standardLevel The level to use in case the method didn't take too long to execute. 
	 */
	public static void logEnd(Logger logger, String action, long startTime) {
		long endTime = System.currentTimeMillis();
		String msg = "Finished " + action + " after " + (System.currentTimeMillis() - startTime) + " ms";
		
		if (endTime - startTime > MAX_TIME_DB_OPER_MS) {
			logger.warn(msg);
		} else {
			logger.info(msg);
		}
		
	}
}
