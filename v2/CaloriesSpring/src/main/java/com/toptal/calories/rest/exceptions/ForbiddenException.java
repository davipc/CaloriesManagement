package com.toptal.calories.rest.exceptions;

public class ForbiddenException extends Exception {
	private static final long serialVersionUID = 2469085713253143232L;
	
	public ForbiddenException() {
	}

	public ForbiddenException(String message) {
		super(message);
	}

	public ForbiddenException(String message, Throwable t) {
		super(message, t);
	}
	
}
