package com.toptal.calories.resources.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.toptal.calories.resources.RepositoryException;
import com.toptal.calories.resources.TestDBBase;
import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.entity.User;

public class TestMeals extends TestDBBase {

	public static Logger logger = Logger.getLogger(TestMeals.class);

	protected static Meals model = new Meals();

	private static SimpleDateFormat sdf = new SimpleDateFormat("HHmmssSSS");
	private static String login = sdf.format(new Date());
	private static User testUser;
	private static String otherLogin = "2_" + login.substring(2);
	private static User otherTestUser;
	private static String thirdLogin = "3_" + login.substring(2);
	private static User thirdTestUser;
	
	SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
	Date mealDate1;
	Date mealTime1;

	Date mealDate2;
	Date mealTime2;

	private static int invalidId = -1;

	private List<Meal> dateTimeRangesTestMeals = new ArrayList<>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger.debug("Creating test users to be used by all tests...");
		
		testUser = TestUsers.getUser(login);
		testUser = TestUsers.createUser(testUser);
		
		otherTestUser = TestUsers.getUser(otherLogin);
		otherTestUser = TestUsers.createUser(otherTestUser);

		thirdTestUser = TestUsers.getUser(thirdLogin);
		thirdTestUser = TestUsers.createUser(thirdTestUser);
		
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
		TestUsers.removeUser(thirdTestUser);

