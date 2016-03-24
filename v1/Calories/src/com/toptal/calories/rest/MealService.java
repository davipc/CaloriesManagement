package com.toptal.calories.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.repository.Meals;
import com.toptal.calories.resources.repository.RepositoryException;
import com.toptal.calories.resources.repository.RepositoryFactory;

@JsonIgnoreProperties(ignoreUnknown=true)
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
	        throw new NotFoundException();
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
	        throw new NotFoundException();
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
	        throw new NotFoundException();
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
}
