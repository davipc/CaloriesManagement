package com.toptal.calories.rest;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.repository.Meals;
import com.toptal.calories.resources.repository.RepositoryException;
import com.toptal.calories.resources.repository.RepositoryFactory;

@Path("/meals")
public class MealService {
	
	private Logger logger = LoggerFactory.getLogger(MealService.class);

	private Meals meals = new RepositoryFactory().createRepository(Meals.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Meal getMeal(@PathParam("id") int mealId) throws RepositoryException {
		logger.debug("Looking for meal with ID " + mealId); 
		
		if (meals == null) {
			logger.error("Meals ainda e null!!");
		}
		
		Meal meal = meals.find(mealId);
		
		if (meal == null) {
			logger.debug("No meals found for meal with ID " + mealId);
	        throw new WebApplicationException(Status.NOT_FOUND);
	    }

		logger.debug("Meal found for ID " + mealId);
		return meal;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void createMeal(Meal meal) throws RepositoryException {
		logger.debug("Persisting meal " + meal); 
		
		meal = meals.createOrUpdate(meal);
		
		if (meal == null) {
			logger.debug("Error persisting meal " + meal);
	        throw new WebApplicationException(Status.NOT_FOUND);
	    }

		logger.debug("Finished persisting " + meal);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateMeal(Meal meal) throws RepositoryException {
		logger.debug("Updating meal " + meal); 
		
		meal = meals.createOrUpdate(meal);
		
		if (meal == null) {
			logger.debug("Error updating meal " + meal);
	        throw new WebApplicationException(Status.NOT_FOUND);
	    }

		logger.debug("Finished updating " + meal);
	}
	
	@DELETE
	@Path("{id}")
	public void deleteMeal(@PathParam("id") int mealId) throws RepositoryException {
		logger.debug("Deleting meal with ID " + mealId); 
		
		boolean removed = false;
		try {
			removed = meals.remove(mealId);
		} catch (RepositoryException re) {
			logger.debug("Error deleting meal with ID " + mealId);
			throw re;
		}
		
		logger.debug("Finished deleting meal with ID " + mealId + ": " + (removed ? "successfully deleted": "did not exist"));
	}
	
	// TODO: optional: JPA could be changed so users include meals and this is a user related api 
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("user/{userId}")
	public List<Meal> getMealsFromUser(@PathParam("userId") int userId) throws RepositoryException {
		logger.debug("Looking for meals from user " + userId); 
		
		List<Meal> mealsFromUser = meals.findUserMeals(userId);
		
		if (mealsFromUser == null) {
			logger.debug("No meals found for user with ID " + userId);
	        throw new WebApplicationException(Status.NOT_FOUND);
	    }

		logger.debug("Meals found for user with ID " + userId + ": "  + mealsFromUser.size());
		return mealsFromUser;
	}
	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("user/{userId}/{fromDate}/{toDate}/{fromTime}/{toTime}")
	public List<Meal> getMealsFromUser(@PathParam("userId") int userId, 
										@PathParam("fromDate") Date fromDate, @PathParam("toDate") Date toDate, 
										@PathParam("fromTime") Date fromTime, @PathParam("toTime") Date toTime) 
	throws RepositoryException {
		String formattedString = String.format(" from user %s and in date range %tF to %tF and time range %tR to %tR", userId, fromDate, toDate, fromTime, toTime);
		
		logger.debug("Looking for meals " + formattedString); 
		
		List<Meal> mealsFromUser = meals.findUserMealsInDateAndTimeRanges(userId, fromDate, toDate, fromTime, toTime);
		
		if (mealsFromUser == null) {
			logger.debug("No meals found " + formattedString);
	        throw new WebApplicationException(Status.NOT_FOUND);
	    }

		logger.debug("Meals found " + formattedString + ": "  + mealsFromUser.size());
		return mealsFromUser;
	}
	
	
}
