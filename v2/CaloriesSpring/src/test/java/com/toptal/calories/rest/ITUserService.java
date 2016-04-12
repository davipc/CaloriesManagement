package com.toptal.calories.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import com.toptal.calories.builder.UserBuilder;
import com.toptal.calories.constants.RestPaths;
import com.toptal.calories.entity.Gender;
import com.toptal.calories.entity.Meal;
import com.toptal.calories.entity.User;
import com.toptal.calories.repository.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:8090")
public class ITUserService {

	private static final String USER_RESOURCE = RestPaths.USERS + "/{id}";	
	private static final String USER_MEALS = RestPaths.USERS + "/{id}/meals";
	
	@Value("${local.server.port}")
	private int serverPort;
	
	@Value("${server.context-path}")
	private String context;
	
	@Autowired
	private TestData testData; 
	
	@Autowired
	// for removing created entries after tests
	private UserRepository userRepository;
	
	private User userForCreate;
	
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
	    
	    userForCreate = new UserBuilder(testData.user1).id(null).login("itUser1").password("1").dailyCalories(1357).build();
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
	/***   Get User tests                                                                                                        ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testGetUserNotAuth() {
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.get(USER_RESOURCE, testData.user1.getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value());
	}	
	
	@Test
	public void testGetUserInexistent() {

		// must be logged as admin to get the not found error (otherwise will get forbidden)
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);

		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.get(USER_RESOURCE, -1)
		.then()
			.log().all()
			.statusCode(HttpStatus.NOT_FOUND.value());
	}	

	@Test
	public void testGetUserDefaultUserNotOwner() {

		// user 2 will try to get one of user 1's user 
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user2.getLogin(), "2", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.get(USER_RESOURCE, testData.user1.getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	

	@Test
	public void testGetUserManagerUserNotOwner() {

		// user 2 will try to get one of user 1's user 
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userManager.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.get(USER_RESOURCE, testData.user1.getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	
	
	@Test
	public void testGetUserDefaultUserOwner() {
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		User user = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.get(USER_RESOURCE, testData.user1.getId())
				.as(User.class);
		
		assertThat(user).isEqualTo(testData.user1);
	}	

	@Test
	public void testGetUserAuthAdmin() {
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);
		
		User user = 
				given()
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
					.filter(sessionFilter)
				.expect()
					.log().all()
					.statusCode(HttpStatus.OK.value())
				.when()
					.get(USER_RESOURCE, testData.user1.getId())
					.as(User.class);
			
		assertThat(user).isEqualTo(testData.user1);		
	}	

	/*******************************************************************************************************************************/
	/***   Get All Users                                                                                                         ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testGetAllUsersNotAuth() {
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.get(RestPaths.USERS)
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value());
	}	
	
	@Test
	public void testGetAllUsersDefaultUser() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.expect()
			.statusCode(HttpStatus.FORBIDDEN.value())	
		.when()
			.get(RestPaths.USERS);
	}	

	@Test
	public void testGetAllUsersOKManager() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userManager.getLogin(), "1", sessionFilter);
		
		User[] users = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
			.expect()
				.statusCode(HttpStatus.OK.value())	
			.when()
				.get(RestPaths.USERS)
				.as(User[].class);
		
		assertThat(users).contains(testData.user1, testData.user2, testData.user3, testData.userManager, testData.userAdmin);
	}	

	@Test
	public void testGetAllUsersOKAdmin() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);
		
		User[] users = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
			.expect()
				.statusCode(HttpStatus.OK.value())	
			.when()
				.get(RestPaths.USERS)
				.as(User[].class);
		
		assertThat(users).contains(testData.user1, testData.user2, testData.user3, testData.userManager, testData.userAdmin);
	}	
	
	// TODO: add test for no users returned
	
	/*******************************************************************************************************************************/
	/***   Create User tests                                                                                                     ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testCreateUserMissingUserAttribute() {

		User user = new UserBuilder(userForCreate).dailyCalories(null).build();
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(user)
		.when()
			.post(RestPaths.USERS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("daily calories"));
	}	

	@Test
	public void testCreateUserMissingPassword() {

		User user = new UserBuilder(userForCreate).password(null).build();
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(user)
		.when()
			.post(RestPaths.USERS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("password"));
	}	
	
	@Test
	public void testCreateUserUniqueKeyViolation() {
		User user = new UserBuilder(userForCreate).login(testData.user1.getLogin()).build();
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(user)
		.when()
			.post(RestPaths.USERS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("login already used"));
	}	
	
	// would need a mock to emulate JPA layer returning null on create (404 scenario)

	@Test
	public void testCreateUserOKNotAuth() {
		Integer id = null;
		try {
			User createdUser = 
				given()
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
					.body(userForCreate)
				.expect()
					.log().all()
					.statusCode(HttpStatus.CREATED.value())
				.when()
					.log().all()
					.post(RestPaths.USERS)
					.as(User.class);
			assertThat(createdUser).isNotNull();
			id = createdUser.getId();
			// id and password are changed as part of creation
			assertThat(createdUser).isEqualToIgnoringGivenFields(userForCreate, "id", "password");
		} finally {
			if (id != null) {
				userRepository.delete(id);
			}
		}
	}	
	
	@Test
	public void testCreateUserOKDefaultUser() {
		Integer id = null; 
		try {
			SessionFilter sessionFilter = new SessionFilter();
			authenticateUser(testData.user1.getLogin(), "1", sessionFilter);
			
			User createdUser = 
				given()
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
					.filter(sessionFilter)
					.body(userForCreate)
				.expect()
					.log().all()
					.statusCode(HttpStatus.CREATED.value())
				.when()
					.post(RestPaths.USERS)
					.as(User.class);
			
			assertThat(createdUser).isNotNull();
			assertThat(createdUser.getId()).isNotNull();
			id = createdUser.getId();
			// id and password are changed as part of creation
			assertThat(createdUser).isEqualToIgnoringGivenFields(userForCreate, "id", "password");
		} finally {		
			if (id != null)
				userRepository.delete(id);
		}
	}	
	

	@Test
	public void testCreateUserOKManagerUser() {
		Integer id = null; 
		try {
			SessionFilter sessionFilter = new SessionFilter();
			authenticateUser(testData.userManager.getLogin(), "1", sessionFilter);
			
			User createdUser = 
				given()
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
					.filter(sessionFilter)
					.body(userForCreate)
				.expect()
					.log().all()
					.statusCode(HttpStatus.CREATED.value())
				.when()
					.post(RestPaths.USERS)
					.as(User.class);
			
			assertThat(createdUser).isNotNull();
			assertThat(createdUser.getId()).isNotNull();
			id = createdUser.getId();
			assertThat(createdUser).isEqualToIgnoringGivenFields(userForCreate, "id", "password");
		} finally {		
			if (id != null)
				userRepository.delete(id);
		}
	}	
	
	@Test
	public void testCreateUserOKAdminUser() {
		Integer id = null; 
		try {
			SessionFilter sessionFilter = new SessionFilter();
			authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);
			
			User createdUser = 
				given()
					.contentType(ContentType.JSON)
					.accept(ContentType.JSON)
					.filter(sessionFilter)
					.body(userForCreate)
				.expect()
					.log().all()
					.statusCode(HttpStatus.CREATED.value())
				.when()
					.post(RestPaths.USERS)
					.as(User.class);
			
			assertThat(createdUser).isNotNull();
			assertThat(createdUser.getId()).isNotNull();
			id = createdUser.getId();
			assertThat(createdUser).isEqualToIgnoringGivenFields(userForCreate, "id", "password");
		} finally {		
			if (id != null)
				userRepository.delete(id);
		}
	}	
	
	/*******************************************************************************************************************************/
	/***   Update User tests                                                                                                     ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testUpdateUserNotAuth() {
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			// no need to change the user since it is expected the request will be denied
			.body(testData.user3)
		.when()
			.log().all()
			.put(RestPaths.USERS)
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value());
	}	
	
	@Test
	public void testUpdateUserDefaultUserNotOwner() {

		// user 4 will try to modify user 3
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user4.getLogin(), "4", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			// no need to change the user since it is expected the request will be denied
			.body(testData.user3)
		.when()
			.put(RestPaths.USERS)
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	

	@Test
	public void testUpdateUserManagerUserNotOwner() {

		// user 4 will try to modify user 3
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userManager.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			// no need to change the user since it is expected the request will be denied
			.body(testData.user3)
		.when()
			.put(RestPaths.USERS)
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	
	
	@Test
	public void testUpdateUserNoId() {

		User user = new UserBuilder(testData.user3).id(null).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user3.getLogin(), "3", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(user)
		.when()
			.put(RestPaths.USERS)
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	

	@Test
	public void testUpdateUserMissingUserAttribute() {

		User user = new UserBuilder(testData.user3).name(null).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user3.getLogin(), "3", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(user)
		.when()
			.put(RestPaths.USERS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("name"));
	}	

	// would need a mock to emulate JPA layer returning null on find (404 scenario)
	
	@Test
	public void testUpdateUserUniqueKeyViolation() {
		// take the last user to make sure it was not modified by other tests
		User user = new UserBuilder(testData.user3).login(testData.user4.getLogin()).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user3.getLogin(), "3", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.body(user)
		.when()
			.put(RestPaths.USERS)
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("Error updating user: login already used"));
	}	

	// would need a mock to emulate JPA layer returning null on update (404 scenario)

	@Test
	public void testUpdateUserOKDefaultUserOwnsUserNoPasswordChange() {
		User user = new UserBuilder(testData.user4).name("New IT Name 4").dailyCalories(890).password(null).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user4.getLogin(), "4", sessionFilter);
		
		User userUpdated = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
				.body(user)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.put(RestPaths.USERS)
				.as(User.class);
			
		assertThat(userUpdated).isNotNull();
		assertThat(userUpdated).isEqualToIgnoringGivenFields(user, "password");
	}	
	

	@Test
	public void testUpdateUserOKAdminUserNoPasswordChange() {
		User user = new UserBuilder(testData.user5).gender(Gender.F).login("user5New").password(null).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);
		
		User userUpdated = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
				.body(user)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.put(RestPaths.USERS)
				.as(User.class);
			
		assertThat(userUpdated).isNotNull();
		assertThat(userUpdated).isEqualToIgnoringGivenFields(user, "password");
	}	
	
	@Test
	public void testUpdateUserOKDefaultUserOwnsUserPasswordChanged() {
		User user = new UserBuilder(testData.user6).name("New IT Name 6").password("6.1").dailyCalories(890).build();
		
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user6.getLogin(), "6", sessionFilter);
		
		User userUpdated = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
				.body(user)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.put(RestPaths.USERS)
				.as(User.class);
			
		assertThat(userUpdated).isNotNull();
		// make sure the password has changed
		assertThat(userUpdated.getPassword()).isNotEqualTo(testData.user6.getPassword());
		assertThat(userUpdated).isEqualToIgnoringGivenFields(user, "password");
	}	
	
	/*******************************************************************************************************************************/
	/***   Delete User tests                                                                                                     ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testDeleteUserNotAuth() {
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.log().all()
			.delete(USER_RESOURCE, testData.user7.getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value());
	}	

	@Test
	public void testDeleteUserDefaultUser() {

		// default user will try to delete himself 
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user7.getLogin(), "7", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.delete(USER_RESOURCE, testData.user7.getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	

	@Test
	public void testDeleteUserManagerUser() {

		// manager user will try to delete himself 
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userManager.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.delete(USER_RESOURCE, testData.userManager.getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}	
	
	@Test
	public void testDeleteUserAdminUserNotFound() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);

		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.log().all()
			.delete(USER_RESOURCE, -1)
		.then()
			.log().all()
			.statusCode(HttpStatus.NOT_FOUND.value());
	}	

	@Test
	public void testDeleteUserOKAdmin() {
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
			.delete(USER_RESOURCE, testData.user8.getId());
	}	

	/*******************************************************************************************************************************/
	/***   Get User Meals tests                                                                                                  ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testGetMealsFromUserNotAuth() {
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.log().all()
			.get(USER_MEALS, testData.user9.getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.UNAUTHORIZED.value());
	}	
	
	// expected date format is always "yyyy-MM-dd"
	// expected time format is always "HH:mm"
	
	@Test
	public void testGetMealsFromUserBadDate() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user9.getLogin(), "9", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.queryParam("fromDate", "X")
			.queryParam("toDate", "2016-03-31")
			.queryParam("fromTime", "09:00")
			.queryParam("toTime", "19:59")
		.when()
			.get(USER_MEALS, testData.user9.getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("Invalid format on input date"));
	}	
	
	
	@Test
	public void testGetMealsFromUserBadTime() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user9.getLogin(), "9", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
			.queryParam("fromDate", "2016-03-25")
			.queryParam("toDate", "2016-03-31")
			.queryParam("fromTime", "09:00")
			.queryParam("toTime", "11:00:01")
		.when()
			.get(USER_MEALS, testData.user9.getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("message", Matchers.containsString("Invalid format on input time"));
	}
	
	@Test
	public void testGetMealsFromUserOnlyUserIdNotFound() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.get(USER_MEALS, -1)
		.then()
			.log().all()
			.statusCode(HttpStatus.NOT_FOUND.value())
			.body("message", Matchers.containsString("No user found"));
	}

	@Test
	public void testGetMealsFromUserDefaultUserNotOwner() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user9.getLogin(), "9", sessionFilter);
		
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.filter(sessionFilter)
		.when()
			.get(USER_MEALS, testData.user10.getId())
		.then()
			.log().all()
			.statusCode(HttpStatus.FORBIDDEN.value());
	}

	@Test
	public void testGetMealsFromUserDefaultUserOwnerNoMeals() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user9.getLogin(), "9", sessionFilter);
		
		Meal[] meals = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.get(USER_MEALS, testData.user9.getId())
				.as(Meal[].class);
		
		assertThat(meals).isEmpty();
	}

	@Test
	public void testGetMealsFromUserDefaultUserOwnerAllMeals() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user10.getLogin(), "10", sessionFilter);
		
		Meal[] meals = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.get(USER_MEALS, testData.user10.getId())
				.as(Meal[].class);
		
		assertThat(meals).hasSameSizeAs(testData.userMeals.get(testData.user10));
	}
	
	@Test
	public void testGetMealsFromUserDefaultUserOwnerFilteredByDate() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.user10.getLogin(), "10", sessionFilter);

		List<Meal> allMeals = testData.userMeals.get(testData.user10);
		Date firstDay = allMeals.get(0).getMealDate();
		Date lastDay = allMeals.get(allMeals.size()-1).getMealDate();
		
		int numDays = (int)((lastDay.getTime() - firstDay.getTime()) / (1000*60*60*24)) + 1;
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		
		Meal[] meals = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
				.queryParam("fromDate", sdfDate.format(addToDateTime(firstDay, Calendar.DATE, 2)))
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.get(USER_MEALS, testData.user10.getId())
				.as(Meal[].class);

		// there are 3 meals per day 
		
		assertThat(meals).hasSize((numDays-2)*3);
	}

	@Test
	public void testGetMealsFromUserManagerFilteredByTime() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userManager.getLogin(), "1", sessionFilter);

		List<Meal> allMeals = testData.userMeals.get(testData.user10);
		Date firsTime = allMeals.get(0).getMealTime();
		Date lastTime = allMeals.get(allMeals.size()-1).getMealTime();
		
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
		
		Meal[] meals = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
				// from one minute after first meal to the minute of last meal (2/3 of meals)
				.queryParam("fromTime", sdfTime.format(addToDateTime(firsTime, Calendar.MINUTE, 1)))
				.queryParam("toTime", sdfTime.format(lastTime))
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.get(USER_MEALS, testData.user10.getId())
				.as(Meal[].class);

		// there are 3 meals per day 
		
		assertThat(meals).hasSize(allMeals.size()*2/3);
	}
	
	@Test
	public void testGetMealsFromUserAdminFilteredByDateAndTime() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);

		List<Meal> allMeals = testData.userMeals.get(testData.user10);
		Date firstDay = allMeals.get(0).getMealDate();
		Date lastDay = allMeals.get(allMeals.size()-1).getMealDate();
		Date lastTime = allMeals.get(allMeals.size()-1).getMealTime();
		int numDays = (int)((lastDay.getTime() - firstDay.getTime()) / (1000*60*60*24)) + 1;
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");		
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
		
		Meal[] meals = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
				.queryParam("toDate", sdfDate.format(addToDateTime(lastDay, Calendar.DATE, -3)))
				.queryParam("fromTime", sdfTime.format(addToDateTime(lastTime, Calendar.MINUTE, -1)))
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.get(USER_MEALS, testData.user10.getId())
				.as(Meal[].class);

		// there are 3 meals per day - we will only get the last one 
		assertThat(meals).hasSize( numDays - 3);
	}
	

	@Test
	public void testGetMealsFromUserAdminFilteredByDateAndTimeNoMeals() {
		SessionFilter sessionFilter = new SessionFilter();
		authenticateUser(testData.userAdmin.getLogin(), "1", sessionFilter);

		List<Meal> allMeals = testData.userMeals.get(testData.user10);
		Date firstDay = allMeals.get(0).getMealDate();
		Date lastTime = allMeals.get(allMeals.size()-1).getMealTime();
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");		
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
		
		Meal[] meals = 
			given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.filter(sessionFilter)
				// from one minute after first meal to the minute of last meal (2/3 of meals)
				.queryParam("toDate", sdfDate.format(addToDateTime(firstDay, Calendar.DATE, 2)))
				.queryParam("fromTime", sdfTime.format(addToDateTime(lastTime, Calendar.MINUTE, 1)))
			.expect()
				.log().all()
				.statusCode(HttpStatus.OK.value())
			.when()
				.get(USER_MEALS, testData.user10.getId())
				.as(Meal[].class);

		assertThat(meals).isEmpty();
	}
	
	private Date addToDateTime(Date d, int calendarField, int units) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		
		cal.add(calendarField, units);
		
		return cal.getTime();
		
	}
	
}
