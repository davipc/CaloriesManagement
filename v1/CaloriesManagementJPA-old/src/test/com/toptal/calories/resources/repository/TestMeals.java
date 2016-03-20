package test.com.toptal.calories.resources.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.com.toptal.calories.resources.TestDBBase;

import com.toptal.calories.resources.RepositoryException;
import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.entity.User;
import com.toptal.calories.resources.repository.Meals;

public class TestMeals extends TestDBBase {

	public static Logger logger = Logger.getLogger(TestMeals.class);

	protected static Meals model = new Meals();

	private static SimpleDateFormat sdf = new SimpleDateFormat("ddMMHHmmss");
	private static String login = sdf.format(new Date());
	private static User testUser;
	private static String otherLogin = "2_" + login.substring(2);
	private static User otherTestUser;
	
	Timestamp mealTime1 = new Timestamp(System.currentTimeMillis()-100);
	Timestamp mealTime2 = new Timestamp(System.currentTimeMillis());

	private static int invalidId = -1;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testUser = TestUsers.getUser(login);
		testUser = TestUsers.createUser(testUser);
		
		otherTestUser = TestUsers.getUser(otherLogin);
		otherTestUser = TestUsers.createUser(otherTestUser);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/******************************************************************************/
	/**  		            Helper Methods                                       **/											
	/******************************************************************************/

	public static Meal getMeal(User user, Timestamp timestamp) {
		
		Meal meal = new Meal();
		meal.setUserId(user.getId());
		meal.setMealTime(timestamp);
		meal.setDescription("Test description");
		meal.setCalories(500);

		return meal;
	}

	
	public static Meal createMeal(Meal testMeal) throws RepositoryException {
		Meal meal = null;
		if (testMeal == null) {
			throw new RepositoryException("Null test Meal received for create!"); 
		}
		meal = model.createOrUpdate(testMeal);
		if (meal == null) {
			throw new RepositoryException("Null Meal returned from create!"); 
		}
		return meal;
	}

	public static void removeMeal(Meal testMeal) throws RepositoryException {
		if (testMeal == null || testMeal.getId() == null ) {
			throw new RepositoryException("Null entity or Id received: " + testMeal);
		}
		model.remove(testMeal.getId());
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
		Meal testMeal = getMeal(testUser, mealTime1);
		boolean removed = false;
		try {
			testMeal = model.createOrUpdate(testMeal);

			Meal meal = model.find(testMeal.getId());
			assertNotNull("Customer was expected to be found: " + testMeal, meal);
			assertEquals("Inserted and found meals didn't match: ", testMeal, meal);
			
			meal.setCalories(700);
			Meal updMeal = model.createOrUpdate(meal);
			// Update the value on local object to compare after DB update
			meal = model.find(testMeal.getId());
			assertNotNull("Meal was expected to be found: " + meal, meal);
			assertEquals("Updated and found meals didn't match: ", updMeal, meal);
			
			model.remove(testMeal.getId());
			removed = true;
			meal = model.find(testMeal.getId());
			assertNull("Meal was expected NOT to be found: " + testMeal, meal);
		} catch (Exception e) {
			printException(e);
			fail(e.getMessage());
		} finally {
			if (!removed && testMeal != null && testMeal.getId() != null) {
				try {
					model.remove(testMeal.getId());
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
			testMeal = getMeal(testUser, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			// create a fourth Meal
			fourthMeal = getMeal(otherTestUser, mealTime2);
			fourthMeal = model.createOrUpdate(fourthMeal);
			
			List<Meal> Meals = model.findAll();
			assertNotNull("At least four meals should have been found", Meals);
			assertTrue("At least four meals should have been found", Meals.size() >= 4);
			assertTrue("Created meals were not found in the returned list", Meals.contains(testMeal) && Meals.contains(secondMeal));
		} catch (RepositoryException me) {
			fail("Get All Meals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Get All Meals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					model.remove(testMeal.getId());
				}
				if (secondMeal != null) {
					model.remove(secondMeal.getId());
				}
				if (thirdMeal != null) {
					model.remove(thirdMeal.getId());
				}
				if (fourthMeal != null) {
					model.remove(fourthMeal.getId());
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
			model.createOrUpdate(null);
			fail("Null Meal should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal should have caused a ModelException!");
		}
	}

	/**
	 * Tests null Meal User Id sent for creation 
	 */
	@Test
	public void testCreateMealNullUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Meal meal = getMeal(testUser, mealTime1);
			meal.setUserId(null);
			model.createOrUpdate(meal);
			fail("Null Meal Login should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal Login should have caused a ModelException!");
		}
	}

	/**
	 * Tests null Meal calories sent for creation 
	 */
	@Test
	public void testCreateMealNullCalories() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Meal meal = getMeal(testUser, mealTime1);
			meal.setCalories(null);
			model.createOrUpdate(meal);
			fail("Null Meal Calories should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal Calories should have caused a ModelException!");
		}
	}
	
