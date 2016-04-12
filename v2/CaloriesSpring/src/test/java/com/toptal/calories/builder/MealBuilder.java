package com.toptal.calories.builder;

import java.util.Date;

import com.toptal.calories.entity.Meal;
import com.toptal.calories.entity.User;

public class MealBuilder {
	private Meal meal = new Meal();

	public MealBuilder() {}
	
	public MealBuilder(Meal inMeal) {
		this.id(inMeal.getId())
			.user(inMeal.getUser())
			.mealDate(inMeal.getMealDate())
			.mealTime(inMeal.getMealTime())
			.description(inMeal.getDescription())
			.calories(inMeal.getCalories());
	}
	
	public MealBuilder id(Integer id) {
		meal.setId(id);
		return this;
	}

	public MealBuilder user(User user) {
		meal.setUser(user);
		return this;
	}

	public MealBuilder mealDate(Date mealDate) {
		meal.setMealDate(mealDate);
		return this;
	}

	public MealBuilder mealTime(Date mealTime) {
		meal.setMealTime(mealTime);
		return this;
	}

	public MealBuilder description(String description) {
		meal.setDescription(description);
		return this;
	}
	
	public MealBuilder calories(Integer calories) {
		meal.setCalories(calories);
		return this;
	}
	
	public Meal build() {
		return meal;
	}
}
