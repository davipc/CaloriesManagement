package com.toptal.calories.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.toptal.calories.rest.exceptions.ForbiddenException;
import com.toptal.calories.rest.exceptions.NotFoundException;
import com.toptal.calories.rest.exceptions.UnauthorizedException;

public abstract class ExceptionAwareService {

	@ExceptionHandler(IllegalArgumentException.class)
	void handleIllegalArgumentException(HttpServletResponse response) throws IOException {
	    response.sendError(HttpStatus.BAD_REQUEST.value());
	}

	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response) throws IOException {
	    response.sendError(HttpStatus.NOT_FOUND.value());
	}

	@ExceptionHandler(ForbiddenException.class)
	void handleForbiddenException(HttpServletResponse response) throws IOException {
	    response.sendError(HttpStatus.FORBIDDEN.value());
	}

	@ExceptionHandler(UnauthorizedException.class)
	void handleUnauthorizedException(HttpServletResponse response) throws IOException {
	    response.sendError(HttpStatus.UNAUTHORIZED.value());
	}
}
