package com.toptal.calories.app;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.util.UrlPathHelper;

import com.toptal.calories.constants.RestPaths;

public class RestAwareAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

	private static Logger logger = LoggerFactory.getLogger(RestAwareAuthenticationEntryPoint.class); 
	
	public RestAwareAuthenticationEntryPoint(String loginUrl) {
        super(loginUrl);
    }
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {
		
    	String resource = new UrlPathHelper().getPathWithinApplication(request); 
		boolean apiReq = resource.startsWith(RestPaths.REST_BASE_URI);
		
		logger.debug("Received authentication failure error for resource \"" + resource + "\": " + authException.getMessage());
		
		if (apiReq) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Service requires authenticated user");
		} else {
			super.commence(request, response, authException);
		}
	}
	
}
