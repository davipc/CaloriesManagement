package com.toptal.calories.resources.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.TestDBBase;
import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.entity.User;

public class TestMeals extends TestDBBase {

	public static Logger logger = LoggerFactory.getLogger(TestMeals.class);

	protected static Meals repository = new RepositoryFactory().createRepository(Meals.class);

	private static SimpleDateFormat sdf = new SimpleDateFormat("HHmmssSSS");
	private static String login = sdf.format(new Date());
	private static User testUser;
	private static String otherLogin = "2_" + login.substring(2);
	private static User otherTestUser;
	
	SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
	Date mealDate1;
	Date mealTime1;

	Date mealDate2;
	Date mealTime2;

	private static int invalidId = -1;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger.debug("Creating test users to be used by all tests...");
		
		testUser = TestUsers.getUser(login);
		testUser = TestUsers.createUser(testUser);
		
		otherTestUser = TestUsers.getUser(otherLogin);
		otherTestUser = TestUsers.createUser(otherTestUser);

		logger.debug("Finished creating test users to be used by all tests");
	}

	@Before
	public void setUp() throws Exception {
		SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
		mealDate1 = dateOnly.parse(dateOnly.format(new Date()));
		mealTime1 = getTime(0, 0);

		mealDate2 = dateOnly.parse(dateOnly.format(new Date()));
		mealTime2 = getTime(23, 59);
	}

	@After
	public void tearDown() throws Exception {
	}

	@AfterClass
	public static void afterClass() throws Exception {
		logger.debug("Deleting test users after all tests done...");
		
		TestUsers.removeUser(testUser);
		TestUsers.removeUser(otherTestUser);

		logger.debug("Finished deleting test users after all tests done");
	}
	
	/******************************************************************************/
	/**  		            Helper Methods                                       **/											
	/******************************************************************************/

	public static Meal getMeal(User user, Date date, Date time) {
		
		Meal meal = new Meal();
		meal.setUser(user);
		meal.setMealDate(date);
		meal.setMealTime(time);
		meal.setDescription("Test description");
		meal.setCalories(500);

		return meal;
	}

	
	public static Meal createMeal(Meal testMeal) throws RepositoryException {
		Meal meal = null;
		if (testMeal == null) {
			throw new RepositoryException("Null test Meal received for create!"); 
		}
		meal = repository.createOrUpdate(testMeal);
		if (meal == null) {
			throw new RepositoryException("Null Meal returned from create!"); 
		}
		return meal;
	}

	public static void removeMeal(Meal testMeal) throws RepositoryException {
		if (testMeal == null || testMeal.getId() == null ) {
			throw new RepositoryException("Null entity or Id received: " + testMeal);
		}
		repository.remove(testMeal.getId());
	}
	
	
	/******************************************************************************/
	/******************************************************************************/
	/**  		            MEAL ONLY tests                                  **/											
	/******************************************************************************/
	/******************************************************************************/
	
	/******************************************************************************/
	/**  		            CRUD tests                                           **/											
	/******************************************************************************/
	
	@Test
	public void testSuccessEnd2End() {
		logger.debug("Running " + getCurrentMethodName());
		Meal testMeal = getMeal(testUser, mealDate1, mealTime1);
		boolean removed = false;
		try {
			testMeal = repository.createOrUpdate(testMeal);

			Meal meal = repository.find(testMeal.getId());
			assertNotNull("Customer was expected to be found: " + testMeal, meal);
			assertEquals("Inserted and found meals didn't match: ", testMeal, meal);
			
			meal.setCalories(700);
			Meal updMeal = repository.createOrUpdate(meal);
			// Update the value on local object to compare after DB update
			meal = repository.find(testMeal.getId());
			assertNotNull("Meal was expected to be found: " + meal, meal);
			assertEquals("Updated and found meals didn't match: ", updMeal, meal);
			
			removed = repository.remove(testMeal.getId());
			assertTrue("Entity should have been removed!", removed);
			meal = repository.find(testMeal.getId());
			assertNull("Meal was expected NOT to be found: " + testMeal, meal);
		} catch (Exception e) {
			printException(e);
			fail(e.getMessage());
		} finally {
			if (!removed && testMeal != null && testMeal.getId() != null) {
				try {
					repository.remove(testMeal.getId());
				} catch (Exception e) {
					logger.error("In Finally Block: Failed to remove entity", e);
				}
			}
		}
	}
	
	@Test
	public void testGetAllMeals() {
		logger.debug("Running " + getCurrentMethodName());
		Meal testMeal = null;
		Meal secondMeal = null;
		Meal thirdMeal = null;
		Meal fourthMeal = null;
		try {
			// create test Meal
			testMeal = getMeal(testUser, mealDate1, mealTime1);
			testMeal = repository.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealDate2, mealTime2);
			secondMeal = repository.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = repository.createOrUpdate(thirdMeal);
			// create a fourth Meal
			fourthMeal = getMeal(otherTestUser, mealDate2, mealTime2);
			fourthMeal = repository.createOrUpdate(fourthMeal);
			
			List<Meal> meals = repository.findAll();
			assertNotNull("At least four meals should have been found", meals);
			assertTrue("At least four meals should have been found", meals.size() >= 4);
			assertTrue("Created meals were not found in the returned list", meals.contains(testMeal) && meals.contains(secondMeal));
		} catch (RepositoryException re) {
			fail("Get All meals should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Get All meals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					repository.remove(testMeal.getId());
				}
				if (secondMeal != null) {
					repository.remove(secondMeal.getId());
				}
				if (thirdMeal != null) {
					repository.remove(thirdMeal.getId());
				}
				if (fourthMeal != null) {
					repository.remove(fourthMeal.getId());
				}
			} catch (Exception e) {
				logger.error("In Finally Block: Failed to remove entities", e);
			}
		}
	}
	
	/**
	 * Tests null Meal sent for creation 
	 */
	@Test
	public void testCreateMealNullMeal() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.createOrUpdate(null);
			fail("Null Meal should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal should have caused a RepositoryExcpetion!");
		}
	}

	/**
	 * Tests null Meal User Id sent for creation 
	 */
	@Test
	public void testCreateMealNullUser() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Meal meal = getMeal(testUser, mealDate1, mealTime1);
			meal.setUser(null);
			repository.createOrUpdate(meal);
			fail("Null User should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User should have caused a RepositoryExcpetion!");
		}
	}

	/**
	 * Tests null Meal calories sent for creation 
	 */
	@Test
	public void testCreateMealNullCalories() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Meal meal = getMeal(testUser, mealDate1, mealTime1);
			meal.setCalories(null);
			repository.createOrUpdate(meal);
			fail("Null Meal Calories should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal Calories should have caused a RepositoryExcpetion!");
		}
	}
	
	/**
	 * Tests null Meal Description sent for creation 
	 */
	@Test
	public void testCreateMealNullDescription() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Meal meal = getMeal(testUser, mealDate1, mealTime1);
			meal.setDescription(null);
			repository.createOrUpdate(meal);
			fail("Null Meal Description should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal Description should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testFindMealNullMealId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.find(null);
			fail("Null Meal ID should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal ID should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testFindMealInvalidMealId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Meal meal = (Meal)repository.find(invalidId);
			assertNull("No meals should have been found for invalid Meal ID", meal);
		} catch (RepositoryException re) {
			fail("Invalid Meal ID should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Invalid Meal ID should NOT have caused an Exception!");
		}
	}
	
	@Test
	public void testUpdateMealNullMeal() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.createOrUpdate(null);
			fail("Null Meal should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testUpdateMealNullMealDescription() {
		logger.debug("Running " + getCurrentMethodName());
		Meal meal = getMeal(testUser, mealDate1, mealTime1);
		try {
			// create Meal for updating
			meal = repository.createOrUpdate(meal);
			meal.setDescription(null);
			meal = repository.createOrUpdate(meal);
			fail("Null Meal Description should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal Description should have caused a RepositoryExcpetion!");
		} finally {
			if (meal != null && meal.getId() != null) {
				try {
					removeMeal(meal);
				} catch (Exception e) {
					printException(e);
				}
			}
		}
	}
	
	@Test
	public void testUpdateMealNullMealCalories() {
		logger.debug("Running " + getCurrentMethodName());
		Meal meal = getMeal(testUser, mealDate1, mealTime1);
		try {
			// create Meal for updating
			meal = repository.createOrUpdate(meal);
			meal.setCalories(null);
			meal = repository.createOrUpdate(meal);
			fail("Null Meal Calories should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal Calories should have caused a RepositoryExcpetion!");
		} finally {
			if (meal != null && meal.getId() != null) {
				try {
					removeMeal(meal);
				} catch (Exception e) {
					printException(e);
				}
			}
		}
	}
	
	@Test
	public void testRemoveMealNullMealId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.remove(null);
			fail("Null Meal ID should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal ID should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testRemoveMealInvalidMealId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			boolean removed = repository.remove(invalidId);
			assertTrue("No entity should have been removed", !removed);
		} catch (RepositoryException re) {
			printException(re);
			fail("Invalid Meal ID should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Invalid Meal ID should NOT have caused a RepositoryExcpetion!");
		}
	}
}
