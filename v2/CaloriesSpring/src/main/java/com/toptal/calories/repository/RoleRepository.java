package com.toptal.calories.repository;

import org.springframework.data.repository.CrudRepository;

import com.toptal.calories.entity.Role;

public interface RoleRepository extends CrudRepository<Role, Integer> {
}