		logger.debug("Finished deleting test users after all tests done");
	}
	
	/******************************************************************************/
	/**  		            Helper Methods                                       **/											
	/******************************************************************************/

	public static Meal getMeal(User user, Date date, Date time) {
		
		Meal meal = new Meal();
		meal.setUserId(user.getId());
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
		Meal testMeal = getMeal(testUser, mealDate1, mealTime1);
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
			testMeal = getMeal(testUser, mealDate1, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealDate2, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			// create a fourth Meal
			fourthMeal = getMeal(otherTestUser, mealDate2, mealTime2);
			fourthMeal = model.createOrUpdate(fourthMeal);
			
			List<Meal> meals = model.findAll();
			assertNotNull("At least four meals should have been found", meals);
			assertTrue("At least four meals should have been found", meals.size() >= 4);
			assertTrue("Created meals were not found in the returned list", meals.contains(testMeal) && meals.contains(secondMeal));
		} catch (RepositoryException me) {
			fail("Get All meals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Get All meals should NOT have caused an Exception!");
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
			Meal meal = getMeal(testUser, mealDate1, mealTime1);
			meal.setUserId(null);
			model.createOrUpdate(meal);
			fail("Null User ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a ModelException!");
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
			Meal meal = getMeal(testUser, mealDate1, mealTime1);
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
			assertNull("No meals should have been found for invalid Meal ID", meal);
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
		Meal meal = getMeal(testUser, mealDate1, mealTime1);
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
		Meal meal = getMeal(testUser, mealDate1, mealTime1);
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
	
	/******************************************************************************/
	/** Find user meals tests                                                    **/
	/******************************************************************************/
	
	@Test
	public void testFindUserMealsNullUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.findUserMeals(null);
			fail("Null User ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a ModelException!");
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
			testMeal = getMeal(testUser, mealDate1, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealDate2, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			// create a fourth Meal
			fourthMeal = getMeal(otherTestUser, mealDate2, mealTime2);
			fourthMeal = model.createOrUpdate(fourthMeal);
			
			List<Meal> meals = model.findUserMeals(invalidId);
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException me) {
			fail("Find User meals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals should NOT have caused an Exception!");
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
			testMeal = getMeal(testUser, mealDate1, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealDate2, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			// create a fourth Meal
			fourthMeal = getMeal(otherTestUser, mealDate2, mealTime2);
			fourthMeal = model.createOrUpdate(fourthMeal);
			
			List<Meal> meals = model.findUserMeals(invalidId);
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException me) {
			fail("Find User meals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals should NOT have caused an Exception!");
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
			testMeal = getMeal(testUser, mealDate1, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealDate2, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			
			List<Meal> meals = model.findUserMeals(otherTestUser.getId());
			assertNotNull("One meal should have been found", meals);
			assertEquals("One meal should have been found", 1, meals.size());
			assertTrue("Created meals were not found in the returned list", meals.contains(thirdMeal));
		} catch (RepositoryException me) {
			fail("Find User meals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals should NOT have caused an Exception!");
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
			testMeal = getMeal(testUser, mealDate1, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealDate2, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			// create a fourth Meal
			fourthMeal = getMeal(otherTestUser, mealDate2, mealTime2);
			fourthMeal = model.createOrUpdate(fourthMeal);
			
			List<Meal> meals = model.findUserMeals(testUser.getId());
			assertNotNull("Two meals should have been found", meals);
			assertEquals("Two meals should have been found", 2, meals.size());
			assertTrue("Created meals were not found in the returned list", meals.contains(testMeal) && meals.contains(secondMeal));
		} catch (RepositoryException me) {
			fail("Find User meals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals should NOT have caused an Exception!");
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
	
	/******************************************************************************/
	/** Find user meals in date and time range tests                             **/
	/******************************************************************************/

	/******************************************************************************/
	/** Test specific helper methods 	                                         **/
	/******************************************************************************/
	
	private void prepareForDateTimeRangeTests() 
	throws RepositoryException {
		// create 3 meals a day for 2 users for the last 60 days
		int numDays = 60;
		int[] mealTimeHours = {9, 12, 18};
		int[] mealTimeMinutes = {0, 30, 45};
		User[] users = {testUser, otherTestUser};
		
		Date mealTime;
		Meal meal;
		
		try {
			for (User user: users) {
				for (int i = numDays; i >= 0; i--) {
					for (int j = 0; j < mealTimeHours.length; j++) {
						mealTime = getDateDaysAgoAtTime(i, mealTimeHours[j], mealTimeMinutes[j]);
						
						meal = getMeal(user, mealTime, mealTime);
						meal = createMeal(meal);
						dateTimeRangesTestMeals.add(meal);
					}
				}
			}
		} catch (RepositoryException re) {
			logger.error("Error creating test meals for date range test", re);

			try {
				for (Meal toRemove: dateTimeRangesTestMeals) {
					removeMeal(toRemove);
				}
			} catch (RepositoryException re2) {
				logger.error("Error removing test meals after creation failure", re2);
			}
			
			throw re;
		}
	}
	
	private void afterDateTimeRangeTest() {
		for (Meal toRemove : dateTimeRangesTestMeals) {
			try {
				removeMeal(toRemove);
			} catch (RepositoryException re) {
				logger.error("Error removing test meals", re);
			}
		}
		dateTimeRangesTestMeals.clear();
	}
	
	private Date getDateDaysAgoAtTime(int daysAgo, int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -daysAgo);
		
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTime();
	}
	
	private Date getTime(int hours, int minutes) 
	throws InvalidParameterException {
		if (hours < 0 || hours > 23)
			throw new InvalidParameterException("Invalid hour: " + hours);
		if (minutes < 0 || minutes > 59)
			throw new InvalidParameterException("Invalid minute: " + minutes);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.HOUR_OF_DAY, hours);
		cal.set(Calendar.MINUTE, minutes);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTime();
	}
	
	/******************************************************************************/
	/** Actual tests 	                                                         **/
	/******************************************************************************/
	
	@Test
	public void testfindUserMealsInDateAndTimeRangesNullUser() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.findUserMealsInDateAndTimeRanges(null, new Date(0), new Date(), getTime(0,0), getTime(23,59));
			fail("Null User ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a ModelException!");
		}
	}

	@Test
	public void testfindUserMealsInDateAndTimeRangesNullFromDate() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.findUserMealsInDateAndTimeRanges(testUser.getId(), null, new Date(), getTime(0,0), getTime(23,59));
			fail("Null start date should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null end date should have caused a ModelException!");
		}
	}

	@Test
	public void testfindUserMealsInDateAndTimeRangesNullToDate() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.findUserMealsInDateAndTimeRanges(testUser.getId(), new Date(0), null, getTime(0,0), getTime(23,59));
			fail("Null end date should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null end date should have caused a ModelException!");
		}
	}

	@Test
	public void testfindUserMealsInDateAndTimeRangesNullFromTime() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.findUserMealsInDateAndTimeRanges(testUser.getId(), new Date(0), new Date(), null, getTime(23,59));
			fail("Null start time should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null start time  should have caused a ModelException!");
		}
	}

	@Test
	public void testfindUserMealsInDateAndTimeRangesNullToTime() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.findUserMealsInDateAndTimeRanges(testUser.getId(), new Date(0), new Date(), getTime(0,0), null);
			fail("Null end time should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null end time should have caused a ModelException!");
		}
	}
	
	@Test
	public void testfindUserMealsInDateAndTimeRangesStartDateAfterEndDate() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// start date = today, end date = yesterday
			model.findUserMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(0, 0, 0), getDateDaysAgoAtTime(1, 0, 0), getTime(0,0), getTime(23,59));
			fail("Invalid start and end dates should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Invalid start and end dates should have caused a ModelException!");
		}
	}

	@Test
	public void testfindUserMealsInDateAndTimeRangesStartTimeAfterEndTime() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.findUserMealsInDateAndTimeRanges(testUser.getId(), new Date(0), new Date(), getTime(15,10), getTime(15,9));
			fail("Invalid start and end times should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Invalid start and end times should have caused a ModelException!");
		}
	}

	
	@Test
	public void testfindUserMealsInDateAndTimeRangesInvalidUser() {
		logger.debug("Running " + getCurrentMethodName());
		Meal testMeal = null;
		Meal secondMeal = null;
		Meal thirdMeal = null;
		Meal fourthMeal = null;
		try {
			// create test Meal
			testMeal = getMeal(testUser, mealDate1, mealTime1);
			testMeal = model.createOrUpdate(testMeal);
			// create a second Meal
			secondMeal = getMeal(testUser, mealDate2, mealTime2);
			secondMeal = model.createOrUpdate(secondMeal);
			// create a third Meal
			thirdMeal = getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = model.createOrUpdate(thirdMeal);
			
			List<Meal> meals = model.findUserMealsInDateAndTimeRanges(invalidId, new Date(0), new Date(), getTime(0,0), getTime(23,59));
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException me) {
			fail("testDateFuncsFourMeals should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("testDateFuncsFourMeals should NOT have caused an Exception!");
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
	public void testfindUserMealsInDateAndTimeRangesNoMealsForUser() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = model.findUserMealsInDateAndTimeRanges(thirdTestUser.getId(), new Date(0), new Date(), getTime(0,0), getTime(23,59));
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException me) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}

	@Test
	public void testfindUserMealsInDateAndTimeRangesNoMealsForDateRange() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = model.findUserMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(62, 0, 0), getDateDaysAgoAtTime(61, 0, 0), getTime(0,0), getTime(23,59));
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException me) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}

	@Test
	public void testfindUserMealsInDateAndTimeRangesNoMealsForTimeRange() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = model.findUserMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(60, 0, 0), getDateDaysAgoAtTime(0, 0, 0), getTime(0,0), getTime(8,30));
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException me) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}
	
	@Test
	public void testfindUserMealsInDateAndTimeRangesStartEndDatesSame() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = model.findUserMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(60, 0, 0), getDateDaysAgoAtTime(60, 0, 0), getTime(0,0), getTime(23,59));
			assertNotNull("Meals should have been found", meals);
			assertEquals("Unexpected number of meals found", 3, meals.size());
		} catch (RepositoryException me) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}
	
	@Test
	public void testfindUserMealsInDateAndTimeRangesStartEndTimesSame() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = model.findUserMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(60, 0, 0), getDateDaysAgoAtTime(0, 0, 0), getTime(9,0), getTime(9,0));
			assertNotNull("Meals should have been found", meals);
			assertEquals("Unexpected number of meals found", 61, meals.size());
		} catch (RepositoryException me) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}
	
	@Test
	public void testfindUserMealsInDateAndTimeRangesAllMeals() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = model.findUserMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(60, 0, 0), getDateDaysAgoAtTime(0, 0, 0), getTime(0,0), getTime(23,0));
			assertNotNull("Meals should have been found", meals);
			assertEquals("Unexpected number of meals found", 183, meals.size());
		} catch (RepositoryException me) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}
	

	
}
