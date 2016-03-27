package com.toptal.calories.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.toptal.calories.entity.Meal;
import com.toptal.calories.entity.User;

public interface UserRepository extends CrudRepository<User, Integer> {
	
	public List<Meal> findMealsInDateAndTimeRange(@Param("userId") int userId, 
			@Param("fromDate") Date fromDate, @Param("toDate") Date  toDate, 
			@Param("fromTime") Date fromTime, @Param("toTime") Date toTime); 
	
	public User findByLogin(@Param("login") String login);
}
