package com.toptal.calories.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

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

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.toptal.calories.TestData;
import com.toptal.calories.app.Application;
import com.toptal.calories.constants.RestPaths;
import com.toptal.calories.entity.Role;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:8090")
public class ITRoleService {

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
	public void testGetAllRoles() {
		
		Role[] roles = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
			.expect()
				.statusCode(HttpStatus.OK.value())	
			.when()
				.get(RestPaths.ROLES)
				.as(Role[].class);
		
		assertThat(roles).contains(testData.roleDefault, testData.roleManager, testData.roleAdmin);
	}	
	
}
