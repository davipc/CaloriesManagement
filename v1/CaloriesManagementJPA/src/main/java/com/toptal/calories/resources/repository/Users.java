package com.toptal.calories.resources.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.entity.User;


public class Users extends BaseRepository<User> {
	public static Logger logger = LoggerFactory.getLogger(Meals.class);

	/**
	 * Finds and returns all the Meals associated with the input user
	 * that happened between input from and to dates and between from and to hours.
	 * @param userId The meal's user Id.
	 * @param fromDate (time portion will be ignored)
	 * @param toDate (time portion will be ignored)
	 * @param fromTime (date portion will be ignored)
	 * @param toTime (date portion will be ignored)
	 * @return The Meal objects associated with the input userId and within both date and time ranges. 
	 * If no Meal objects are found, an empty list is returned.
	 */
	@SuppressWarnings("unchecked")
	public List<Meal> findMealsInDateAndTimeRanges(Integer userId, Date fromDate, Date toDate, Date fromTime, Date toTime) 
	throws RepositoryException {
		long startTime = System.currentTimeMillis();

		String formattedInputParameters = String.format("from user %s from %tF to %tF and from %tR to %tR ", userId, fromDate, toDate, fromTime, toTime);
		logger.info("Finding meals " + formattedInputParameters);

		try {
			validateFindUserMealsInDateAndTimeRangesParms(userId, fromDate, toDate, fromTime, toTime);
			
			
			List<Meal> meals = null;
			try {
				meals = em.createNamedQuery("User.findMealsInDateAndTimeRange")
							.setParameter("userId", userId)
							.setParameter("startDate", fromDate, TemporalType.DATE)
							.setParameter("endDate", toDate, TemporalType.DATE)
							.setParameter("startTime", fromTime, TemporalType.TIME)
							.setParameter("endTime", toTime, TemporalType.TIME)
							.getResultList();
						
			} catch (PersistenceException pe) {
				throw new RepositoryException(
						String.format("Error in find for meals " + formattedInputParameters), 
						pe);
			}
			logger.info("Finished finding Meals " + formattedInputParameters + getWrapupMsg(startTime));
			return meals;
		} catch (RepositoryException e) {
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
		
	}

	private void validateFindUserMealsInDateAndTimeRangesParms(Integer userId, Date fromDate, Date toDate, Date fromTime, Date toTime) 
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
}
