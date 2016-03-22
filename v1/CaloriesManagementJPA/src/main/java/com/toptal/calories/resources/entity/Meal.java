package com.toptal.calories.resources.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the meal database table.
 * 
 * Notes on the meal date and time choice:
 * 
 * 
 * We will not store in a timestamp with TZ because:  
 *   - there are no queries related to timezone (there's no usable report considering meal date/time TZs)
 *   - if a user travels from one location to another, he will still want to always store the meal time information in the local timezone
 *     
 * We will not store in a timestamp without TZ because:  
 *   - JPA queries in this format are platform dependent, and more complex
 *   
 * So we will store date and time in distinct columns, so: 
 * 	- we will always store absolute date and time values (DST and TZ independent)
 *  - queries will be simpler
 *  
 */


@XmlRootElement(name = "meal")
@XmlType(propOrder = {"id","mealDate","mealTime","description", "calories"})
@XmlAccessorType(XmlAccessType.FIELD)

@Entity
@Table(
		uniqueConstraints={@UniqueConstraint(name = "Meal_UNIQ", columnNames = {"user_id" , "meal_date", "meal_time"})},
		indexes = {@Index(name="Meal_IDX", columnList="user_id, meal_date, meal_time")
})
@NamedQueries ({
	@NamedQuery(name="Meal.findAll", query="SELECT m FROM Meal m"),
	@NamedQuery(name="Meal.findByUserId", query="SELECT m FROM Meal m where m.user.id = :userId"),
	@NamedQuery(name="Meal.findInDateAndTimeRange", 
			query="SELECT m FROM Meal m where m.user.id = :userId and m.mealDate between :startDate and :endDate and m.mealTime between :startTime and :endTime")
})

public class Meal extends BaseEntity {

	@Id
	@SequenceGenerator(name="meal_id_seq", sequenceName="meal_id_seq", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="meal_id_seq")
	@Column(name = "id", updatable=false, nullable=false)	
	private Integer id;

	// makes sure this is not present in the generated JSON
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false, foreignKey = @ForeignKey(name = "Meal_User_FK"))
    private User user;
	
	@Column(name="meal_date", nullable=false)
	@Temporal(TemporalType.DATE)
	private Date mealDate;
	
	@Column(name="meal_time", nullable=false)
	@Temporal(TemporalType.TIME)
	private Date mealTime;

	@Column(nullable=false)
	private String description;

	@Column(nullable=false)
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

	public Date getMealDate() {
		return this.mealDate;
	}

	public void setMealDate(Date mealDate) {
		this.mealDate = mealDate;
	}

	public Date getMealTime() {
		return this.mealTime;
	}

	public void setMealTime(Date mealTime) {
		this.mealTime = mealTime;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		
		if (obj != null && obj instanceof Meal) {
			Meal other = (Meal) obj;

			//System.out.println("Comparing " + mealDate + " to " + new Date(other.getMealDate().getTime()));
			//System.out.println("Comparing " + mealTime + " to " + new Date(other.getMealTime().getTime()));
			result = areEqual(other.getId(), getId()) &&
					areEqual(other.getUser(), getUser()) &&
					areEqual(other.getMealDate(), getMealDate()) &&
					areEqual(other.getMealTime(), getMealTime()) &&
					areEqual(other.getDescription(), getDescription()) &&
					areEqual(other.getCalories(), getCalories());
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("Meal [Id: ").append(id)
			.append(", user: {").append(user).append("}")
			.append(", mealDate: ").append(mealDate)
			.append(", mealTime: ").append(mealTime)
			.append(", description: ").append(description)
			.append(", calories: ").append(calories);
		sb.append("]");
		
		return sb.toString();
	}	
}