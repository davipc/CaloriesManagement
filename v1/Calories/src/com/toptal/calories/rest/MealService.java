package com.toptal.calories.rest;

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

import com.toptal.calories.resources.RepositoryException;
import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.repository.Meals;

@Path("/meals")
public class MealService {
	
	private Logger logger = LoggerFactory.getLogger(MealService.class);

	//@Inject
	private Meals meals = new Meals();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Meal getMeal(@PathParam("id") int mealId) throws RepositoryException {
		logger.debug("Looking for meal with ID " + mealId); 
		
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
		
		try {
			meals.remove(mealId);
		} catch (RepositoryException re) {
			logger.debug("Error deleting meal with ID " + mealId);
			throw re;
		}
		
		logger.debug("Finished deleting meal with ID " + mealId);
	}
	
}
