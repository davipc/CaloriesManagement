package com.toptal.calories.resources.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.entity.User;
import com.toptal.calories.resources.util.LogUtil;


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

			LogUtil.logEnd(logger, "finding Meals " + formattedInputParameters, startTime);
			
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
	
	/**
	 * Finds and returns the User associated with the input login.
	 * @param login The user login.
	 * @return The User object associated with the input login. If no user object
	 * is associated to the input login, null is returned.
	 */
	public User findByLogin(String login) throws RepositoryException {
		logger.info("Finding user with login " + login);
		long startTime = System.currentTimeMillis();
		try {
			if (login == null) {
				throw new RepositoryException("Null login!!");
			} else {
				User user = null;
				try {
					user = (User) em.createNamedQuery("User.findByLogin")
								.setParameter("login", login)
								.getSingleResult();
							
				} catch (NoResultException nre) {
					// not really an error - it's just that there is no customer by that attribute value
					// leave customer = null
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error in find for user with login " + login, pe);
				}

				LogUtil.logEnd(logger, "finding user with login " + login, startTime);
				return user;
			}
		} catch (RepositoryException e) {
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
		
	}
	
	/**
	 * Used to update the user but not the password field if that one is not provided (null).
	 * This is needed because an admin could be updating a user profile, but he wouldn't be able to know the password,
	 * and it would be annoying for the user if he had to reset his password everytime an admin changed something on his profile.
	 * 
	 * @param user The user to be updated
	 * @throws RepositoryException If the input user is null, or any DB error occurs 
	 */
	public User updateKeepPasswordIfNotProvided(User user) throws RepositoryException {
		User updated = null;
		
		logger.info("Updating user " + (user != null ? user.getId() : "null" )+ " checking for password existence");
		long startTime = System.currentTimeMillis();
		try {
			if (user == null) {
				throw new RepositoryException("Null user!!");
			} else {
				try {
					// regular case, just call regular update method
				    if (user.getPassword() == null) {
						final User existingUser = find(user.getId());

						user.setPassword(existingUser.getPassword());
				    }						
					
				    updated = createOrUpdate(user);
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error updating user " + user.getId() + " checking for password existence", pe);
				}

				LogUtil.logEnd(logger, "updating user " + user + " checking password existence", startTime);
				
				return updated;
			}
		} catch (RepositoryException e) {
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
	}	
}
