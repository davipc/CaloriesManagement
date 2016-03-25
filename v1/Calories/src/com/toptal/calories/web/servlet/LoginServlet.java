package com.toptal.calories.web.servlet;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.toptal.calories.resources.entity.User;
import com.toptal.calories.web.constants.ApplicationPaths;
import com.toptal.calories.web.constants.RestPaths;
import com.toptal.calories.web.constants.SessionContextParameters;
  
public class LoginServlet extends HttpServlet{  
  
    private static final long serialVersionUID = 1L;  

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);	
    
    /**
     * get is not supported, just redirects user to index page if user is already in session, or to login page otherwise
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)    
            throws ServletException, IOException {
        response.setContentType("text/html");    

        HttpSession session = request.getSession(false);  
        if(session != null) {
        	User user = (User) session.getAttribute(SessionContextParameters.USER_ATTRIBUTE);  
        	// if user is not in context, send to login page - otherwise just ignore the request
        	if (user != null) {
        		logger.info("Received a GET request, going to " + ApplicationPaths.INDEX_PATH);
        		RequestDispatcher rd=request.getRequestDispatcher(ApplicationPaths.INDEX_PATH);    
                rd.forward(request,response);    
                return;
        	}
        }

		logger.info("Received a GET request, going to " + ApplicationPaths.LOGIN_PATH);
        RequestDispatcher rd=request.getRequestDispatcher(ApplicationPaths.LOGIN_PATH);
        rd.include(request,response);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)    
            throws ServletException, IOException {    
  
        response.setContentType("text/html");    
          
        String login=request.getParameter("login");    
        String password=request.getParameter("password");   
        
        // make sure we are not working with null for empty passwords
        if (password == null) {
        	password = "";
        }

        String baseURI = request.getRequestURL().toString();
        baseURI = baseURI.substring(0, baseURI.length() - request.getRequestURI().length());
        baseURI += request.getContextPath();
        
        User user = authenticateUser(login, password, baseURI);

        String contextPath = request.getContextPath();
        String targetPath;
        if(user != null) {
            // handle the role groups / setup the roles appropriately before storing into context
        	if (logger.isDebugEnabled()) {
        		logger.debug("Authenticated user: " + user);
        	} else {
        		logger.info("Authenticated user: " + login);
        	}
        	// store the user information on the session context
            HttpSession session = request.getSession(false);  
            if(session!=null) {
            	session.setAttribute(SessionContextParameters.USER_ATTRIBUTE, user);  
            }
            
            // for redirects we need to not use the starting "/"
            targetPath = contextPath + ApplicationPaths.INDEX_PATH;
        }    
        else{
        	logger.warn("Authentication failed for user: " + login);
        	
        	// for redirects we need to not use the starting "/"
        	targetPath = contextPath + ApplicationPaths.LOGIN_PATH + "?loginError=" + URLEncoder.encode("Invalid login or password", "UTF8");
        }    
        
        logger.trace("Going to \"" + targetPath + "\" after authentication");
        response.sendRedirect(targetPath);
    }
    
    private User authenticateUser(String login, String password, String contextPath) {
    	User authUser = null;
    	
    	User inUser = new User();
    	inUser.setLogin(login);
    	inUser.setPassword(password);
    	
    	JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider();
    	ObjectMapper objectMapper = jacksonJsonProvider.locateMapper(User.class, MediaType.APPLICATION_JSON_TYPE);
    	
    	String sentJSON = null;
    	try {
			sentJSON = objectMapper.writeValueAsString(inUser);
			
	    	Client client = JerseyClientBuilder.newBuilder().build().register(jacksonJsonProvider);
	    	WebTarget target = client.target(contextPath);
	    	String response = target.path(RestPaths.AUTH_USER)
	                .request(MediaType.APPLICATION_JSON_TYPE)
	                .post(Entity.json(sentJSON), String.class);
	
			authUser = objectMapper.readValue(response, User.class);
			
			System.out.println(authUser);
		} catch (JsonProcessingException e) {
			logger.error("Error generating User JSON from object: " + e.getMessage(), e);
		} catch (IOException e) {
			logger.error("Error generating User from received JSON : " + e.getMessage(), e);
		} catch (NotFoundException e) {
			logger.error("User not found during authentication: " + login);
		} catch (NotAuthorizedException e) {
			logger.error("Bad password during authentication: " + login);
		}
    	
    	System.out.println("User found: " + authUser);
    	
    	return authUser;
    }
    
    
}   