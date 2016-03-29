package com.toptal.calories.rest;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.toptal.calories.entity.Meal;
import com.toptal.calories.entity.User;
import com.toptal.calories.repository.UserRepository;
import com.toptal.calories.rest.exceptions.NotFoundException;
import com.toptal.calories.rest.util.EncryptionHelper;
import com.toptal.calories.rest.util.RestUtil;

@Controller
@RequestMapping("/api/v2/users")
public class UserService extends ExceptionAwareService {

	private static Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	UserRepository repository; 

	@PreAuthorize ("hasRole('ROLE_ADMIN') or #id == principal.id")
	@RequestMapping(value="{id}", method=RequestMethod.GET)
	public @ResponseBody User getUser(@PathVariable int id, HttpServletResponse response) 
	throws NotFoundException {
		logger.debug("Looking for user with ID " + id); 
		
		User user = repository.findOne(id);
		
		if (user == null) {
			String msg = "No users found with ID " + id; 
			logger.debug(msg);
			throw new NotFoundException(msg);
		} 

		logger.debug("User found for ID " + id + ": " + user);
		
		return user;
	}
	
	@PreAuthorize ("hasRole('ROLE_ADMIN')")
	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody List<User> getAllUsers() 
	throws NotFoundException {
		List<User> users = null;
		
		logger.debug("Fetching all users"); 
		
		users = RestUtil.makeList(repository.findAll());

		logger.debug("Users found: " + users.size());
		
		return users;
	}

	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody User createUser(@RequestBody User user, HttpServletResponse response) {
		logger.debug("Persisting user " + user); 

		String validationResult = user.validate();
		if (validationResult != null) {
			throw new IllegalArgumentException(validationResult);
		}
		
		// also need to validate user ID - must be null
		if (user.getId() != null) {
			String msg = "User ID should be null in POST request: " + user;
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}
		
		// we will assume if password is provided it comes not encrypted
		if (user.getPassword() != null)
			user.setPassword(new EncryptionHelper().encrypt(user.getPassword()));
		
		try {
			// repository will set the ID to the input user
			repository.save(user);
		} catch (DataIntegrityViolationException e) {
			// login already used - that's the only constraint not validated
			String msg = "Error creating user: login already used: " + user.getLogin();
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}
		
		response.setStatus(HttpServletResponse.SC_CREATED);
		
	    logger.debug("Finished persisting " + user);
		
		// need to return so JSON will be returned (with the new ID)
		return user;
	}
	
	/**
	 * Updates the user.
	 * If the password is not provided, the current one is kept.
	 * @param user The user to be updated
	 * @param response 
	 * @return
	 */
	@PreAuthorize ("hasRole('ROLE_ADMIN') or #user.id == principal.id")
	@RequestMapping(method=RequestMethod.PUT)
	public @ResponseBody User updateUser(@RequestBody User user, HttpServletResponse response) 
	throws NotFoundException {
		logger.debug("Updating user " + user); 

		User userUpdated = null;
		
		String validationResult = user.validate();
		if (validationResult != null) {
			throw new IllegalArgumentException(validationResult);
		}
		
		// also need to validate user ID - must NOT be null
		if (user.getId() == null) {
			String msg = "User ID should NOT be null in PUT request: " + user;
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}
		
		User currentUser = repository.findOne(user.getId());

		// if user is not in the DB return error (supposed to exist in an update operation)
		if (currentUser == null) {
			String msg = "User with ID " + user.getId() + " not found for update";
			logger.error(msg);
			throw new NotFoundException(msg);
		} 

		// we will assume if password is provided it comes not encrypted
		if (user.getPassword() != null) {
			user.setPassword(new EncryptionHelper().encrypt(user.getPassword()));
		} 
		// if it is not provided, the existing one will be kept (front will handle policy on password update during profile update)
		else {
			user.setPassword(currentUser.getPassword());
		}
			
		try {
			userUpdated = repository.save(user);
		} catch (DataIntegrityViolationException e) {
			// login already used - that's the only constraint not validated
			String msg = "Error updating user: login already used: " + user.getLogin();
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}

		if (userUpdated == null) {
			String msg = "Error updating user " + user + ": no user returned from update";
			logger.error(msg);
		    throw new NotFoundException(msg);
	    }

		//set HTTP code to "200 OK" since we are returning content
	    response.setStatus(HttpServletResponse.SC_OK);
	    
		logger.debug("Finished updating " + user);
		
		return userUpdated;
	}

