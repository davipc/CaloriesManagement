package com.toptal.calories.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;

import com.toptal.calories.builder.MealBuilder;
import com.toptal.calories.builder.RoleBuilder;
import com.toptal.calories.builder.UserBuilder;
import com.toptal.calories.entity.Gender;
import com.toptal.calories.entity.Meal;
import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.RoleType;
import com.toptal.calories.entity.User;
import com.toptal.calories.repository.UserRepository;
import com.toptal.calories.rest.exceptions.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class TestUserService {

	// max login size is 12
	private static final String BIG_LOGIN = "123456789012";
	private static final String TOO_BIG_LOGIN =  BIG_LOGIN + "1";

	// max name size is 80
	private static final String BIG_NAME = "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890";
	private static final String TOO_BIG_NAME =  BIG_NAME + "1";
	
	private static final Role ROLE_DEFAULT = new RoleBuilder().id(1).name(RoleType.DEFAULT).build();
	private static final Role ROLE_MANAGER = new RoleBuilder().id(2).name(RoleType.MANAGER).build();
	
	private static final User USER_DEFAULT_1 = new UserBuilder().id(1).login("aLogin").password("XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=").name("aName").gender(Gender.F).dailyCalories(1500).roles(Arrays.asList(new Role[]{ROLE_DEFAULT})).meals(Arrays.asList(new Meal[]{})).creationDt(new Timestamp(System.currentTimeMillis())).build();
	private static final User USER_MANAGER = new UserBuilder().id(3).login("cLogin").password("XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=").name("cName").gender(Gender.F).dailyCalories(1400).roles(Arrays.asList(new Role[]{ROLE_MANAGER})).meals(Arrays.asList(new Meal[]{})).creationDt(new Timestamp(System.currentTimeMillis())).build();
	
	private static final User USER_DEFAULT_1_NO_ID = new UserBuilder(USER_DEFAULT_1).id(null).build();
	
	private static final Meal MEAL_1 = new MealBuilder().id(1).user(USER_DEFAULT_1).mealDate(new Date()).mealTime(new Date()).description("meal 1").calories(1200).build();
	private static final Meal MEAL_2 = new MealBuilder().id(2).user(USER_DEFAULT_1).mealDate(new Date()).mealTime(new Date()).description("meal 2").calories(1200).build();
	
	@InjectMocks
	private UserService userService;
	
	@Mock
	private UserRepository userRepository;

	/*******************************************************************************************************************************/
	/***   Get User tests                                                                                                        ***/
	/*******************************************************************************************************************************/

	@Test
	public void testGetUserNotFound() {
		// setup the mock repository
		given(userRepository.findOne(anyInt())).willReturn(null);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.getUser(USER_DEFAULT_1.getId()); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("No users found");
	}
	
	@Test
	public void testGetUserOneFound() {
		// setup the mock repository
		given(userRepository.findOne(anyInt())).willReturn(USER_DEFAULT_1);
		
		// make the service call
		try {
			assertThat(userService.getUser(USER_DEFAULT_1.getId())).isEqualTo(USER_DEFAULT_1);
		} catch (NotFoundException e) {
			fail("Error testing getUser: " + e.getMessage());
		}
	}
	
	/*******************************************************************************************************************************/
	/***   Get All User tests                                                                                                        ***/
	/*******************************************************************************************************************************/

	@Test
	public void testGetAllUsersNoUsers() {
		// setup the mock repository
		given(userRepository.findAll()).willReturn(new ArrayList<User>());
		
		// make the service call
		try {
			assertThat(userService.getAllUsers()).isEmpty();
		} catch (NotFoundException e) {
			fail("Error testing getAllUsers: " + e.getMessage());
		}
	}
	
	@Test
	public void testGetAllUsersOneFound() {
		// setup the mock repository
		given(userRepository.findAll()).willReturn(Arrays.asList(new User[]{USER_DEFAULT_1}));
		
		// make the service call
		try {
			assertThat(userService.getAllUsers()).contains(USER_DEFAULT_1);
		} catch (NotFoundException e) {
			fail("Error testing getAllUsers: " + e.getMessage());
		}
	}
	
	@Test
	public void testGetAllUsersTwoFound() {
		// setup the mock repository
		given(userRepository.findAll()).willReturn(Arrays.asList(new User[]{USER_DEFAULT_1, USER_MANAGER}));
		
		// make the service call
		try {
			assertThat(userService.getAllUsers()).contains(USER_DEFAULT_1, USER_MANAGER);
		} catch (NotFoundException e) {
			fail("Error testing getUser: " + e.getMessage());
		}
	}
	
	/*******************************************************************************************************************************/
	/***   Create User tests                                                                                                     ***/
	/*******************************************************************************************************************************/
	
	
	@Test
	public void testCreateUserNoLogin() {
		User user = new UserBuilder(USER_DEFAULT_1_NO_ID).login(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("login is null");
	}	

	@Test
	public void testCreateUserLoginTooBig() {
		User user = new UserBuilder(USER_DEFAULT_1_NO_ID).login(TOO_BIG_LOGIN).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("login size is bigger than");
	}	
	
	@Test
	public void testCreateUserNoName() {
		User user = new UserBuilder(USER_DEFAULT_1_NO_ID).name(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name is null");
	}	

	@Test
	public void testCreateUserNameTooBig() {
		User user = new UserBuilder(USER_DEFAULT_1_NO_ID).name(TOO_BIG_NAME).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name size is bigger than");
	}	

	@Test
	public void testCreateUserNoGender() {
		User user = new UserBuilder(USER_DEFAULT_1_NO_ID).gender(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("gender is null");
	}	
	

	@Test
	public void testCreateUserNoDailyCalories() {
		User user = new UserBuilder(USER_DEFAULT_1_NO_ID).dailyCalories(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("daily calories is null");
	}	
	
	@Test
	public void testCreateUserNoCreationDt() {
		User user = new UserBuilder(USER_DEFAULT_1_NO_ID).creationDt(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("creation date is null");
	}	
	
	@Test
	public void testCreateUserNoPassword() {
		User user = new UserBuilder(USER_DEFAULT_1_NO_ID).password(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("password is null");
	}	
	
	@Test
	public void testCreateUserNotNullId() {
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(USER_DEFAULT_1); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("should be null");
	}	

	@Test
	public void testCreateUserDataIntegrityViolation() {
		given(userRepository.save(USER_DEFAULT_1_NO_ID)).willThrow(new DataIntegrityViolationException(""));
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(USER_DEFAULT_1_NO_ID); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Error creating user: login already used");
	}	
	
	@Test
	public void testCreateUserNullReturned() {
		given(userRepository.save(USER_DEFAULT_1_NO_ID)).willReturn(null);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.createUser(USER_DEFAULT_1_NO_ID); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("no user returned from create");
	}	

	@Test
	public void testCreateUserOK() {
		given(userRepository.save(USER_DEFAULT_1_NO_ID)).willReturn(USER_DEFAULT_1);
		
		// make the service call
		try {
			assertThat(userService.createUser(USER_DEFAULT_1_NO_ID)).isEqualTo(USER_DEFAULT_1);
		} catch (NotFoundException e) {
			fail("Error testing createUser: " + e.getMessage());
		}	
	}	
	
	/*******************************************************************************************************************************/
	/***   Update User tests                                                                                                     ***/
	/*******************************************************************************************************************************/

	@Test
	public void testUpdateUserNoLogin() {
		User user = new UserBuilder(USER_DEFAULT_1).login(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("login is null");
	}	

	@Test
	public void testUpdateUserLoginTooBig() {
		User user = new UserBuilder(USER_DEFAULT_1).login(TOO_BIG_LOGIN).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("login size is bigger than");
	}	
	
	@Test
	public void testUpdateUserNoName() {
		User user = new UserBuilder(USER_DEFAULT_1).name(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name is null");
	}	

	@Test
	public void testUpdateUserNameTooBig() {
		User user = new UserBuilder(USER_DEFAULT_1).name(TOO_BIG_NAME).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name size is bigger than");
	}	

	@Test
	public void testUpdateUserNoGender() {
		User user = new UserBuilder(USER_DEFAULT_1).gender(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("gender is null");
	}	
	

	@Test
	public void testUpdateUserNoDailyCalories() {
		User user = new UserBuilder(USER_DEFAULT_1).dailyCalories(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("daily calories is null");
	}	
	
	@Test
	public void testUpdateUserNoCreationDt() {
		User user = new UserBuilder(USER_DEFAULT_1).creationDt(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(user); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("creation date is null");
	}	
	
	@Test
	public void testUpdateUserNullId() {
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(USER_DEFAULT_1_NO_ID); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("should NOT be null");
	}	

	@Test
	public void testUpdateUserNotFound() {
		given(userRepository.findOne(anyInt())).willReturn(null);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(USER_DEFAULT_1); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("not found for update");
	}	
	
	@Test
	public void testUpdateUserDataIntegrityViolation() {
		given(userRepository.findOne(anyInt())).willReturn(USER_DEFAULT_1);
		given(userRepository.save(USER_DEFAULT_1)).willThrow(new DataIntegrityViolationException(""));
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(USER_DEFAULT_1); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Error updating user: login already used");
	}	
	
	@Test
	public void testUpdateUserNullReturned() {
		given(userRepository.findOne(anyInt())).willReturn(USER_DEFAULT_1);
		given(userRepository.save(USER_DEFAULT_1)).willReturn(null);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.updateUser(USER_DEFAULT_1); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("no user returned from update");
	}	

	@Test
	public void testUpdateUserNoPasswordOK() {
		given(userRepository.findOne(anyInt())).willReturn(USER_DEFAULT_1);
		User user = new UserBuilder(USER_DEFAULT_1).password(null).build();
		
		given(userRepository.save(user)).willReturn(USER_DEFAULT_1);
		
		// make the service call
		try {
			assertThat(userService.updateUser(user)).isEqualTo(USER_DEFAULT_1);
		} catch (NotFoundException e) {
			fail("Error testing updateUser: " + e.getMessage());
		}	
	}	

	@Test
	public void testUpdateUserOK() {
		given(userRepository.findOne(anyInt())).willReturn(USER_DEFAULT_1);
		given(userRepository.save(USER_DEFAULT_1)).willReturn(USER_DEFAULT_1);
		
		// make the service call
		try {
			assertThat(userService.updateUser(USER_DEFAULT_1)).isEqualTo(USER_DEFAULT_1);
		} catch (NotFoundException e) {
			fail("Error testing updateUser: " + e.getMessage());
		}	
	}	
	
	/*******************************************************************************************************************************/
	/***   Delete User tests                                                                                                     ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testDeleteUserIdNotFound() {
		// setup mock repository
		willThrow(new EmptyResultDataAccessException(1)).given(userRepository).delete(anyInt());
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.deleteUser(USER_DEFAULT_1.getId()); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("not found");
	}
	
	@Test
	public void testDeleteUserOK() {
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.deleteUser(USER_DEFAULT_1.getId()); } );
		assertThat(thrown).isNull();
	}	
	
	
	/*******************************************************************************************************************************/
	/***   Get Meals from User tests                                                                                             ***/
	/*******************************************************************************************************************************/

	// expected date format is always "yyyy-MM-dd"
	// expected time format is always "HH:mm"
	
	@Test
	public void testGetMealsFromUserBadStartDate() {
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.getMealsFromUser(USER_DEFAULT_1.getId(), "X", "2016-03-31", "09:00", "19:59"); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid format on input date");
	}	
	
	@Test
	public void testGetMealsFromUserBadEndDate() {
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.getMealsFromUser(USER_DEFAULT_1.getId(), "2016-03-28", "13-04-2016", "09:00", "19:59"); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid format on input date");
	}	
	
	@Test
	public void testGetMealsFromUserBadStartTime() {
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.getMealsFromUser(USER_DEFAULT_1.getId(), "2016-03-28", "2016-03-31", "09:00:01", "19:59"); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid format on input time");
	}	
	
	@Test
	public void testGetMealsFromUserBadEndTime() {
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.getMealsFromUser(USER_DEFAULT_1.getId(), "2016-03-28", "2016-03-31", "09:00", "Y"); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid format on input time");
	}
	
	@Test
	public void testGetMealsFromUserOnlyUserIdNotFound() {
		given(userRepository.findOne(anyInt())).willReturn(null);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { userService.getMealsFromUser(USER_DEFAULT_1.getId(), null, null, null, null); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("No user found with ID");
	}
	
	// this one should not really happen - just making the service code bullet proof
	@Test
	public void testGetMealsFromUserOnlyUserIdOKNullMeals() {
		given(userRepository.findOne(anyInt())).willReturn(new UserBuilder(USER_DEFAULT_1).meals(null).build());
		
		// make the service call
		try {
			assertThat(userService.getMealsFromUser(USER_DEFAULT_1.getId(), null, null, null, null)).isNull();
		} catch (NotFoundException e) {
			fail("Error testing GetMealsFromUser: " + e.getMessage());
		}	
	}	

	@Test
	public void testGetMealsFromUserOnlyUserIdOKNoMeals() {
		given(userRepository.findOne(anyInt())).willReturn(USER_DEFAULT_1);
		
		// make the service call
		try {
			assertThat(userService.getMealsFromUser(USER_DEFAULT_1.getId(), null, null, null, null)).isEmpty();
		} catch (NotFoundException e) {
			fail("Error testing GetMealsFromUser: " + e.getMessage());
		}	
	}	

	@Test
	public void testGetMealsFromUserOnlyUserIdOKOneMeal() {
		given(userRepository.findOne(anyInt())).willReturn(new UserBuilder(USER_DEFAULT_1).meals(Arrays.asList(new Meal[]{MEAL_1})).build());
		
		// make the service call
		try {
			assertThat(userService.getMealsFromUser(USER_DEFAULT_1.getId(), null, null, null, null)).contains(MEAL_1);
		} catch (NotFoundException e) {
			fail("Error testing GetMealsFromUser: " + e.getMessage());
		}	
	}	

	@Test
	public void testGetMealsFromUserOnlyUserIdOKTwoMeals() {
		given(userRepository.findOne(anyInt())).willReturn(new UserBuilder(USER_DEFAULT_1).meals(Arrays.asList(new Meal[]{MEAL_1, MEAL_2})).build());
		
		// make the service call
		try {
			assertThat(userService.getMealsFromUser(USER_DEFAULT_1.getId(), null, null, null, null)).contains(MEAL_1, MEAL_2);
		} catch (NotFoundException e) {
			fail("Error testing GetMealsFromUser: " + e.getMessage());
		}	
	}
	
	@Test
	public void testGetMealsFromUserOnlyStartDateOKTwoMeals() {
		given(userRepository.findMealsInDateAndTimeRange(anyInt(), any(Date.class), any(Date.class), any(Date.class), any(Date.class))).willReturn(Arrays.asList(new Meal[]{MEAL_1, MEAL_2}));
		
		// make the service call
		try {
			assertThat(userService.getMealsFromUser(USER_DEFAULT_1.getId(), "2016-03-28", null, null, null)).contains(MEAL_1, MEAL_2);
		} catch (NotFoundException e) {
			fail("Error testing GetMealsFromUser: " + e.getMessage());
		}	
	}
	
	@Test
	public void testGetMealsFromUserOnlyEndDateOKOneMeal() {
		given(userRepository.findMealsInDateAndTimeRange(anyInt(), any(Date.class), any(Date.class), any(Date.class), any(Date.class))).willReturn(Arrays.asList(new Meal[]{MEAL_1}));
		
		// make the service call
		try {
			assertThat(userService.getMealsFromUser(USER_DEFAULT_1.getId(), null, "2016-03-28", null, null)).contains(MEAL_1);
		} catch (NotFoundException e) {
			fail("Error testing GetMealsFromUser: " + e.getMessage());
		}	
	}

	@Test
	public void testGetMealsFromUserOnlyStartTimeOKOneMeal() {
		given(userRepository.findMealsInDateAndTimeRange(anyInt(), any(Date.class), any(Date.class), any(Date.class), any(Date.class))).willReturn(Arrays.asList(new Meal[]{MEAL_2}));
		
		// make the service call
		try {
			assertThat(userService.getMealsFromUser(USER_DEFAULT_1.getId(), null, null, "01:26", null)).contains(MEAL_2);
		} catch (NotFoundException e) {
			fail("Error testing GetMealsFromUser: " + e.getMessage());
		}	
	}
	
	@Test
	public void testGetMealsFromUserOnlyEndTimeOKNoMeals() {
		given(userRepository.findMealsInDateAndTimeRange(anyInt(), any(Date.class), any(Date.class), any(Date.class), any(Date.class))).willReturn(new ArrayList<Meal>());
		
		// make the service call
		try {
			assertThat(userService.getMealsFromUser(USER_DEFAULT_1.getId(), null, null, null, "18:39")).isEmpty();
		} catch (NotFoundException e) {
			fail("Error testing GetMealsFromUser: " + e.getMessage());
		}	
	}

	// shouldn't really happen, just making the service code bullet proof
	@Test
	public void testGetMealsFromUserStartEndTimesOKNullMeals() {
		given(userRepository.findMealsInDateAndTimeRange(anyInt(), any(Date.class), any(Date.class), any(Date.class), any(Date.class))).willReturn(null);
		//given(userRepository.findMealsInDateAndTimeRange(anyInt(), eq(null), eq(null), eq(null), eq(null))).willReturn(null);
		
		// make the service call
		try {
			assertThat(userService.getMealsFromUser(USER_DEFAULT_1.getId(), null, null, "02:43", "18:39")).isNull();
		} catch (NotFoundException e) {
			fail("Error testing GetMealsFromUser: " + e.getMessage());
		}	
	}
	
}
