package com.toptal.calories.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.filter.session.SessionFilter;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory;
import com.toptal.calories.CustomDateDeserializer;
import com.toptal.calories.TestData;
import com.toptal.calories.app.Application;
import com.toptal.calories.builder.MealBuilder;
import com.toptal.calories.constants.RestPaths;
import com.toptal.calories.entity.Meal;
import com.toptal.calories.repository.MealRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:8090")
public class ITMealService {

	private static final String MEAL_RESOURCE = RestPaths.MEALS + "/{id}";	
	
	@Value("${local.server.port}")
	private int serverPort;
	
	@Value("${server.context-path}")
	private String context;
	
	@Autowired
	private TestData testData; 
	
	@Autowired
	// for removing created entries after tests
	private MealRepository mealRepository;
	
	private Meal mealForCreate;
	
	private static int mealToAlter = 1;
	
	@Before
	public void setUp() {
	    RestAssured.port = serverPort;
	    RestAssured.basePath = context;
	    
	    // need to change Jackson object mapper (JSON serializer/deserializer) because of specific date parsing required for time attributes  
	    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
	    		new Jackson2ObjectMapperFactory() {
	    		        @Override
	    		        public ObjectMapper create(@SuppressWarnings("rawtypes") Class aClass, String s) {
	    		            ObjectMapper mapper = new ObjectMapper();
	    		    		SimpleModule module = new SimpleModule();
	    		    		module.addDeserializer(Date.class, new CustomDateDeserializer());
	    		    		mapper.registerModule(module);

	    		            return mapper;
	    		        }
	    		}
	    ));
	    
	    mealForCreate = new MealBuilder().user(testData.user1).mealDate(new Date()).mealTime(new Date()).description("meal desc").calories(1765).build();
	}

	/*******************************************************************************************************************************/
	/***   Authentication for all authenticated tests                                                                            ***/
	/*******************************************************************************************************************************/
	
	// we will need a custom authentication method, since we are authenticating through the login page
	private void authenticateUser(String username, String password, SessionFilter sessionFilter) {
		given()
			.param("username", username)
			.param("password", password)
			.param("submit", "Login")
			.filter(sessionFilter)
			.log().all()
		.when()
			.post("/login.jsp");
	}
	
	/*******************************************************************************************************************************/
	/***   Get Meal tests                                                                                                        ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testGetMealNotAuth() {
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.get(MEAL_RESOURCE, testData.userMeals.get(testData.user1).get(0).getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value());
	}	
	
	@Test
	public void testGetMealInexistent() {

		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);

		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.get(MEAL_RESOURCE, -1)
		.then()
			.log().all()
			.statusCode(HttpStatus.NOT_FOUND.value());
	}	

	@Test
	public void testGetMealDefaultUserNotOwner() {

		// user 2 will try to get one of user 1's meal 
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user2.getLogin(), "2", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.get(MEAL_RESOURCE, testData.userMeals.get(testData.user1).get(0).getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	

	@Test
	public void testGetMealDefaultUserOwner() {
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		Meal meal = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.get(MEAL_RESOURCE, testData.userMeals.get(testData.user1).get(0).getId())
				.as(Meal.class);
		
		assertThat(meal).isEqualTo(testData.userMeals.get(testData.user1).get(0));
	}	

	@Test
	public void testGetMealAuthManager() {
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userManager.getLogin(), "1", sessionFilter);
		
		Meal meal = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.get(MEAL_RESOURCE, testData.userMeals.get(testData.user1).get(0).getId())
				.as(Meal.class);
		
		assertThat(meal).isEqualTo(testData.userMeals.get(testData.user1).get(0));		
	}	

	@Test
	public void testGetMealAuthAdmin() {
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);
		
		Meal meal = 
				given()
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
					.filter(sessionFilter)
				.expect()
					.log().all()
					.statusCode(HttpStatus.OK.value())
				.when()
					.get(MEAL_RESOURCE, testData.userMeals.get(testData.user1).get(0).getId())
					.as(Meal.class);
			
		assertThat(meal).isEqualTo(testData.userMeals.get(testData.user1).get(0));		
	}	
	
	/*******************************************************************************************************************************/
	/***   Create Meal tests                                                                                                     ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testCreateMealNotAuth() {
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(mealForCreate)
		.when()
			.log().all()
			.post(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value());
	}	
	
	@Test
	public void testCreateMealDefaultUserNotOwner() {

		// user 2 will try to get one of user 1's meal 
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user2.getLogin(), "2", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(mealForCreate)
		.when()
			.post(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	

	@Test
	public void testCreateMealNoUser() {

		Meal meal = new MealBuilder(mealForCreate).user(null).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(meal)
		.when()
			.post(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("user"));
	}	

	@Test
	public void testCreateMealMissingMealAttribute() {

		Meal meal = new MealBuilder(mealForCreate).calories(null).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(meal)
		.when()
			.post(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("calories"));
	}	

	@Test
	public void testCreateMealUniqueKeyViolation() {
		Meal meal = new MealBuilder(testData.userMeals.get(testData.user1).get(0)).id(null).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(meal)
		.when()
			.post(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("either user ID is invalid or there is already a meal"));
	}	


	// would need a mock to emulate JPA layer returning null on create (404 scenario)

	@Test
	public void testCreateMealOKDefaultUserOwnsMeal() {
		Integer id = null; 
		try {
			SessionFilter sessionFilter = new SessionFilter();
			authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
			
			Meal mealCreated = 
				given()
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
					.filter(sessionFilter)
					.body(mealForCreate)
				.expect()
					.log().all()
					.statusCode(HttpStatus.CREATED.value())
				.when()
					.post(RestPaths.MEALS)
					.as(Meal.class);
			
			assertThat(mealCreated).isNotNull();
			assertThat(mealCreated.getId()).isNotNull();
			id = mealCreated.getId();
			mealCreated.setId(null);
			assertThat(mealCreated).isEqualTo(mealForCreate);
		} finally {		
			if (id != null)
				mealRepository.delete(id);
		}
	}	
	

	@Test
	public void testCreateMealOKManagerUser() {
		Integer id = null; 
		try {
			SessionFilter sessionFilter = new SessionFilter();
			authenticateUser(testData.userManager.getLogin(), "1", sessionFilter);
			
			Meal mealCreated = 
				given()
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
					.filter(sessionFilter)
					.body(mealForCreate)
				.expect()
					.log().all()
					.statusCode(HttpStatus.CREATED.value())
				.when()
					.post(RestPaths.MEALS)
					.as(Meal.class);
			
			assertThat(mealCreated).isNotNull();
			assertThat(mealCreated.getId()).isNotNull();
			id = mealCreated.getId();
			mealCreated.setId(null);
			assertThat(mealCreated).isEqualTo(mealForCreate);
		} finally {		
			if (id != null)
				mealRepository.delete(id);
		}
	}	
	
	@Test
	public void testCreateMealOKAdminUser() {
		Integer id = null; 
		try {
			SessionFilter sessionFilter = new SessionFilter();
			authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);
			
			Meal mealCreated = 
				given()
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
					.filter(sessionFilter)
					.body(mealForCreate)
				.expect()
					.log().all()
					.statusCode(HttpStatus.CREATED.value())
				.when()
					.post(RestPaths.MEALS)
					.as(Meal.class);
			
			assertThat(mealCreated).isNotNull();
			assertThat(mealCreated.getId()).isNotNull();
			id = mealCreated.getId();
			mealCreated.setId(null);
			assertThat(mealCreated).isEqualTo(mealForCreate);
		} finally {		
			if (id != null)
				mealRepository.delete(id);
		}
	}	
	
	/*******************************************************************************************************************************/
	/***   Update Meal tests                                                                                                     ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testUpdateMealNotAuth() {
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			// no need to change the meal since it is expected the request will be denied
			.body(testData.userMeals.get(testData.user1).get(0))
		.when()
			.log().all()
			.put(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value());
	}	
	
	@Test
	public void testUpdateMealDefaultUserNotOwner() {

		// user 2 will try to get one of user 1's meal 
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user2.getLogin(), "2", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			// no need to change the meal since it is expected the request will be denied
			.body(testData.userMeals.get(testData.user1).get(0))
		.when()
			.put(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	

	@Test
	public void testUpdateMealNoUser() {

		Meal meal = new MealBuilder(testData.userMeals.get(testData.user1).get(0)).user(null).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(meal)
		.when()
			.put(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("user"));
	}	

	@Test
	public void testUpdateMealMissingMealAttribute() {

		Meal meal = new MealBuilder(testData.userMeals.get(testData.user1).get(0)).description(null).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(meal)
		.when()
			.put(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("description"));
	}	

	@Test
	public void testUpdateMealTamperedUserId() {
		// user 2 tries to modify user1's meal by tampering the user ID in the meal details
		Meal meal = new MealBuilder(testData.userMeals.get(testData.user1).get(0)).user(testData.user2).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user2.getLogin(), "2", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(meal)
		.when()
			.put(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value())
			.body("message", Matchers.containsString("No privileges to update meal"));
	}	

	@Test
	public void testUpdateMealUniqueKeyViolation() {
		// take the last meal to make sure it was not modified by other tests
		List<Meal> userMeals = testData.userMeals.get(testData.user1); 
		Meal otherMeal = userMeals.get(userMeals.size()-1);
		Meal meal = new MealBuilder(testData.userMeals.get(testData.user1).get(mealToAlter++)).mealDate(otherMeal.getMealDate()).mealTime(otherMeal.getMealTime()).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(meal)
		.when()
			.put(RestPaths.MEALS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("either user ID is invalid or there is already another meal"));
	}	

	// would need a mock to emulate JPA layer returning null on update (404 scenario)

	@Test
	public void testUpdateMealOKDefaultUserOwnsMeal() {
		Meal meal = new MealBuilder(testData.userMeals.get(testData.user1).get(mealToAlter++)).description("CHANGED BY Integration Test 1").mealTime(new Date()).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		Meal mealUpdated = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
				.body(meal)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.put(RestPaths.MEALS)
				.as(Meal.class);
			
		assertThat(mealUpdated).isNotNull();
		assertThat(mealUpdated).isEqualTo(meal);
	}	
	

	@Test
	public void testUpdateMealOKManagerUser() {
		Meal meal = new MealBuilder(testData.userMeals.get(testData.user1).get(mealToAlter++)).mealDate(new Date()).calories(2432).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userManager.getLogin(), "1", sessionFilter);
		
		Meal mealUpdated = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
				.body(meal)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.put(RestPaths.MEALS)
				.as(Meal.class);
			
		assertThat(mealUpdated).isNotNull();
		assertThat(mealUpdated).isEqualTo(meal);
	}	
	
	@Test
	public void testUpdateMealOKAdminUser() {
		Meal meal = new MealBuilder(testData.userMeals.get(testData.user1).get(mealToAlter++)).description("CHANGED BY Integration Test 3").build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);
		
		Meal mealUpdated = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
				.body(meal)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.put(RestPaths.MEALS)
				.as(Meal.class);
			
		assertThat(mealUpdated).isNotNull();
		assertThat(mealUpdated).isEqualTo(meal);
	}	
	
	/*******************************************************************************************************************************/
	/***   Delete Meal tests                                                                                                     ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testDeleteMealNotAuth() {
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.log().all()
			.delete(MEAL_RESOURCE, testData.userMeals.get(testData.user1).get(0).getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value());
	}	

	@Test
	public void testDeleteMealDefaultUserNotOwner() {

		// user 2 will try to get one of user 1's meal 
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user2.getLogin(), "2", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.delete(MEAL_RESOURCE, testData.userMeals.get(testData.user1).get(0).getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	
	
	// if not authenticated will return 401, due to spring security configuration in security configuration class
	@Test
	public void testDeleteMealNotFound() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);

		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.log().all()
			.delete(MEAL_RESOURCE, -1)
		.then()
			.log().all()
			.statusCode(HttpStatus.NOT_FOUND.value());
	}	

	// would need a mock to emulate JPA layer returning not found exception on delete (404 scenario)

	@Test
	public void testDeleteMealOKDefaultUserOwnsMeal() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.expect()
			.log().all()
			.statusCode(HttpStatus.NO_CONTENT.value())
		.when()
			.delete(MEAL_RESOURCE, testData.userMeals.get(testData.user1).get(mealToAlter++).getId());
	}	
	

	@Test
	public void testDeleteMealOKManagerUser() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userManager.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.expect()
			.log().all()
			.statusCode(HttpStatus.NO_CONTENT.value())
		.when()
			.delete(MEAL_RESOURCE, testData.userMeals.get(testData.user1).get(mealToAlter++).getId());
			
	}	
	
	@Test
	public void testDeleteMealOKAdminUser() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.expect()
			.log().all()
			.statusCode(HttpStatus.NO_CONTENT.value())
		.when()
			.delete(MEAL_RESOURCE, testData.userMeals.get(testData.user1).get(mealToAlter++).getId());
		
	}	
	
}
