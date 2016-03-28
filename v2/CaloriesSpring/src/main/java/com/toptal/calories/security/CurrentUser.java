package com.toptal.calories.security;

import org.springframework.security.core.authority.AuthorityUtils;

import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.User;

public class CurrentUser extends org.springframework.security.core.userdetails.User {
    private User user;
    public CurrentUser(User user) {
        super(user.getLogin(), user.getPassword(), AuthorityUtils.createAuthorityList(user.getRoles() != null ? user.getRoles().toArray(new String[]{}) : new String[]{"Default"}));
        this.user = user;
    }
    public User getUser() {
        return user;
    }

    public Role getRole() {
        return user.getRoles().get(0);
    }
}
