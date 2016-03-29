package com.toptal.calories.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.toptal.calories.constants.RestPaths;
import com.toptal.calories.entity.Gender;
import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.User;

@Component
public class RESTBasedAuthenticationProvider implements AuthenticationProvider {

	private static Logger logger = LoggerFactory.getLogger(RESTBasedAuthenticationProvider.class); 
	
    @Override
    public Authentication authenticate(Authentication authentication)
	throws AuthenticationException {
        Authentication result = null; 
    	String name = authentication.getName();

        logger.debug("Authenticating user " + name);
        // You can get the password here
        String password = authentication.getCredentials().toString();

        // call authentication method
        User authenticatedUser = callCustomAuthenticationMethod(name, password);

        // if authenticated (and thus returned a user), create the return object with the credentials + roles  
        if (authenticatedUser != null) {
        	
        	List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(getRoleNames(authenticatedUser));
        	
        	result = new UsernamePasswordAuthenticationToken(authenticatedUser, password, authorities);
        }

        logger.debug("Finished authenticating user " + name);
        
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    /**
     * Calls the authentication REST API
     * @param username
     * @param password
     * @return
     */
    private User callCustomAuthenticationMethod(String username, String password) {
    	User user = null;
    	
    	User loginUser = new User();
    	loginUser.setLogin(username);
    	loginUser.setPassword(password);
    	
    	RestTemplate restTemplate = new RestTemplate();
    	
    	URI endpoint = null;
    	
    	try {
			// TODO: the REST API URL would be set into a properties file in a real world application 
    		endpoint = new URI("http://localhost:8080/CaloriesSpring" + RestPaths.AUTH);
		} catch (URISyntaxException e) {
			// nothing to do if the URL is wrong
			logger.error("Bad Auth service URI", e);
			return null;
		}
    	
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);    	
    	headers.setAccept(Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON}));
    	
    	HttpEntity<User> entity = new HttpEntity<User>(loginUser,headers);
    	
    	try {
	    	ResponseEntity<User> response = restTemplate.postForEntity(endpoint, entity, User.class);
	    	
	    	user = response.getBody();
	    	
	    	if (response.getStatusCode().value() != HttpServletResponse.SC_OK) {
	    		logger.warn("Login attempt for user " + username + " failed with status " + response.getStatusCode());
	    	}
    	} catch (HttpClientErrorException e) {
    		logger.warn("Error authenticating user " + username + ": " + e.getMessage());
    	}
    	return user;
    }
    
    private static String[] getRoleNames (User user) {
    	String[] result = new String[0];
    	
    	if (user != null && user.getRoles() != null && user.getRoles().size() > 0) {
    		result = new String[user.getRoles().size()];
    		int i = 0;
    		for (Role role: user.getRoles()) {
    			result[i++] = "ROLE_" + role.getName().name();
    		}
    	}
    	
    	return result;
    }
    
}