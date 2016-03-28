package com.toptal.calories.rest;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.toptal.calories.entity.Role;
import com.toptal.calories.repository.RoleRepository;
import com.toptal.calories.rest.exceptions.NotFoundException;
import com.toptal.calories.rest.util.RestUtil;

@Controller
@RequestMapping("/api/v2/roles")
public class RoleService extends ExceptionAwareService {

	private static Logger logger = LoggerFactory.getLogger(RoleService.class);
	
	@Autowired
	RoleRepository repository; 

	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody List<Role> getRoles(HttpServletResponse response) 
	throws NotFoundException {
		logger.debug("Fetching all roles"); 
		
		List<Role> roles = RestUtil.makeList(repository.findAll());
		
		logger.debug("Roles found: " + roles.size());
		return roles;
	}
}
