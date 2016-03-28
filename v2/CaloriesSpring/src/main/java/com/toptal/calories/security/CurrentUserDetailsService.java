package com.toptal.calories.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.toptal.calories.entity.User;
import com.toptal.calories.rest.UserService;

@Service
public class CurrentUserDetailsService implements UserDetailsService {

	private static Logger logger = LoggerFactory.getLogger(CurrentUserDetailsService.class); 
	
	private final UserService userService;
    
    @Autowired
    public CurrentUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    public CurrentUser loadUserByUsername(String username) throws UsernameNotFoundException {

    	//userService.getUserByUsername(username);

    	logger.debug("Started loadUserByUsername");
    	
        User user = new User();
        user.setLogin("test_user");
        user.setPassword("testpwd");
        
    	logger.debug("Finished loadUserByUsername");
        
        return new CurrentUser(user);
    }
}
