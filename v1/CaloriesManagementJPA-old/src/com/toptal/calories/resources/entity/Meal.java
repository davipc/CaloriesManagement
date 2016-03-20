package com.toptal.calories.resources.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.toptal.calories.resources.BaseEntity;

/**
 * The persistent class for the meal database table.
 * 
 */
@Entity
@Table(indexes = {
		@Index(name="Meal_IDX", columnList="user_id, meal_time")
})
@NamedQueries ({
	@NamedQuery(name="Meal.findAll", query="SELECT m FROM Meal m"),
	@NamedQuery(name="Meal.findByUserId", query="SELECT m FROM Meal m where m.userId = :userId")
	//@NamedQuery(name="Meal.findInDateTimeRanges", 
				//query="SELECT m FROM Meal m where m.userId = :userId and m.mealTime >= ")
})

public class Meal extends BaseEntity {
	//private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="meal_id_seq", sequenceName="meal_id_seq", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="meal_id_seq")
	@Column(name = "id", updatable=false)	
	private Integer id;

	@Column(name="user_id")
	private Integer userId;

	@Column(name="meal_time")
	private Timestamp mealTime;

	private String description;

	private Integer calories;

	public Meal() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCalories() {
		return this.calories;
	}

	public void setCalories(Integer calories) {
		this.calories = calories;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getMealTime() {
		return this.mealTime;
	}

	public void setMealTime(Timestamp mealTime) {
		this.mealTime = mealTime;
	}

	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		
		if (obj != null && obj instanceof Meal) {
			Meal other = (Meal) obj;
			result = areEqual(other.getId(), getId()) &&
					areEqual(other.getUserId(), getUserId()) &&
					areEqual(other.getMealTime(), getMealTime()) &&
					areEqual(other.getDescription(), getDescription()) &&
					areEqual(other.getCalories(), getCalories());
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("Meal [Id: ").append(id)
			.append(", userId: ").append(userId)
			.append(", mealTime: ").append(mealTime)
			.append(", description: ").append(description)
			.append(", calories: ").append(calories);
		sb.append("]");
		
		return sb.toString();
	}	
}