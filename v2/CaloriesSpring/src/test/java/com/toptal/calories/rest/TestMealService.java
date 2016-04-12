package com.toptal.calories.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.anyInt;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.toptal.calories.builder.MealBuilder;
import com.toptal.calories.builder.RoleBuilder;
import com.toptal.calories.builder.UserBuilder;
import com.toptal.calories.entity.Meal;
import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.RoleType;
import com.toptal.calories.entity.User;
import com.toptal.calories.repository.MealRepository;
import com.toptal.calories.rest.exceptions.ForbiddenException;
import com.toptal.calories.rest.exceptions.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class TestMealService {

	// max description size is 200
	private static final String BIG_DESCRIPTION = "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + 
			  									  "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890";
	
	private static final String TOO_BIG_DESCRIPTION =  BIG_DESCRIPTION + "1";
	
	private static final Role ROLE_DEFAULT = new RoleBuilder().id(1).name(RoleType.DEFAULT).build();
	private static final Role ROLE_MANAGER = new RoleBuilder().id(2).name(RoleType.MANAGER).build();
	private static final Role ROLE_ADMIN = new RoleBuilder().id(3).name(RoleType.ADMIN).build();
	
	private static final User USER_DEFAULT_1 = new UserBuilder().id(1).roles(Arrays.asList(new Role[]{ROLE_DEFAULT})).build();
	private static final User USER_DEFAULT_2 = new UserBuilder().id(2).roles(Arrays.asList(new Role[]{ROLE_DEFAULT})).build();
	private static final User USER_MANAGER = new UserBuilder().id(3).roles(Arrays.asList(new Role[]{ROLE_MANAGER})).build();
	private static final User USER_ADMIN = new UserBuilder().id(4).roles(Arrays.asList(new Role[]{ROLE_ADMIN})).build();
	
	private static final Meal MEAL_NO_ID = new MealBuilder().user(USER_DEFAULT_1).mealDate(new Date()).mealTime(new Date()).description(BIG_DESCRIPTION).calories(1200).build();

	private static final Meal MEAL = new MealBuilder(MEAL_NO_ID).id(1).build();
	
	@InjectMocks
	private MealService mealService;
	
	@Mock
	private MealRepository mealRepository;

	// for update and delete we will need the user credentials
	@Mock
	private Authentication authentication;
	@Mock
	private SecurityContext securityContext;
	
	
	private void setupAuthentication(User principal) {
		given(securityContext.getAuthentication()).willReturn(authentication);
		given(authentication.getPrincipal()).willReturn(principal);
		SecurityContextHolder.setContext(securityContext);
	}
	
	
	/*******************************************************************************************************************************/
	/***   Get Meal tests                                                                                                        ***/
	/*******************************************************************************************************************************/

	@Test
	public void testGetMealNotFound() {
		// setup the mock repository
		given(mealRepository.findOne(anyInt())).willReturn(null);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.getMeal(MEAL.getId()); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("No meals found");
	}
	
	@Test
	public void testGetMealOneFound() {
		// setup the mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		
		// make the service call
		try {
			assertThat(mealService.getMeal(MEAL.getId())).isEqualTo(MEAL);
		} catch (NotFoundException e) {
			fail("Error testing getMeal: " + e.getMessage());
		}
	}
	
	/*******************************************************************************************************************************/
	/***   Create Meal tests                                                                                                     ***/
	/*******************************************************************************************************************************/
	
	
	@Test
	public void testCreateMealNoUser() {
		Meal meal = new MealBuilder(MEAL_NO_ID).user(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.createMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("user is null");
	}
	
	@Test
	public void testCreateMealNoUserId() {
		Meal meal = new MealBuilder(MEAL_NO_ID).user(new UserBuilder().build()).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.createMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("user.Id is null");
	}	

	@Test
	public void testCreateMealNoMealDate() {
		Meal meal = new MealBuilder(MEAL_NO_ID).mealDate(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.createMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("meal date is null");
	}	

	@Test
	public void testCreateMealNoMealTime() {
		Meal meal = new MealBuilder(MEAL_NO_ID).mealTime(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.createMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("meal time is null");
	}	

	@Test
	public void testCreateMealNoDescription() {
		Meal meal = new MealBuilder(MEAL_NO_ID).description(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.createMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("description is null");
	}	

	@Test
	public void testCreateMealDescriptionTooBig() {
		Meal meal = new MealBuilder(MEAL_NO_ID).description(TOO_BIG_DESCRIPTION).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.createMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("description size is bigger than");
	}	

	@Test
	public void testCreateMealNoMealCalories() {
		Meal meal = new MealBuilder(MEAL_NO_ID).calories(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.createMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("calories is null");
	}	
	
	@Test
	public void testCreateMealNotNullId() {
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.createMeal(MEAL); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("should be null");
	}	

	@Test
	public void testCreateMealDataIntegrityViolation() {
		given(mealRepository.save(MEAL_NO_ID)).willThrow(new DataIntegrityViolationException(""));
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.createMeal(MEAL_NO_ID); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Failed creating meal: either user ID is invalid or there is already a meal for the informed date and time");
	}	
	
	@Test
	public void testCreateMealNullReturned() {
		given(mealRepository.save(MEAL_NO_ID)).willReturn(null);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.createMeal(MEAL_NO_ID); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("meal not returned from persist");
	}	

	@Test
	public void testCreateMealOK() {
		given(mealRepository.save(MEAL_NO_ID)).willReturn(MEAL);
		
		// make the service call
		try {
			assertThat(mealService.createMeal(MEAL_NO_ID)).isEqualTo(MEAL);
		} catch (NotFoundException e) {
			fail("Error testing createMeal: " + e.getMessage());
		}	
	}	
	
	/*******************************************************************************************************************************/
	/***   Update Meal tests                                                                                                     ***/
	/*******************************************************************************************************************************/

	@Test
	public void testUpdateMealNoUser() {
		Meal meal = new MealBuilder(MEAL).user(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("user is null");
	}
	
	@Test
	public void testUpdateMealNoUserId() {
		Meal meal = new MealBuilder(MEAL).user(new UserBuilder().build()).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("user.Id is null");
	}	

	@Test
	public void testUpdateMealNoMealDate() {
		Meal meal = new MealBuilder(MEAL).mealDate(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("meal date is null");
	}	

	@Test
	public void testUpdateMealNoMealTime() {
		Meal meal = new MealBuilder(MEAL).mealTime(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("meal time is null");
	}	

	@Test
	public void testUpdateMealNoDescription() {
		Meal meal = new MealBuilder(MEAL).description(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("description is null");
	}	

	@Test
	public void testUpdateMealDescriptionTooBig() {
		Meal meal = new MealBuilder(MEAL).description(TOO_BIG_DESCRIPTION).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("description size is bigger than");
	}	

	@Test
	public void testUpdateMealNoMealCalories() {
		Meal meal = new MealBuilder(MEAL).calories(null).build();
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(meal); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("calories is null");
	}	
	
	@Test
	public void testUpdateMealNullId() {
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(MEAL_NO_ID); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("should NOT be null");
	}	
	
	@Test
	public void testUpdateMealIdNotFound() {
		// setup the mock authentication
		setupAuthentication(USER_DEFAULT_1);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(null);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(MEAL); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("not found");
	}
	
	@Test
	public void testUpdateMealNoPrivilegeNoPrincipal() {
		// setup the mock authentication
		setupAuthentication(null);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(MEAL); } );
		assertThat(thrown).isNotNull().isInstanceOf(ForbiddenException.class).hasMessageContaining("No privileges to update");
	}

	@Test
	public void testUpdateMealNoPrivilegeOtherDefaultUser() {
		// setup the mock authentication
		setupAuthentication(USER_DEFAULT_2);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(MEAL); } );
		assertThat(thrown).isNotNull().isInstanceOf(ForbiddenException.class).hasMessageContaining("No privileges to update");
	}

	@Test
	public void testUpdateMealDataIntegrityViolation() {
		// setup the mock authentication
		setupAuthentication(USER_DEFAULT_1);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		given(mealRepository.save(MEAL)).willThrow(new DataIntegrityViolationException(""));
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(MEAL); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Failed updating meal: either user ID is invalid");
	}	
	
	@Test
	public void testUpdateMealNullReturned() {
		// setup the mock authentication
		setupAuthentication(USER_DEFAULT_1);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		given(mealRepository.save(MEAL)).willReturn(null);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.updateMeal(MEAL); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("meal not returned from update");
	}	

	
	@Test
	public void testUpdateMealOKPrincipalIsOwner() {
		// setup the mock authentication
		setupAuthentication(USER_DEFAULT_1);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		given(mealRepository.save(MEAL)).willReturn(MEAL);
		
		// make the service call
		try {
			assertThat(mealService.updateMeal(MEAL)).isEqualTo(MEAL);
		} catch (Exception e) {
			fail("Error testing updateMeal: " + e.getMessage());
		}	
	}	

	@Test
	public void testUpdateMealOKPrincipalIsManager() {
		// setup the mock authentication
		setupAuthentication(USER_MANAGER);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		given(mealRepository.save(MEAL)).willReturn(MEAL);
		
		// make the service call
		try {
			assertThat(mealService.updateMeal(MEAL)).isEqualTo(MEAL);
		} catch (Exception e) {
			fail("Error testing updateMeal: " + e.getMessage());
		}	
	}	

	@Test
	public void testUpdateMealOKPrincipalIsAdmin() {
		// setup the mock authentication
		setupAuthentication(USER_ADMIN);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		given(mealRepository.save(MEAL)).willReturn(MEAL);
		
		// make the service call
		try {
			assertThat(mealService.updateMeal(MEAL)).isEqualTo(MEAL);
		} catch (Exception e) {
			fail("Error testing updateMeal: " + e.getMessage());
		}	
	}	
	
	/*******************************************************************************************************************************/
	/***   Delete Meal tests                                                                                                     ***/
	/*******************************************************************************************************************************/
	
	@Test
	public void testDeleteMealIdNotFound() {
		// setup the mock authentication
		setupAuthentication(USER_DEFAULT_1);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(null);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.deleteMeal(MEAL.getId()); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("not found");
	}
	
	@Test
	public void testDeleteMealNoPrivilegeNoPrincipal() {
		// setup the mock authentication
		setupAuthentication(null);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.deleteMeal(MEAL.getId()); } );
		assertThat(thrown).isNotNull().isInstanceOf(ForbiddenException.class).hasMessageContaining("No privileges to delete");
	}

	@Test
	public void testDeleteMealNoPrivilegeOtherDefaultUser() {
		// setup the mock authentication
		setupAuthentication(USER_DEFAULT_2);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.deleteMeal(MEAL.getId()); } );
		assertThat(thrown).isNotNull().isInstanceOf(ForbiddenException.class).hasMessageContaining("No privileges to delete");
	}

	@Test
	public void testDeleteMealEmptyResultDataAccess() {
		// setup the mock authentication
		setupAuthentication(USER_DEFAULT_1);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		willThrow(new EmptyResultDataAccessException(1)).given(mealRepository).delete(MEAL.getId());
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.deleteMeal(MEAL.getId()); } );
		assertThat(thrown).isNotNull().isInstanceOf(NotFoundException.class).hasMessageContaining("not found for delete");
	}	

	@Test
	public void testDeleteMealOKPrincipalIsOwner() {
		// setup the mock authentication
		setupAuthentication(USER_DEFAULT_1);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.deleteMeal(MEAL.getId()); } );
		assertThat(thrown).isNull();
	}	

	@Test
	public void testDeleteMealOKPrincipalIsManager() {
		// setup the mock authentication
		setupAuthentication(USER_MANAGER);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.deleteMeal(MEAL.getId()); } );
		assertThat(thrown).isNull();
	}	

	@Test
	public void testDeleteMealOKPrincipalIsAdmin() {
		// setup the mock authentication
		setupAuthentication(USER_ADMIN);
		// setup mock repository
		given(mealRepository.findOne(anyInt())).willReturn(MEAL);
		
		// make the service call
		Throwable thrown = catchThrowable(() -> { mealService.deleteMeal(MEAL.getId()); } );
		assertThat(thrown).isNull();
	}	
	
}
