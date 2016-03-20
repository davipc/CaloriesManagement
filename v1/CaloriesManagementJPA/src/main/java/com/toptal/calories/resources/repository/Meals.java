package com.toptal.calories.resources.repository;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.toptal.calories.resources.RepositoryException;
import com.toptal.calories.resources.entity.Meal;


public class Meals extends BaseCustomRepository<Meal> {
	
	public static Logger logger = Logger.getLogger(Meals.class);
	
	/**
	 * Finds and returns all the Meals associated with the input user.
	 * @param userId The meal's user Id.
	 * @return The Meal objects associated with the input userId. 
	 * If no Meal object is associated to the input userId, an empty list is returned.
	 */
	@SuppressWarnings("unchecked")
	public List<Meal> findUserMeals(Integer userId) throws RepositoryException {
		long startTime = System.currentTimeMillis();
		logger.info("Finding meals from user " + userId);
		try {
			if (userId == null) {
				throw new RepositoryException("Null userId!!");
			} else {
				List<Meal> meals = null;
				EntityManager em = null; 
				try {
					em = getEntityManager();
					meals = em.createNamedQuery("Meal.findByUserId")
								.setParameter("userId", userId)
								.getResultList();
							
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error in find for meals from user " + userId, pe);
				} finally {
					if (em != null && em.isOpen()) {
						em.close();
					}
				}
				logger.info("Finished finding Meals from user " + userId + getWrapupMsg(startTime));
				return meals;
			}
		} catch (RepositoryException e) {
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
		
	}

	/**
	 * Finds and returns all the Meals associated with the input user
	 * that happened between input from and to dates and between from and to hours.
	 * @param userId The meal's user Id.
	 * @param fromDate
	 * @param toDate
	 * @param startHour
	 * @param startMinute
	 * @param endHour
	 * @param endMinute
	 * @return The Meal objects associated with the input userId and within both date and time ranges. 
	 * If no Meal object is found, an empty list is returned.
	 */
	@SuppressWarnings("unchecked")
	public List<Meal> findUserMealsInDateAndTimeRanges(Integer userId, Date fromDate, Date toDate, Time fromTime, Time toTime) 
	throws RepositoryException {
		long startTime = System.currentTimeMillis();

		// make sure dates have the time fields correct
		fromDate = setTimeInfo(fromDate, 0, 0, 0);
		toDate = setTimeInfo(toDate, 23, 59, 59);
		
		String formattedInputParameters = String.format("from user %s from %tF to %tF and from %tR to %tR ", userId, fromDate, toDate, fromTime, toTime);
		logger.info("Finding meals " + formattedInputParameters);

		try {
			validateFindUserMealsInDateAndTimeRangesParms(userId, fromDate, toDate, fromTime, toTime);
			
			
			int startMinutes = getMinuteOfDay(fromTime);
			int endMinutes = getMinuteOfDay(toTime);

			List<Meal> meals = null;
			EntityManager em = null; 
			try {
				em = getEntityManager();
				meals = em.createNamedQuery("Meal.findInDateAndTimeRange")
							.setParameter("userId", userId)
							.setParameter("startDate", fromDate, TemporalType.TIMESTAMP)
							.setParameter("endDate", toDate, TemporalType.TIMESTAMP)
							.setParameter("startTimeMinutes", startMinutes)
							.setParameter("endTimeMinutes", endMinutes)
							.getResultList();
						
			} catch (PersistenceException pe) {
				throw new RepositoryException(
						String.format("Error in find for meals " + formattedInputParameters), 
						pe);
			} finally {
				if (em != null && em.isOpen()) {
					em.close();
				}
			}
			logger.info("Finished finding Meals " + formattedInputParameters + getWrapupMsg(startTime));
			return meals;
		} catch (RepositoryException e) {
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
		
	}

	/** 
	 * Returns the number of minutes elapsed in the day in the input Time.
	 * @param time
	 * @return The number of minutes elapsed in the day in the input Time.
	 */
	private int getMinuteOfDay(Time time) {
		int result = 0;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		result = 60 * cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE);
		
		return result;
	}
	
	private Date setTimeInfo(Date date, int hour, int minute, int second) {
		Date result = null;
		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, second);
			cal.set(Calendar.MILLISECOND, 0);
			
			result = cal.getTime();
		}		
		return result;
	}

	private void validateFindUserMealsInDateAndTimeRangesParms(Integer userId, Date fromDate, Date toDate, Time fromTime, Time toTime) 
	throws RepositoryException {
		if (userId == null) {
			throw new RepositoryException("Null user Id provided");
		} else if (fromDate == null) {
			throw new RepositoryException("Null start date provided");
		} else if (toDate == null) {
			throw new RepositoryException("Null end date provided");
		} else if (fromTime == null) {
			throw new RepositoryException("Null start time provided");
		} else if (toTime== null) {
			throw new RepositoryException("Null end time provided");
		} else if (fromDate.after(toDate)) {
			throw new RepositoryException(String.format("Start date %tF is after end date %tF", fromDate, toDate));
		} else if (fromTime.after(toTime)) {
			throw new RepositoryException(String.format("Start time %tR is after end time %tR", fromTime, toTime));
		}
	}

//	/**
//	 * Finds and returns all the Meals associated with the input user
//	 * that happened between input from and to dates and between from and to hours.
//	 * @param userId The meal's user Id.
//	 * @param startDate
//	 * @param endDate
//	 * @param hourFrom  
//	 * @param hourTo  
//	 * @return The Meal objects associated with the input userId. 
//	 * If no Meal object is associated to the input userId, an empty list is returned.
//	 */
//	@SuppressWarnings("unchecked")
//	public List<Meal> testDateFuncs(Integer startHour, Integer startMinute, Integer endHour, Integer endMinute) 
//	throws RepositoryException {
//		long startTime = System.currentTimeMillis();
//
//		if (startHour == null)
//			startHour = 0;
//		if (endHour == null)
//			endHour = 23;
//		if (startMinute == null)
//			startHour = 0;
//		if (endMinute == null)
//			endHour = 59;
//
//		int startMinutes = 60 *  startHour + startMinute;
//		int endMinutes = 60 *  endHour + endMinute;
//				
//		logger.info("Finding testDateFuncs");
//		try {
//			List<Meal> meals = null;
//			EntityManager em = null; 
//			try {
//				em = getEntityManager();
//				meals = em.createNamedQuery("Meal.testDateFuncs")
//							.setParameter("startTimeMinutes", startMinutes)
//							.setParameter("endTimeMinutes", endMinutes)
//							.getResultList();
//						
//			} catch (PersistenceException pe) {
//				throw new RepositoryException("Error in testDateFuncs", pe);
//			} finally {
//				if (em != null && em.isOpen()) {
//					em.close();
//				}
//			}
//			logger.info("Finished testDateFuncs " + getWrapupMsg(startTime));
//			return meals;
//		} catch (RepositoryException e) {
//			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
//			throw e;
//		}
//		
//	}
//	
}
