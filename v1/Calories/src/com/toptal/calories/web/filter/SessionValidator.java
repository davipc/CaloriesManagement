package com.toptal.calories.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.entity.User;
import com.toptal.calories.web.constants.ApplicationPaths;
import com.toptal.calories.web.constants.RestPaths;
import com.toptal.calories.web.constants.SessionContextParameters;

public class SessionValidator implements Filter {

  private static final Logger log = LoggerFactory.getLogger(SessionValidator.class);	
	
  public void init(FilterConfig arg0) throws ServletException {
  }

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		
		String path = httpRequest.getServletPath() + (httpRequest.getPathInfo() != null ? httpRequest.getPathInfo() : "");
		
		if (!(path.equals(ApplicationPaths.LOGIN_PATH) || path.equals(ApplicationPaths.LOGIN_SERVLET) || path.equals(RestPaths.AUTH_USER) || 
				path.endsWith(".css") || path.endsWith(".js"))) {
			
			User user = null;
			HttpSession session = httpRequest.getSession(false);
			if (session != null) {
				user = (User) session.getAttribute(SessionContextParameters.USER_ATTRIBUTE);
			}
			
			// if session doesn't exist or user is not in session, send to login page
			if (session == null || user == null) {
				log.warn("User not in session, going to " + ApplicationPaths.LOGIN_PATH);
				String targetPath = buildTargetPath(httpRequest, ApplicationPaths.LOGIN_PATH);
	        	
				((HttpServletResponse) response).sendRedirect(targetPath);
				return;
			} 
		}
		chain.doFilter(request, response);
	}

	private String buildTargetPath(HttpServletRequest httpRequest, String targetResource) throws IOException, ServletException {
		String targetPath = null;
		
		if (httpRequest != null) {
			String contextPath = httpRequest.getContextPath();
	    	targetPath = contextPath + targetResource;
		}		
		return targetPath;
	}
	
	
	public void destroy() {
	}
}