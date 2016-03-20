package com.toptal.calories.resources.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

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
	
}