	/**
	 * Tests null Meal Description sent for creation 
	 */
	@Test
	public void testCreateMealNullDescription() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Meal meal = getMeal(testUser, mealTime1);
			meal.setDescription(null);
			model.createOrUpdate(meal);
			fail("Null Meal Description should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal Description should have caused a ModelException!");
		}
	}

	@Test
	public void testFindMealNullMealId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.find(null);
			fail("Null Meal ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal ID should have caused a ModelException!");
		}
	}

	@Test
	public void testFindMealInvalidMealId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Meal meal = (Meal)model.find(invalidId);
			assertNull("No Meals should have been found for invalid Meal ID", meal);
		} catch (RepositoryException me) {
			fail("Invalid Meal ID should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Invalid Meal ID should NOT have caused an Exception!");
		}
	}
	
	@Test
	public void testUpdateMealNullMeal() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.createOrUpdate(null);
			fail("Null Meal should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal should have caused a ModelException!");
		}
	}

	@Test
	public void testUpdateMealNullMealDescription() {
		logger.debug("Running " + getCurrentMethodName());
		Meal meal = getMeal(testUser, mealTime1);
		try {
			// create Meal for updating
			meal = model.createOrUpdate(meal);
			meal.setDescription(null);
			meal = model.createOrUpdate(meal);
			fail("Null Meal Description should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal Description should have caused a ModelException!");
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
		Meal meal = getMeal(testUser, mealTime1);
		try {
			// create Meal for updating
			meal = model.createOrUpdate(meal);
			meal.setCalories(null);
			meal = model.createOrUpdate(meal);
			fail("Null Meal Calories should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal Calories should have caused a ModelException!");
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
			model.remove(null);
			fail("Null Meal ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal ID should have caused a ModelException!");
		}
	}

	@Test
	public void testRemoveMealInvalidMealId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.remove(invalidId);
			fail("Invalid Meal ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Invalid Meal ID should have caused a ModelException!");
		}
	}
	
	/******************************************************************************/
	/**  		            Extended queries tests                               **/											
	/******************************************************************************/
	
	@Test
	public void testFindUserMealsNullUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.findUserMeals(null);
			fail("Null Meal ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Meal ID should have caused a ModelException!");
		}
	}
	
	@Test
	public void testFindUserMealsInvalidUser() {
		logger.debug("Running " + getCurrentMethodName());
		Meal testMeal = null;
		Meal secondMeal = null;
		Meal thirdMeal = null;
		Meal fourthMeal = null;
		try {
			// create test Meal
			testMeal = getMeal(testUser, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			// create a fourth Meal
			fourthMeal = getMeal(otherTestUser, mealTime2);
			fourthMeal = model.createOrUpdate(fourthMeal);
			
			List<Meal> Meals = model.findUserMeals(invalidId);
			assertNotNull("No meals should have been found", Meals);
			assertEquals("No meals should have been found", 0, Meals.size());
		} catch (RepositoryException me) {
			fail("Find User Meals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User Meals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					model.remove(testMeal.getId());
				}
				if (secondMeal != null) {
					model.remove(secondMeal.getId());
				}
				if (thirdMeal != null) {
					model.remove(thirdMeal.getId());
				}
				if (fourthMeal != null) {
					model.remove(fourthMeal.getId());
				}
			} catch (Exception e) {
				logger.error("In Finally Block: Failed to remove entities", e);
			}
		}
	}
	
	@Test
	public void testFindUserMealsValidUserNoMeals() {
		logger.debug("Running " + getCurrentMethodName());
		Meal testMeal = null;
		Meal secondMeal = null;
		Meal thirdMeal = null;
		Meal fourthMeal = null;
		try {
			// create test Meal
			testMeal = getMeal(testUser, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			// create a fourth Meal
			fourthMeal = getMeal(otherTestUser, mealTime2);
			fourthMeal = model.createOrUpdate(fourthMeal);
			
			List<Meal> Meals = model.findUserMeals(invalidId);
			assertNotNull("No meals should have been found", Meals);
			assertEquals("No meals should have been found", 0, Meals.size());
		} catch (RepositoryException me) {
			fail("Find User Meals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User Meals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					model.remove(testMeal.getId());
				}
				if (secondMeal != null) {
					model.remove(secondMeal.getId());
				}
				if (thirdMeal != null) {
					model.remove(thirdMeal.getId());
				}
				if (fourthMeal != null) {
					model.remove(fourthMeal.getId());
				}
			} catch (Exception e) {
				logger.error("In Finally Block: Failed to remove entities", e);
			}
		}
	}
	
	
	@Test
	public void testFindUserMealsValidUserOneMeal() {
		logger.debug("Running " + getCurrentMethodName());
		Meal testMeal = null;
		Meal secondMeal = null;
		Meal thirdMeal = null;
		Meal fourthMeal = null;
		try {
			// create test Meal
			testMeal = getMeal(testUser, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			
			List<Meal> Meals = model.findUserMeals(otherTestUser.getId());
			assertNotNull("One meal should have been found", Meals);
			assertEquals("One meal should have been found", 1, Meals.size());
			assertTrue("Created meals were not found in the returned list", Meals.contains(thirdMeal));
		} catch (RepositoryException me) {
			fail("Find User Meals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User Meals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					model.remove(testMeal.getId());
				}
				if (secondMeal != null) {
					model.remove(secondMeal.getId());
				}
				if (thirdMeal != null) {
					model.remove(thirdMeal.getId());
				}
				if (fourthMeal != null) {
					model.remove(fourthMeal.getId());
				}
			} catch (Exception e) {
				logger.error("In Finally Block: Failed to remove entities", e);
			}
		}
	}
	
	@Test
	public void testFindUserMealsValidUserTwoMeals() {
		logger.debug("Running " + getCurrentMethodName());
		Meal testMeal = null;
		Meal secondMeal = null;
		Meal thirdMeal = null;
		Meal fourthMeal = null;
		try {
			// create test Meal
			testMeal = getMeal(testUser, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			// create a fourth Meal
			fourthMeal = getMeal(otherTestUser, mealTime2);
			fourthMeal = model.createOrUpdate(fourthMeal);
			
			List<Meal> Meals = model.findUserMeals(testUser.getId());
			assertNotNull("Two meals should have been found", Meals);
			assertEquals("Two meals should have been found", 2, Meals.size());
			assertTrue("Created meals were not found in the returned list", Meals.contains(testMeal) && Meals.contains(secondMeal));
		} catch (RepositoryException me) {
			fail("Find User Meals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User Meals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					model.remove(testMeal.getId());
				}
				if (secondMeal != null) {
					model.remove(secondMeal.getId());
				}
				if (thirdMeal != null) {
					model.remove(thirdMeal.getId());
				}
				if (fourthMeal != null) {
					model.remove(fourthMeal.getId());
				}
			} catch (Exception e) {
				logger.error("In Finally Block: Failed to remove entities", e);
			}
		}
	}
	
	
}
