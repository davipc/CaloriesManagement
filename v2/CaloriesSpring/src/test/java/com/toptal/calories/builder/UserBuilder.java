package com.toptal.calories.builder;

import java.sql.Timestamp;
import java.util.List;

import com.toptal.calories.entity.Gender;
import com.toptal.calories.entity.Meal;
import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.User;

public class UserBuilder {
	private User user = new User();
	
	public UserBuilder() {}
	
	public UserBuilder(User inUser) {
		this.id(inUser.getId())
			.login(inUser.getLogin())
			.password(inUser.getPassword())
			.name(inUser.getName())
			.gender(inUser.getGender())
			.dailyCalories(inUser.getDailyCalories())
			.roles(inUser.getRoles())
			.creationDt(inUser.getCreationDt());
	}
	
	public UserBuilder id(Integer id) {
		user.setId(id);
		return this;
	}

	public UserBuilder login(String login) {
		user.setLogin(login);
		return this;
	}

	public UserBuilder password(String password) {
		user.setPassword(password);
		return this;
	}

	public UserBuilder name(String name) {
		user.setName(name);
		return this;
	}

	public UserBuilder gender(Gender gender) {
		user.setGender(gender);
		return this;
	}

	public UserBuilder dailyCalories(Integer dailyCalories) {
		user.setDailyCalories(dailyCalories);
		return this;
	}

	public UserBuilder roles(List<Role> roles) {
		user.setRoles(roles);
		return this;
	}

	public UserBuilder meals(List<Meal> meals) {
		user.setMeals(meals);
		return this;
	}
	
	public UserBuilder creationDt(Timestamp creationDt) {
		user.setCreationDt(creationDt);
		return this;
	}

	
	public User build() {
		return user;
	}
}
