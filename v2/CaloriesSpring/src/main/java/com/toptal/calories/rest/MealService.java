package com.toptal.calories.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.toptal.calories.entity.Meal;
import com.toptal.calories.entity.RoleType;
import com.toptal.calories.entity.User;
import com.toptal.calories.repository.MealRepository;
import com.toptal.calories.rest.exceptions.ForbiddenException;
import com.toptal.calories.rest.exceptions.NotFoundException;

@Controller
@RequestMapping("/api/v2/meals")
public class MealService extends ExceptionAwareService {

	private static Logger logger = LoggerFactory.getLogger(MealService.class);
	
	@Autowired
	MealRepository repository; 

	// user can only fetch meals if special user or if meal is his
	@PostAuthorize ("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or isAuthenticated() and returnObject.user.id == principal.id")
	@RequestMapping(value="{id}", method=RequestMethod.GET)
	public @ResponseBody Meal getMeal(@PathVariable int id) 
	throws NotFoundException {
		logger.debug("Looking for meal with ID " + id); 
		
		Meal meal = repository.findOne(id);
		if (meal == null) {
			String msg = "No meals found for ID " + id;
			logger.debug(msg);
			throw new NotFoundException(msg);
		}

		logger.debug("Meal found for ID " + id);
		return meal;
	}

	@PreAuthorize ("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or isAuthenticated() and #meal.user.id == principal.id")
	@RequestMapping(method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Meal createMeal(@RequestBody Meal meal) 
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

		logger.debug("Finished persisting meal " + meal);
		
		return createdMeal;
	}

	// Not using @PreAuthorize, doing the check inside, as the caller could have altered the meal so its ID is his own
	//@PreAuthorize ("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or isAuthenticated() and #meal.user.id == principal.id")
	@RequestMapping(method=RequestMethod.PUT)
	// let the service return 200 (OK) since it returns content
	public @ResponseBody Meal updateMeal(@RequestBody Meal meal) 
	throws NotFoundException, ForbiddenException {
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
		
		// then confirm the user calling the API has the privileges to perform the update
		if (!hasPrivilegeOn(meal.getId())) {
			String msg = "No privileges to update meal with ID " + meal.getId();
			logger.warn(msg);
			throw new ForbiddenException(msg);
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

		logger.debug("Finished updating meal " + meal);

		return updatedMeal;
	}
	
	@RequestMapping(value="{id}", method=RequestMethod.DELETE)
	//set HTTP code to "204 NO CONTENT" since no content is returned
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteMeal(@PathVariable int id) 
	throws NotFoundException, ForbiddenException {
		logger.debug("Deleting meal with ID " + id); 

		// first confirm the user calling the API has the privileges to perform the deletion
		if (!hasPrivilegeOn(id)) {
			String msg = "No privileges to delete meal with ID " + id;
			logger.warn(msg);
			throw new ForbiddenException(msg);
		}
		
		// then do the delete
		try {
			repository.delete(id);
		} catch (EmptyResultDataAccessException e) {
			String msg = "Meal with ID " + id + " not found for delete";
			logger.debug(msg, e);
			throw new NotFoundException(msg);
		}
		
		logger.debug("Finished deleting meal with ID " + id);
	}
	
	private boolean hasPrivilegeOn(int mealId) 
	throws NotFoundException {
		boolean result = true;
		
		Meal meal = repository.findOne(mealId);
		if (meal == null) {
			String msg = "Meal with ID " + mealId + " not found";
			logger.debug(msg);
			throw new NotFoundException(msg);
		}
		
		User loggedUser = getLoggedUser();
		// can only delete if user is authenticated and is admin or manager or is the meal owner
		if (loggedUser == null || loggedUser.getId() == null || 
				!(loggedUser.hasRole(RoleType.MANAGER) || loggedUser.hasRole(RoleType.ADMIN) || meal.getUser().getId().equals(loggedUser.getId()))) {

			result = false;
		}
		
		return result;
	}
	
	private User getLoggedUser() {
		User loggedUser = null;
		Authentication loggedUserCreds = SecurityContextHolder.getContext().getAuthentication();
		if (loggedUserCreds != null) {
			loggedUser = (User) loggedUserCreds.getPrincipal();
		}
		
		return loggedUser;
	}
	
}
