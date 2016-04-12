package com.toptal.calories.rest;

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
import com.toptal.calories.rest.exceptions.UnauthorizedException;
import com.toptal.calories.rest.util.EncryptionHelper;

@Controller
@RequestMapping("/api/v2/auth")
public class AuthService extends ExceptionAwareService {

	private static Logger logger = LoggerFactory.getLogger(AuthService.class);
	
	@Autowired
	UserRepository repository; 

	@Autowired
	EncryptionHelper encHelper;
	
	@RequestMapping(method=RequestMethod.POST)
	//let HTTP code be "200 OK" since we are returning content
	public @ResponseBody User authenticateUser(@RequestBody User user) 
	throws NotFoundException, UnauthorizedException {
		logger.debug("Authenticating user " + (user == null ? "null" : user.getLogin())); 

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
			// we could handle bad login and password cases differently (locking user after X attempts, etc), but since the service can be called 
			// directly using any rest client (auth service is unsecured ) we will always return the more secure message: "Invalid login/password" 
			logger.info("No users found with login " + user.getLogin());
			throw new UnauthorizedException("Invalid login/password");
	    } else {

			// compare passwords (provided vs stored)
			String providedPwdEnc = encHelper.encrypt(user.getPassword());
			
			authenticated = providedPwdEnc.equals(foundUser.getPassword());
			
			// we could handle bad login and password cases differently (locking user after X attempts, etc), but since the service can be called 
			// directly using any rest client (auth service is unsecured ) we will always return the more secure message: "Invalid login/password" 
			if (!authenticated) {
				logger.info("Bad password provided for user with login " + user.getLogin());
				throw new UnauthorizedException("Invalid login/password");
			}
		}
		
		logger.info("User " + user.getLogin() + " authentication "  + (authenticated ? "succeeded!" : "failed!" ));
		
		return foundUser;
	}
	
}
