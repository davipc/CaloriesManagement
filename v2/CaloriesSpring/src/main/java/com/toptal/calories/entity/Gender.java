package com.toptal.calories.entity;

public enum Gender {
	M ("Male"),
	F ("Female");
	
	@SuppressWarnings("unused")
	private String name;
	
	private Gender(String name) {
		this.name = name;
	}
}
