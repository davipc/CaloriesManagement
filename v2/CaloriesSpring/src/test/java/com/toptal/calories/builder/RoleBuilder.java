package com.toptal.calories.builder;

import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.RoleType;

public class RoleBuilder {
	private Role role = new Role();
	
	public RoleBuilder id(Integer id) {
		role.setId(id);
		return this;
	}

	public RoleBuilder name(RoleType name) {
		role.setName(name);
		return this;
	}
	
	public Role build() {
		return role;
	}
}
