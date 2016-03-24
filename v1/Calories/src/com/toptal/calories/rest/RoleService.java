package com.toptal.calories.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.entity.Role;
import com.toptal.calories.resources.repository.RepositoryException;
import com.toptal.calories.resources.repository.RepositoryFactory;
import com.toptal.calories.resources.repository.Roles;

@Path("/roles")
public class RoleService {
	
	private Logger logger = LoggerFactory.getLogger(RoleService.class);

	@Context
	private HttpServletRequest httpRequest;	

	private Roles roles = new RepositoryFactory().createRepository(Roles.class, httpRequest);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Role> getRoles() throws RepositoryException {
		logger.debug("Getting all roles"); 
		
		List<Role> allRoles = roles.findAll();
		
		if (allRoles == null) {
			logger.debug("No roles found");
	        throw new WebApplicationException(Status.NOT_FOUND);
	    }

		logger.debug("Roles found: " + allRoles.size());
		return allRoles;
	}
}
