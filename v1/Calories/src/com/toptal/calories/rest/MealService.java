package com.toptal.calories.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.repository.Meals;
import com.toptal.calories.resources.repository.RepositoryException;
import com.toptal.calories.resources.repository.RepositoryFactory;

@Path("/meals")
public class MealService {
	
	private Logger logger = LoggerFactory.getLogger(MealService.class);

	@Context
	private HttpServletRequest httpRequest;	
	
	private Meals meals = new RepositoryFactory().createRepository(Meals.class, httpRequest);

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Meal getMeal(@PathParam("id") int mealId) throws RepositoryException {
		logger.debug("Looking for meal with ID " + mealId); 
		
		// TODO: FOR DEBUGGING, REMOVE IT
		if (meals == null) {
			logger.error("Meals ainda e null!!");
		}
		// TODO: END OF FOR DEBUGGING, REMOVE IT

		Meal meal = meals.find(mealId);
		
		if (meal == null) {
			logger.debug("No meals found for meal with ID " + mealId);
	        throw new NotFoundException();
	    }

		logger.debug("Meal found for ID " + mealId);
		return meal;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Meal createMeal(Meal meal) throws RepositoryException {
		logger.debug("Persisting meal " + meal); 
		
		Meal createdMeal = meals.createOrUpdate(meal);
		
		if (createdMeal == null) {
			logger.debug("Error persisting meal " + meal);
	        throw new NotFoundException();
	    }

		logger.debug("Finished persisting meal " + meal);
		
		return createdMeal;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Meal updateMeal(Meal meal) throws RepositoryException {
		logger.debug("Updating meal " + meal); 
		
		Meal updatedMeal = meals.createOrUpdate(meal);
		
		if (updatedMeal == null) {
			logger.debug("Error updating meal " + meal);
	        throw new NotFoundException();
	    }

		logger.debug("Finished updating meal " + meal);
		
		return updatedMeal;
	}
	
	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean deleteMeal(@PathParam("id") int mealId) throws RepositoryException {
		logger.debug("Deleting meal with ID " + mealId); 
		
		boolean removed = false;
		try {
			removed = meals.remove(mealId);
		} catch (RepositoryException re) {
			logger.debug("Error deleting meal with ID " + mealId);
			throw re;
		}
		
		logger.debug("Finished deleting meal with ID " + mealId + ": " + (removed ? "successfully deleted": "did not exist"));
		
		return removed;
	}
}
