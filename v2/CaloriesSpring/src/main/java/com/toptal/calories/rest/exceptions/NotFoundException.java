package com.toptal.calories.rest.exceptions;

public class NotFoundException extends Exception {
	private static final long serialVersionUID = 2469085713253143232L;
	
	public NotFoundException() {
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, Throwable t) {
		super(message, t);
	}
	
}
