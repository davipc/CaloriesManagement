package com.toptal.calories.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.toptal.calories.entity.User;
import com.toptal.calories.repository.UserRepository;
import com.toptal.calories.rest.exceptions.NotFoundException;
import com.toptal.calories.rest.util.EncryptionHelper;

@Controller
@RequestMapping("/api/v2/auth")
public class AuthService extends ExceptionAwareService {

	private static Logger logger = LoggerFactory.getLogger(AuthService.class);
	
	@Autowired
	UserRepository repository; 

	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody User authenticateUser(@RequestBody User user, HttpServletResponse response) 
	throws NotFoundException {
		logger.debug("Authenticating user " + user.getLogin()); 

		boolean authenticated = false;
		User foundUser = null;
		
		if (user == null || user.getLogin() == null || user.getPassword() == null) {
			String msg = "Missing login and/or password in the request";
			logger.warn(msg);
			throw new IllegalArgumentException(msg);
		} 

		
		// NOTE: incoming user comes with only login and password fields set

		// we will fetch the user back, so if in future we want to add some "block after X attempts" feature we already have the user here to update 
		foundUser = repository.findByLogin(user.getLogin());
		
		if (foundUser == null) {
			// we will return this response (and message), but the front end can inform the user the more secure message: "Invalid login/password" 
			// for both not found and not not authorized codes
			String msg = "No users found with login " + user.getLogin();
			logger.info(msg);
			try {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, msg);
			} catch (IOException ioe) {
				logger.warn("Error returning response error " + HttpServletResponse.SC_UNAUTHORIZED, ioe);
			}
	    } else {

			// compare passwords (provided vs stored)
			String providedPwdEnc = new EncryptionHelper().encrypt(user.getPassword());
			
			authenticated = providedPwdEnc.equals(foundUser.getPassword());
			
			// we will return this response (and message), but the front end can inform the user the more secure message: "Invalid login/password" 
			// for both not found and not not authorized codes
			if (!authenticated) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				foundUser = null;
			} else {
				//set HTTP code to "200 OK" since we are returning content
			    response.setStatus(HttpServletResponse.SC_OK);
			}
		}
		
		logger.info("User " + user.getLogin() + " authentication "  + (authenticated ? "succeeded!" : "failed!" ));
		
		return foundUser;
	}
	
}
