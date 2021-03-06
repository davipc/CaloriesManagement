package com.toptal.calories.rest;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.entity.User;
import com.toptal.calories.resources.repository.RepositoryException;
import com.toptal.calories.resources.repository.RepositoryFactory;
import com.toptal.calories.resources.repository.Users;
import com.toptal.calories.rest.util.EncryptionHelper;
import com.toptal.calories.rest.util.RestUtil;

@Path("/users")
public class UserService {
	
	private Logger logger = LoggerFactory.getLogger(UserService.class);

	@Context
	private HttpServletRequest httpRequest;	

	@Context
	private HttpServletResponse response;
	
	// TODO: fix second parameter so it is a key shared between all repositories and unique for this request
	private Users users = new RepositoryFactory().createRepository(Users.class, System.currentTimeMillis());

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@PathParam("id") int userId) throws RepositoryException {
		logger.debug("Looking for user with ID " + userId); 
		
		User user = users.find(userId);
		
		if (user == null) {
			logger.debug("No users found for user with ID " + userId);
	        throw new NotFoundException();
	    }

		logger.debug("User found for ID " + userId);
		return user;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getAllUsers() throws RepositoryException {
		logger.debug("Fetching all users"); 
		
		List<User> allUsers = users.findAll();
		
		logger.debug("Users found :" + allUsers.size());
		return allUsers;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User createUser(User user) throws RepositoryException {
		logger.debug("Persisting user " + user); 
		
		// we will assume if password is provided it comes not encrypted
		if (user.getPassword() != null)
			user.setPassword(new EncryptionHelper().encrypt(user.getPassword()));

		User userCreated = users.createOrUpdate(user);
		
		if (userCreated == null) {
			logger.debug("Error persisting user " + user);
	        throw new NotFoundException();
	    }

		//set HTTP code to "201 Created"
	    response.setStatus(HttpServletResponse.SC_CREATED);
	    try {
	        response.flushBuffer();
	    }catch(Exception e){}		

	    logger.debug("Finished persisting " + user);
		
		return userCreated;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User updateUser(User user) throws RepositoryException {
		logger.debug("Updating user " + user); 
		
		// we will assume if password is provided it comes not encrypted
		if (user.getPassword() != null)
			user.setPassword(new EncryptionHelper().encrypt(user.getPassword()));
		
		// if it is not provided, the existing one will be kept (front will handle policy on password update during profile update)
		User userUpdated = users.updateKeepPasswordIfNotProvided(user);
		
		if (userUpdated == null) {
			logger.debug("Error updating user " + user);
	        throw new NotFoundException();
	    }

		//set HTTP code to "200 OK" since we are returning content
	    response.setStatus(HttpServletResponse.SC_OK);
	    try {
	        response.flushBuffer();
	    }catch(Exception e){}		
		
		logger.debug("Finished updating " + user);
		
		return userUpdated;
	}
	
	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean deleteUser(@PathParam("id") int userId) throws RepositoryException {
		logger.debug("Deleting user with ID " + userId); 
		
		boolean removed = false;
		
		try {
			removed = users.remove(userId);
		} catch (RepositoryException re) {
			logger.debug("Error deleting user with ID " + userId);
			throw re;
		}

		//set HTTP code to "200 OK" since we are returning content
	    response.setStatus(HttpServletResponse.SC_OK);
	    try {
	        response.flushBuffer();
	    }catch(Exception e){}		
		
		logger.debug("Finished deleting user with ID " + userId);
		return removed;
	}
	
	@GET
	@Path("{userId}/meals")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Meal> getMealsFromUser(@PathParam("userId") int userId, 
										@QueryParam("fromDate") String fromDate, @QueryParam("toDate") String  toDate, 
										@QueryParam("fromTime") String fromTime, @QueryParam("toTime") String toTime) 
	throws RepositoryException {
		
		List<Meal> mealsFromUser = null;
		
		// if no query parameters provided, just get all the user's meals 
		if (fromDate == null && toDate == null && fromTime == null && toTime == null) {
			mealsFromUser = getMealsFromUser(userId);	
		} 
		// otherwise, use the fields to perform a narrower query (more efficient on the DB than fetching all meals from user then filtering here)
		else {
			Date fromD = RestUtil.getDateFromJSON(fromDate, RestUtil.DEFAULT_DATE_MIN);
			Date toD = RestUtil.getDateFromJSON(toDate, RestUtil.DEFAULT_DATE_MAX);
			Date fromT = RestUtil.getTimeFromJSON(fromTime, RestUtil.DEFAULT_TIME_MIN);
			Date toT = RestUtil.getTimeFromJSON(toTime, RestUtil.DEFAULT_TIME_MAX);
			
			mealsFromUser = getMealsFromUser(userId, fromD, toD, fromT, toT);
		}
		
		return mealsFromUser;
	}
	
	private List<Meal> getMealsFromUser(int userId) throws RepositoryException {
		logger.debug("Looking for meals from user " + userId); 
		
		User user = users.find(userId);
		
		if (user == null) {
			logger.debug("No users found for user with ID " + userId);
	        throw new WebApplicationException(Status.NOT_FOUND);
	    }

		logger.debug("User found for ID " + userId);
		
		List<Meal> mealsFromUser = user.getMeals();
		if (mealsFromUser == null) {
			logger.debug("No meals found for user with ID " + userId);
	        throw new NotFoundException();
	    }

		logger.debug("Meals found for user with ID " + userId + ": "  + mealsFromUser.size());
		return mealsFromUser;
	}
	
	private List<Meal> getMealsFromUser(int userId, Date fromDate, Date toDate, Date fromTime, Date toTime) 
	throws RepositoryException {
		String formattedString = String.format(" from user %s and in date range %tF to %tF and time range %tR to %tR", userId, fromDate, toDate, fromTime, toTime);

		logger.debug("Looking for meals " + formattedString); 
		
		List<Meal> mealsFromUser = users.findMealsInDateAndTimeRanges(userId, fromDate, toDate, fromTime, toTime);
		
		if (mealsFromUser == null) {
			logger.debug("No meals found " + formattedString);
	        throw new NotFoundException();
	    }

		logger.debug("Meals found " + formattedString + ": "  + mealsFromUser.size());

		return mealsFromUser;
	}

	
	@POST
	@Path("auth")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User authenticateUser(User user) throws RepositoryException {
		logger.debug("Authenticating user " + user.getLogin()); 

		if (user == null || user.getLogin() == null || user.getPassword() == null)
			throw new BadRequestException("Missing login and/or password in the request");
		
		// NOTE: incoming user comes with only login and password fields set

		// we will fetch the user back, so if in future we want to add some "block after X attempts" feature we already have the user here to update 
		
		User foundUser = users.findByLogin(user.getLogin());
		
		if (foundUser == null) {
			// we will return this response (and message), but the front end can inform the user the more secure message: "Invalid login/password" 
			// for both not found and not not authorized codes
			logger.info("No users found for user with login " + user.getLogin());
	        throw new NotFoundException();
	    }

		// compare passwords (provided vs stored)
		boolean authenticated = false;
		
		String providedPwdEnc = new EncryptionHelper().encrypt(user.getPassword());
		
		authenticated = providedPwdEnc.equals(foundUser.getPassword());
		
		// we will return this response (and message), but the front end can inform the user the more secure message: "Invalid login/password" 
		// for both not found and not not authorized codes
		if (!authenticated)
			throw new NotAuthorizedException("Invalid password");
		
		//set HTTP code to "200 OK" since we are returning content
	    response.setStatus(HttpServletResponse.SC_OK);
	    try {
	        response.flushBuffer();
	    }catch(Exception e){}		
		
		logger.info("User " + user.getLogin() + " authentication "  + (authenticated ? "succeeded!" : "failed!" ));
		
		return foundUser;
	}
}
