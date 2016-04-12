package com.toptal.calories.app;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import com.toptal.calories.constants.RestPaths;
import com.toptal.calories.constants.WebResources;

@Component
public class RestAwareAccessDeniedHandler implements AccessDeniedHandler {
 
	private static Logger logger = LoggerFactory.getLogger(RestAwareAccessDeniedHandler.class); 
	
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) 
    throws IOException, ServletException {
        
    	String resource = new UrlPathHelper().getPathWithinApplication(request); 
		boolean apiReq = resource.startsWith(RestPaths.REST_BASE_URI);
		
		logger.debug("Received access denied error for resource \"" + resource + "\": " + ex.getMessage());
		
		if (apiReq) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} else {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			// need to ignore the starting "/" for redirects
	    	response.sendRedirect(WebResources.ACCESS_DENIED_PAGE.substring(1));
		}
    }
}