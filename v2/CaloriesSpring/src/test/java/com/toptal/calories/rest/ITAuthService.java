package com.toptal.calories.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.toptal.calories.TestData;
import com.toptal.calories.app.Application;
import com.toptal.calories.builder.UserBuilder;
import com.toptal.calories.constants.RestPaths;
import com.toptal.calories.entity.User;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:8090")
public class ITAuthService {

	@Value("${local.server.port}")
	private int serverPort;
	
	@Value("${server.context-path}")
	private String context;
	
	@Autowired
	private TestData testData; 
	
	@Before
	public void setUp() {
	    RestAssured.port = serverPort;
	    RestAssured.basePath = context;
	}

	
	@Test
	public void testAuthenticateBadMethodGet() 
	throws JsonProcessingException {
		User user = new UserBuilder().login(null).password("password").build(); 
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(user)
		.when()
			.get(RestPaths.AUTH)
		.then()
			.log().all()
			.statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
	}	
	
	@Test
	public void testAuthenticateBadMediaType() 
	throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		User user = new UserBuilder().login("user1").password("password").build(); 
		String json = mapper.writeValueAsString(user);
		
		given()
			.contentType(ContentType.URLENC)
			.accept(ContentType.JSON)
			.body(json)
		.when()
			.post(RestPaths.AUTH)
		.then()
			.log().all()
			.statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
	}	
	
	@Test
	public void testAuthenticateUserMissingLogin() 
	throws JsonProcessingException {
		User user = new UserBuilder().login(null).password("password").build(); 
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(user)
		.when()
			.post(RestPaths.AUTH)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("Missing login"));
	}	
	
	@Test
	public void testAuthenticateUserMissingPassword() 
	throws JsonProcessingException {
		User user = new UserBuilder().login("dcavalca").password(null).build(); 
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(user)
		.when()
			.post(RestPaths.AUTH)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("Missing"));
	}	
	
	
	@Test
	public void testAuthenticateUserBadLogin() 
	throws JsonProcessingException {
		User user = new UserBuilder().login("XXXXX").password("password").build(); 
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(user)
		.when()
			.post(RestPaths.AUTH)
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value())
			.body("message", Matchers.is("Invalid login/password"));
	}	

	@Test
	public void testAuthenticateUserBadPassword() 
	throws JsonProcessingException {
		User user = new UserBuilder().login(testData.user1.getLogin()).password("2").build(); 
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(user)
		.when()
			.post(RestPaths.AUTH)
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value())
			.body("message", Matchers.is("Invalid login/password"));
	}	

	@Test
	public void testAuthenticateUserGoodCredentials() 
	throws JsonProcessingException {
		User user = new UserBuilder().login(testData.user1.getLogin()).password("1").build(); 
		
		User returnedUser = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(user)
			.expect()
				.statusCode(HttpStatus.OK.value())
				.log().all()
			.when()
				.post(RestPaths.AUTH)
				.as(User.class);

		assertThat(returnedUser).isEqualTo(testData.user1);
	}	
	
}
