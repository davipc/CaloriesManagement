package com.toptal.calories.rest.exceptions;

public class UnauthorizedException extends Exception {
	private static final long serialVersionUID = 2469085713253143233L;
	
	public UnauthorizedException() {
	}

	public UnauthorizedException(String message) {
		super(message);
	}

	public UnauthorizedException(String message, Throwable t) {
		super(message, t);
	}
	
}
