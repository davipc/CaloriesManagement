package com.toptal.calories.rest;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.toptal.calories.entity.Meal;
import com.toptal.calories.repository.MealRepository;
import com.toptal.calories.rest.exceptions.NotFoundException;

@Controller
@RequestMapping("/api/v2/meals")
public class MealService extends ExceptionAwareService {

	private static Logger logger = LoggerFactory.getLogger(MealService.class);
	
	@Autowired
	MealRepository repository; 

	@RequestMapping(value="{id}", method=RequestMethod.GET)
	public @ResponseBody Meal getMeal(@PathVariable int mealId, HttpServletResponse response) 
	throws NotFoundException {
		logger.debug("Looking for meal with ID " + mealId); 
		
		Meal meal = repository.findOne(mealId);
		
		if (meal == null) {
			String msg = "No meals found for ID " + mealId;
			logger.debug(msg);
			throw new NotFoundException(msg);
		}

		logger.debug("Meal found for ID " + mealId);
		return meal;
	}

	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody Meal createMeal(@RequestBody Meal meal, HttpServletResponse response) 
	throws NotFoundException {
		logger.debug("Persisting meal " + meal); 
		
		String validationResult = meal.validate();
		if (validationResult != null) {
			throw new IllegalArgumentException(validationResult);
		}
		
		// also need to validate meal ID - must be null
		if (meal.getId() != null) {
			String msg = "Meal ID should be null in POST request: " + meal;
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}
		
		Meal createdMeal = null;
		
		try {
			createdMeal = repository.save(meal);
		} catch (DataIntegrityViolationException e) {
			String msg = "Failed creating meal: either user ID is invalid or there is already a meal for the informed date and time";
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}
		
		if (createdMeal == null) {
			String msg = "Error persisting meal " + meal + ": meal not returned from persist";
			logger.error(msg);
			throw new NotFoundException(msg);
	    } 

		//set HTTP code to "201 Created"
	    response.setStatus(HttpServletResponse.SC_CREATED);
		
		logger.debug("Finished persisting meal " + meal);
		
		return createdMeal;
	}

	@RequestMapping(method=RequestMethod.PUT)
	public @ResponseBody Meal updateMeal(@RequestBody Meal meal, HttpServletResponse response) 
	throws NotFoundException {
		logger.debug("Updating meal " + meal); 
		
		String validationResult = meal.validate();
		if (validationResult != null) {
			throw new IllegalArgumentException(validationResult);
		}
		
		// also need to validate meal ID - must NOT be null
		if (meal.getId() == null) {
			String msg = "Meal ID should NOT be null in PUT request: " + meal;
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}
		
		Meal currentMeal = repository.findOne(meal.getId());

		// if user is not in the DB return error (supposed to exist in an update operation)
		if (currentMeal== null) {
			String msg = "Meal with ID " + meal.getId() + " not found for update";
			logger.error(msg);
			throw new NotFoundException(msg);
		} 		
		
		Meal updatedMeal = null;
		
		try {
			updatedMeal = repository.save(meal);
		} catch (DataIntegrityViolationException e) {
			String msg = "Failed updating meal: either user ID is invalid or there is already another meal for the informed date and time";
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}
		
		if (updatedMeal == null) {
			String msg = "Error updating meal " + meal + ": meal not returned from update";
			logger.error(msg);
			throw new NotFoundException(msg);
	    } 

		//set HTTP code to "200 OK" since we are returning content
	    response.setStatus(HttpServletResponse.SC_OK);
		
		logger.debug("Finished updating meal " + meal);

		return updatedMeal;
	}
	
	@RequestMapping(value="{id}", method=RequestMethod.DELETE)
	public void deleteMeal(@PathVariable int id, HttpServletResponse response) 
	throws NotFoundException {
		logger.debug("Deleting meal with ID " + id); 
		
		try {
			repository.delete(id);
		} catch (EmptyResultDataAccessException e) {
			String msg = "Meal with ID " + id + " not found for delete";
			logger.debug(msg, e);
			throw new NotFoundException(msg);
		}
		
		//set HTTP code to "204 NO CONTENT" since no content is returned
	    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		
		logger.debug("Finished deleting meal with ID " + id);
	}
	
}
