package com.toptal.calories.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.toptal.calories.builder.RoleBuilder;
import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.RoleType;
import com.toptal.calories.repository.RoleRepository;
import com.toptal.calories.rest.exceptions.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class TestRoleService {

	@InjectMocks
	private RoleService roleService;
	
	@Mock
	private RoleRepository roleRepository;

	
	private static final Role ROLE1 = new RoleBuilder().id(1).name(RoleType.DEFAULT).build();
	private static final Role ROLE2 = new RoleBuilder().id(2).name(RoleType.MANAGER).build();
	
	
	@Test
	public void testGetRolesNoRolesFound() {
		// setup the mock repository
		given(roleRepository.findAll()).willReturn(new ArrayList<Role>());
		
		// make the service call
		try {
			assertThat(roleService.getRoles()).isEmpty();
		} catch (NotFoundException e) {
			Assertions.fail("Error testing getAllRoles: " + e.getMessage());
		}
	}
	
	@Test
	public void testGetRolesOneRoleFound() {
		// setup the mock repository
		given(roleRepository.findAll()).willReturn(Arrays.asList(new Role[]{ROLE1}));
		
		// make the service call
		try {
			assertThat(roleService.getRoles()).contains(ROLE1);
		} catch (NotFoundException e) {
			Assertions.fail("Error testing getAllRoles: " + e.getMessage());
		}
	}

	@Test
	public void testGetRolesTwoRolesFound() {
		// setup the mock repository
		given(roleRepository.findAll()).willReturn(Arrays.asList(new Role[]{ROLE1, ROLE2}));
		
		// make the service call
		try {
			assertThat(roleService.getRoles()).contains(ROLE1, ROLE2);
		} catch (NotFoundException e) {
			Assertions.fail("Error testing getAllRoles: " + e.getMessage());
		}
	}
	
}
