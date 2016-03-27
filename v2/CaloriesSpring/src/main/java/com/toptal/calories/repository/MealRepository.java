package com.toptal.calories.repository;

import org.springframework.data.repository.CrudRepository;

import com.toptal.calories.entity.Meal;

public interface MealRepository extends CrudRepository<Meal, Integer> {
}
