package com.toptal.calories.rest;

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

import com.toptal.calories.resources.entity.User;
import com.toptal.calories.resources.repository.RepositoryException;
import com.toptal.calories.resources.repository.RepositoryFactory;
import com.toptal.calories.resources.repository.Users;

@Path("/users")
public class UserService {
	
	private Logger logger = LoggerFactory.getLogger(UserService.class);

	private Users users = new RepositoryFactory().createRepository(Users.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public User getUser(@PathParam("id") int userId) throws RepositoryException {
		logger.debug("Looking for user with ID " + userId); 
		
		if (users == null) {
			logger.error("Users ainda e null!!");
		}
		
		User user = users.find(userId);
		
		if (user == null) {
			logger.debug("No users found for user with ID " + userId);
	        throw new WebApplicationException(Status.NOT_FOUND);
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
	public void createUser(User user) throws RepositoryException {
		logger.debug("Persisting user " + user); 
		
		user = users.createOrUpdate(user);
		
		if (user == null) {
			logger.debug("Error persisting user " + user);
	        throw new WebApplicationException(Status.NOT_FOUND);
	    }

		logger.debug("Finished persisting " + user);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUser(User user) throws RepositoryException {
		logger.debug("Updating user " + user); 
		
		user = users.createOrUpdate(user);
		
		if (user == null) {
			logger.debug("Error updating user " + user);
	        throw new WebApplicationException(Status.NOT_FOUND);
	    }

		logger.debug("Finished updating " + user);
	}
	
	@DELETE
	@Path("{id}")
	public void deleteUser(@PathParam("id") int userId) throws RepositoryException {
		logger.debug("Deleting user with ID " + userId); 
		
		try {
			users.remove(userId);
		} catch (RepositoryException re) {
			logger.debug("Error deleting user with ID " + userId);
			throw re;
		}
		
		logger.debug("Finished deleting user with ID " + userId);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("auth/{userId}")
	public User authenticateUser(@PathParam("userId") int userId) throws RepositoryException {
		logger.debug("Authenticating user " + userId); 
		
		User user = users.find(userId);
		
		if (user == null) {
			logger.debug("No users found for user with ID " + userId);
	        throw new WebApplicationException(Status.NOT_FOUND);
	    }

		// compare passwords (provided vs stored)
		boolean authenticated = false;

		
		
		logger.debug("User " + userId + " authentication "  + (authenticated ? "succeeded!" : "failed!" ));
		return user;
	}
}
