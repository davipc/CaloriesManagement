package com.toptal.calories.security;

import java.util.List;

import org.springframework.security.core.authority.AuthorityUtils;

import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.User;

public class CurrentUser extends org.springframework.security.core.userdetails.User {
	private static final long serialVersionUID = -2127103018530374687L;

	private User user;
    public CurrentUser(User user) {
        super(user.getLogin(), user.getPassword(), AuthorityUtils.createAuthorityList(getRoleNames(user)));
        this.user = user;
    }
    public User getUser() {
        return user;
    }

    public List<Role> getRoles() {
        return user.getRoles();
    }
    
    private static String[] getRoleNames (User user) {
    	String[] result = new String[0];
    	
    	if (user.getRoles() != null && user.getRoles().size() > 0) {
    		result = new String[user.getRoles().size()];
    		int i = 0;
    		for (Role role: user.getRoles()) {
    			result[i++] = "ROLE_" + role.getName().name();
    		}
    	}
    	
    	return result;
    }
}
