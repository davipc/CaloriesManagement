<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>  

<%@page import="org.springframework.security.core.Authentication" %>
<%@page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@page import="com.toptal.calories.entity.Gender" %>
<%@page import="com.toptal.calories.entity.Role" %>
<%@page import="com.toptal.calories.entity.RoleType" %>
<%@page import="com.toptal.calories.entity.User" %>
<%@page import="java.io.BufferedReader" %>

<%

Authentication auth = SecurityContextHolder.getContext().getAuthentication();
User sessionUser = (auth != null) ? (User) auth.getPrincipal() :  null;

//out.println(myUser);

if (sessionUser != null) {
	StringBuffer jb = new StringBuffer();
	String line = null;
	try {
	  	BufferedReader reader = request.getReader();
	  	while ((line = reader.readLine()) != null)
	    	jb.append(line);

	  	//out.println("Received user " + inUser);
	  	
	  	String userJSON = java.net.URLDecoder.decode(jb.toString(), "UTF-8");
	  	ObjectMapper mapper = new ObjectMapper();
	  	User inUser = mapper.readValue(userJSON, User.class);
	  	
	  	// now we compare the 2 users. 
	  	// If same ID, overwrite the security wise "harmless" values of the session object with the received ones
		if (inUser.getId().equals(sessionUser.getId())) {
			// skip id (not safe)
			sessionUser.setLogin(inUser.getLogin());
			sessionUser.setName(inUser.getName());
			// skip password  (not safe)
			sessionUser.setGender(inUser.getGender());
			// skip roles  (not safe)
			// skip meals  (not safe)
			// skip creationDt (it never changes)
			sessionUser.setDailyCalories(inUser.getDailyCalories());
		}
	  	
	} catch (Exception e) { 
		out.println("Error parsing received user: " + e.getMessage());	
	}
} else {
	out.println("User in session was null");
}


%>