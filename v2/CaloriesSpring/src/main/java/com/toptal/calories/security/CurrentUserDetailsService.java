package com.toptal.calories.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.toptal.calories.entity.User;

/**
 * Used to fetch user information from a third party system.
 * The password must come back in raw format, as spring security will use it to compare to the password provided in the form.
 * 
 * We CANNOT use this to perform authentication through a REST API, since we would not have access to the form provided 
 * password: thus, we cannot even do the authentication in code, then force spring to authenticate by setting  the expected 
 * password and returning to it.   
 *  
 * @author Davi
 *
 */

@Service
public class CurrentUserDetailsService implements UserDetailsService {

	private static Logger logger = LoggerFactory.getLogger(CurrentUserDetailsService.class); 
	
    public CurrentUser loadUserByUsername(String username) throws UsernameNotFoundException {

    	logger.debug("Started loadUserByUsername");

    	// fetch the user from the external repository here... 
    	// it will be used by spring security to perform the authentication (with the password stored)
    	
    	// failed login scenario - only login is set, password takes a bad value (some a user would never type)
        User user = new User();
        user.setLogin(username);
        user.setPassword("");

        // passed authentication scenario - all fields are set (CANT REALLY DO IT, SINCE WE DONT HAVE ACCESS TO THE FORM PROVIDED PASSWORD)
//        user.setPassword("test");
//        Role role = new Role();
//        role.setName(RoleType.DEFAULT);
//        user.setRoles(Arrays.asList(new Role[]{role}));
        
    	logger.debug("Finished loadUserByUsername");
        
        return new CurrentUser(user);
    }
}
