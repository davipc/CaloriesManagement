package com.toptal.calories.resources.entity;

public enum Gender {
	M ("Male"),
	F ("Female");
	
	@SuppressWarnings("unused")
	private String name;
	
	private Gender(String name) {
		this.name = name;
	}
}