	@PreAuthorize ("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="{id}", method=RequestMethod.DELETE)
	public void deleteUser(@PathVariable int id, HttpServletResponse response) 
	throws NotFoundException {
		logger.debug("Deleting user with ID " + id); 
		
		try {
			repository.delete(id);
		} catch (EmptyResultDataAccessException e) {
			String msg = "User with ID " + id + " not found for delete";
			logger.debug(msg, e);
			throw new NotFoundException(msg);
		}

		//set HTTP code to "204 NO CONTENT" since no content is returned
	    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		
		logger.debug("Finished deleting user with ID " + id);
	}
	
	@PreAuthorize ("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or #id == principal.id")
	@RequestMapping(value="{userId}/meals", method=RequestMethod.GET)
	public @ResponseBody List<Meal> getMealsFromUser(@PathVariable int userId, 
		@RequestParam(required=false, name="fromDate") String fromDate, @RequestParam(required=false, name="toDate") String  toDate, 
		@RequestParam(required=false, name="fromTime") String fromTime, @RequestParam(required=false, name="toTime") String toTime,
		HttpServletResponse response) 
	throws NotFoundException {
		
		List<Meal> mealsFromUser = null;
		
		// if no query parameters provided, just get all the user's meals 
		if (fromDate == null && toDate == null && fromTime == null && toTime == null) {
			mealsFromUser = getMealsFromUser(userId, response);	
		} 
		// otherwise, use the fields to perform a narrower query (more efficient on the DB than fetching all meals from user then filtering here)
		else {
			Date fromD = RestUtil.getDateFromJSON(fromDate, RestUtil.DEFAULT_DATE_MIN);
			Date toD = RestUtil.getDateFromJSON(toDate, RestUtil.DEFAULT_DATE_MAX);
			Date fromT = RestUtil.getTimeFromJSON(fromTime, RestUtil.DEFAULT_TIME_MIN);
			Date toT = RestUtil.getTimeFromJSON(toTime, RestUtil.DEFAULT_TIME_MAX);
			
			mealsFromUser = getMealsFromUser(userId, fromD, toD, fromT, toT, response);
		}
		
		return mealsFromUser;
	}
	
	private List<Meal> getMealsFromUser(int userId, HttpServletResponse response) 
	throws NotFoundException {
		logger.debug("Looking for meals from user " + userId); 
		
		User user = repository.findOne(userId);
		List<Meal> mealsFromUser = null;
		if (user == null) {
			String msg = "No user found with ID " + userId;
			logger.debug(msg);
			throw new NotFoundException(msg);
	    } 

		logger.debug("User found for ID " + userId);
		
		mealsFromUser = user.getMeals();

    	logger.debug("Meals found for user with ID " + userId + ": "  + mealsFromUser.size());
		
		return mealsFromUser;
	}
	
	private List<Meal> getMealsFromUser(int userId, Date fromDate, Date toDate, Date fromTime, Date toTime, HttpServletResponse response) 
	throws NotFoundException {
		String formattedString = String.format(" from user %s and in date range %tF to %tF and time range %tR to %tR", userId, fromDate, toDate, fromTime, toTime);

		logger.debug("Looking for meals " + formattedString); 
		
		List<Meal> mealsFromUser = repository.findMealsInDateAndTimeRange(userId, fromDate, toDate, fromTime, toTime);
		
		logger.debug("Meals found " + formattedString + ": "  + mealsFromUser.size());

		return mealsFromUser;
	}
}
