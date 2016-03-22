package com.toptal.calories.resources.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.toptal.calories.resources.entity.Gender;
import com.toptal.calories.resources.entity.Meal;
import com.toptal.calories.resources.entity.Role;
import com.toptal.calories.resources.entity.User;

public class TestUsers extends TestDBBase {

	public static Logger logger = LoggerFactory.getLogger(TestUsers.class);

	protected static Users repository = new RepositoryFactory().createRepository(Users.class);

	private static SimpleDateFormat sdf = new SimpleDateFormat("HHmmssSSS");
	private static String login = sdf.format(new Date());
	private static String otherLogin = "2_" + login.substring(2);

	private static User testUser;
	private static User otherTestUser;
	private static String thirdLogin = "3_" + login.substring(2);
	private static User thirdTestUser;
	
	private static int invalidId = -1;
	
	SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
	Date mealDate1;
	Date mealTime1;

	Date mealDate2;
	Date mealTime2;

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

	public static User getUser(String login) {
		User user = new User();
		user.setLogin(login);
		user.setName(login + " is my name");
		user.setPassword("123");
		user.setCreationDt(new Timestamp(System.currentTimeMillis()));
		user.setGender(Gender.F);
		user.setDailyCalories(1500);
		
		return user;
	}

	public static User getUserWithRoles(String login, int numRoles) 
	throws RepositoryException {
		User user = new User();
		user.setLogin(login);
		user.setName(login + " is my name");
		user.setPassword("456");
		user.setCreationDt(new Timestamp(System.currentTimeMillis()));
		user.setGender(Gender.M);
		user.setDailyCalories(2000);

		List<Role> roles = new ArrayList<>();
		
		for (int i = 1; i <= numRoles; i++) {
			Role testRole = TestRoles.getRole("r" + i + login.substring(2));
			try {
				testRole = TestRoles.createRole(testRole);
				roles.add(testRole);
			} catch (RepositoryException re) {
				for (Role role: roles) {
					if (role != null && role.getId() != null) {
						TestRoles.removeRole(role);
					}
				}
			}
		}
		user.setRoles(roles);
		
		return user;
	}
	
	
	public static User createUser(User testUser) throws RepositoryException {
		User user = null;
		if (testUser == null) {
			throw new RepositoryException("Null test User received for create!"); 
		}
		user = repository.createOrUpdate(testUser);
		if (user == null) {
			throw new RepositoryException("Null User returned from create!"); 
		}
		return user;
	}

	public static void removeUser(User testUser) throws RepositoryException {
		if (testUser == null || testUser.getId() == null ) {
			throw new RepositoryException("Null entity or Id received: " + testUser);
		}
		repository.remove(testUser.getId());
	}
	
	
	/******************************************************************************/
	/******************************************************************************/
	/**  		            USER ONLY tests                                  **/											
	/******************************************************************************/
	/******************************************************************************/
	
	/******************************************************************************/
	/**  		            CRUD tests                                           **/											
	/******************************************************************************/
	
	@Test
	public void testSuccessEnd2End() {
		logger.debug("Running " + getCurrentMethodName());
		User testUser = getUser(login);
		boolean removed = false;
		try {
			testUser = repository.createOrUpdate(testUser);

			User user = repository.find(testUser.getId());
			assertNotNull("Customer was expected to be found: " + testUser, user);
			assertEquals("Inserted and found users didn't match: ", testUser, user);
			
			user.setName(otherLogin);
			User updUser = repository.createOrUpdate(user);
			// Update the value on local object to compare after DB update
			user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			removed = repository.remove(testUser.getId());
			assertTrue("Entity should have been removed!", removed);
			user = repository.find(testUser.getId());
			assertNull("User was expected NOT to be found: " + testUser, user);
		} catch (Exception e) {
			printException(e);
			fail(e.getMessage());
		} finally {
			if (!removed && testUser != null && testUser.getId() != null) {
				try {
					repository.remove(testUser.getId());
				} catch (Exception e) {
					logger.error("In Finally Block: Failed to remove entity", e);
				}
			}
		}
	}
	
	@Test
	public void testGetAllUsers() {
		logger.debug("Running " + getCurrentMethodName());
		User testUser = null;
		User otherUser = null;
		try {
			// create test User
			testUser = getUser(login);
			testUser = repository.createOrUpdate(testUser);
			// create a second User
			otherUser = getUser(otherLogin);
			otherUser = repository.createOrUpdate(otherUser);
			
			List<User> Users = repository.findAll();
			assertNotNull("At least two users should have been found", Users);
			assertTrue("At least two users should have been found", Users.size() >= 2);
			assertTrue("Created users were not found in the returned list", Users.contains(testUser) && Users.contains(otherUser));
		} catch (RepositoryException re) {
			fail("Get All Users should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Get All Users should NOT have caused an Exception!");
		} finally {
			try {
				if (testUser != null && testUser.getId() != null) {
					removeUser(testUser);
				}
			} catch (Exception e) {
				logger.error("In Finally Block: Failed to remove entity", e);
			}
			try {
				if (otherUser != null && otherUser.getId() != null) {
					removeUser(otherUser);
				}
			} catch (Exception e) {
				logger.error("In Finally Block: Failed to remove entity", e);
			}
		}
	}
	
	/**
	 * Tests null User sent for creation 
	 */
	@Test
	public void testCreateUserNullUser() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.createOrUpdate(null);
			fail("Null User should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User should have caused a RepositoryExcpetion!");
		}
	}

	/**
	 * Tests null User login sent for creation 
	 */
	@Test
	public void testCreateUserNullLogin() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			User user = getUser(null);
			repository.createOrUpdate(user);
			fail("Null User Login should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User Login should have caused a RepositoryExcpetion!");
		}
	}

	/**
	 * Tests null User name sent for creation 
	 */
	@Test
	public void testCreateUserNullName() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			User user = getUser(login);
			user.setName(null);
			repository.createOrUpdate(user);
			fail("Null User Name should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User Name should have caused a RepositoryExcpetion!");
		}
	}
	
	/**
	 * Tests null User password sent for creation 
	 */
	@Test
	public void testCreateUserNullPassword() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			User user = getUser(login);
			user.setPassword(null);
			repository.createOrUpdate(user);
			fail("Null User Password should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User Password should have caused a RepositoryExcpetion!");
		}
	}

	/**
	 * Tests null User gender sent for creation 
	 */
	@Test
	public void testCreateUserNullGender() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			User user = getUser(login);
			user.setGender(null);
			repository.createOrUpdate(user);
			fail("Null User Gender should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User Gender should have caused a RepositoryExcpetion!");
		}
	}

	/**
	 * Tests null User daily calories sent for creation 
	 */
	@Test
	public void testCreateUserNullDailyCalories() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			User user = getUser(login);
			user.setDailyCalories(null);
			repository.createOrUpdate(user);
			fail("Null User daily calories should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User daily calories should have caused a RepositoryExcpetion!");
		}
	}

	/**
	 * Tests null User creation date sent for creation 
	 */
	@Test
	public void testCreateUserNullCreationDt() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			User user = getUser(login);
			user.setCreationDt(null);
			repository.createOrUpdate(user);
			fail("Null User creation date should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User creation date should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testFindUserNullUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.find(null);
			fail("Null User ID should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testFindUserInvalidUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			User user = (User)repository.find(invalidId);
			assertNull("No Users should have been found for invalid User ID", user);
		} catch (RepositoryException re) {
			fail("Invalid User ID should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Invalid User ID should NOT have caused an Exception!");
		}
	}
	
	@Test
	public void testUpdateUserNullUser() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.createOrUpdate(null);
			fail("Null User should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testUpdateUserNullUserLogin() {
		logger.debug("Running " + getCurrentMethodName());
		User user = getUser(login);
		try {
			// create User for updating
			user = repository.createOrUpdate(user);
			user.setLogin(null);
			user = repository.createOrUpdate(user);
			fail("Null User ID should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a RepositoryExcpetion!");
		} finally {
			if (user != null && user.getId() != null) {
				try {
					removeUser(user);
				} catch (Exception e) {
					printException(e);
				}
			}
		}
	}
	
	@Test
	public void testUpdateUserNullUserName() {
		logger.debug("Running " + getCurrentMethodName());
		User user = getUser(login);
		try {
			// create User for updating
			user = repository.createOrUpdate(user);
			user.setName(null);
			user = repository.createOrUpdate(user);
			fail("Null User name should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User name should have caused a RepositoryExcpetion!");
		} finally {
			if (user != null && user.getId() != null) {
				try {
					removeUser(user);
				} catch (Exception e) {
					printException(e);
				}
			}
		}
	}
	
	@Test
	public void testUpdateUserNullUserPassword() {
		logger.debug("Running " + getCurrentMethodName());
		User user = getUser(login);
		try {
			// create User for updating
			user = repository.createOrUpdate(user);
			user.setPassword(null);
			user = repository.createOrUpdate(user);
			fail("Null User Password should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User Password should have caused a RepositoryExcpetion!");
		} finally {
			if (user != null && user.getId() != null) {
				try {
					removeUser(user);
				} catch (Exception e) {
					printException(e);
				}
			}
		}
	}
	
	@Test
	public void testUpdateUserNullUserGender() {
		logger.debug("Running " + getCurrentMethodName());
		User user = getUser(login);
		try {
			// create User for updating
			user = repository.createOrUpdate(user);
			user.setGender(null);
			user = repository.createOrUpdate(user);
			fail("Null User gender should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User gender should have caused a RepositoryExcpetion!");
		} finally {
			if (user != null && user.getId() != null) {
				try {
					removeUser(user);
				} catch (Exception e) {
					printException(e);
				}
			}
		}
	}
	
	@Test
	public void testUpdateUserNullUserDailyCalories() {
		logger.debug("Running " + getCurrentMethodName());
		User user = getUser(login);
		try {
			// create User for updating
			user = repository.createOrUpdate(user);
			user.setDailyCalories(null);
			user = repository.createOrUpdate(user);
			fail("Null User daily calories should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User daily calories should have caused a RepositoryExcpetion!");
		} finally {
			if (user != null && user.getId() != null) {
				try {
					removeUser(user);
				} catch (Exception e) {
					printException(e);
				}
			}
		}
	}
	
	@Test
	public void testUpdateUserNullUserCreationDate() {
		logger.debug("Running " + getCurrentMethodName());
		User user = getUser(login);
		try {
			// create User for updating
			user = repository.createOrUpdate(user);
			user.setCreationDt(null);
			user = repository.createOrUpdate(user);
			fail("Null User creation date should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User creation date should have caused a RepositoryExcpetion!");
		} finally {
			if (user != null && user.getId() != null) {
				try {
					removeUser(user);
				} catch (Exception e) {
					printException(e);
				}
			}
		}
	}
	
	
	@Test
	public void testRemoveUserNullUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.remove(null);
			fail("Null User ID should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testRemoveUserInvalidUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			boolean removed = repository.remove(invalidId);
			assertTrue("No entity should have been removed", !removed);
		} catch (RepositoryException re) {
			printException(re);
			fail("Invalid User ID should  NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Invalid User ID should NOT have caused a RepositoryExcpetion!");
		}
	}
	
	/******************************************************************************/
	/******************************************************************************/
	/**  		            HIERARCHY tests                                      **/											
	/******************************************************************************/
	/******************************************************************************/

	@Test
	public void testSuccessEnd2End_HierarchyOneRole() {
		logger.debug("Running " + getCurrentMethodName());
		User testUser = null;
		boolean removed = false;
		List<Role> rolesToRemove = null;
		int numRoles = 1;
		try {
			testUser = getUserWithRoles(login, numRoles);
			testUser = repository.createOrUpdate(testUser);

			rolesToRemove = new ArrayList<>(testUser.getRoles());
			
			User user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + testUser, user);
			assertEquals("Inserted and found users didn't match: ", testUser, user);
			
			user.setName(login+" with other name");
			User updUser = repository.createOrUpdate(user);
			// Update the value on local object to compare after DB update
			user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			// now test adding a role
			updUser = user;

			Role testRole = TestRoles.getRole("r" + (numRoles+1) + login.substring(2));
			try {
				testRole = TestRoles.createRole(testRole);
				updUser.getRoles().add(testRole);
				rolesToRemove.add(testRole);
			} catch (RepositoryException re) {
				if (testRole != null && testRole.getId() != null) {
					TestRoles.removeRole(testRole);
				}
			}
			
			updUser = repository.createOrUpdate(updUser);
			user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);

			// now test removing a role
			updUser.getRoles().remove(0);
			updUser = repository.createOrUpdate(updUser);
			user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			// now test removing all roles
			updUser.setRoles(null);
			updUser = repository.createOrUpdate(updUser);
			user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			// finally test removal of the entire user hierarchy		
			removed = repository.remove(testUser.getId());
			assertTrue("Entity should have been removed!", removed);
			user = repository.find(testUser.getId());
			assertNull("User was expected NOT to be found: " + testUser, user);
		} catch (Exception e) {
			printException(e);
			fail(e.getMessage());
		} finally {
			if (!removed && testUser != null && testUser.getId() != null) {
				try {
					removeUser(testUser);
				} catch (Exception e) {
					logger.error("In Finally Block: Failed to remove entity", e);
				}
			}
			
			if (testUser != null && testUser.getId() != null) {
				Exception failure = null;
				for (Role role: rolesToRemove) {
					try {
						TestRoles.removeRole(role);
					} catch (RepositoryException e) {
						failure = e;
						System.out.println("Error trying to remove test role " + role);
						e.printStackTrace();
					}
				}
				if (failure != null) 
					fail("Error deleting test role: " + failure.getMessage());
			}
		}
	}

	@Test
	public void testSuccessEnd2End_HierarchyTwoRoles() {
		logger.debug("Running " + getCurrentMethodName());
		User testUser = null;
		boolean removed = false;
		List<Role> rolesToRemove = null;
		int numRoles = 2;
		try {
			testUser = getUserWithRoles(login, numRoles);
			testUser = repository.createOrUpdate(testUser);

			rolesToRemove = new ArrayList<>(testUser.getRoles());
			
			User user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + testUser, user);
			assertEquals("Inserted and found users didn't match: ", testUser, user);
			
			user.setName(login+" with other name");
			User updUser = repository.createOrUpdate(user);
			// Update the value on local object to compare after DB update
			user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			// now test adding a role
			updUser = user;

			Role testRole = TestRoles.getRole("r"+ (numRoles+1) + login.substring(2));
			try {
				testRole = TestRoles.createRole(testRole);
				updUser.getRoles().add(testRole);
				rolesToRemove.add(testRole);
			} catch (RepositoryException re) {
				if (testRole != null && testRole.getId() != null) {
					TestRoles.removeRole(testRole);
				}
			}
			
			updUser = repository.createOrUpdate(updUser);
			user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);

			// now test removing a role
			updUser.getRoles().remove(0);
			updUser = repository.createOrUpdate(updUser);
			user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			// finally test removal of the entire user hierarchy (while still having roles)		
			removed = repository.remove(testUser.getId());
			assertTrue("Entity should have been removed!", removed);
			user = repository.find(testUser.getId());
			assertNull("User was expected NOT to be found: " + testUser, user);
		} catch (Exception e) {
			printException(e);
			fail(e.getMessage());
		} finally {
			if (!removed && testUser != null && testUser.getId() != null) {
				try {
					removeUser(testUser);
				} catch (Exception e) {
					logger.error("In Finally Block: Failed to remove entity", e);
				}
			}
			
			if (testUser != null && testUser.getId() != null) {
				Exception failure = null; 
				for (Role role: rolesToRemove) {
					try {
						TestRoles.removeRole(role);
					} catch (RepositoryException e) {
						failure = e;
						System.out.println("Error trying to remove test role " + role);
						e.printStackTrace();
					}
				}
				if (failure != null) 
					fail("Error deleting test role: " + failure.getMessage());
			}
		}
	}
	
	@Test
	public void testRoleInfoNotChangedOnUserUpdate() {
		logger.debug("Running " + getCurrentMethodName());
		User testUser = null;
		boolean removed = false;
		List<Role> rolesToRemove = null;
		int numRoles = 2;
		try {
			testUser = getUserWithRoles(login, numRoles);
			testUser = repository.createOrUpdate(testUser);

			rolesToRemove = new ArrayList<>(testUser.getRoles());
			
			User user = repository.find(testUser.getId());
			assertNotNull("User was expected to be found: " + testUser, user);
			assertEquals("Inserted and found users didn't match: ", testUser, user);

			// just remove the first char from the name
			user.getRoles().get(0).setName(user.getRoles().get(0).getName().substring(1));
			
			user = repository.createOrUpdate(user);
			
			// Update the value on local object to compare after DB update
			user = repository.find(testUser.getId());

			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Found and initial users didn't match: ", user, testUser);
			
		} catch (Exception e) {
			printException(e);
			fail(e.getMessage());
		} finally {
			if (!removed && testUser != null && testUser.getId() != null) {
				try {
					removeUser(testUser);
				} catch (Exception e) {
					logger.error("In Finally Block: Failed to remove entity", e);
				}
			}
			
			if (testUser != null && testUser.getId() != null) {
				Exception failure = null; 
				for (Role role: rolesToRemove) {
					try {
						TestRoles.removeRole(role);
					} catch (RepositoryException e) {
						failure = e;
						System.out.println("Error trying to remove test role " + role);
						e.printStackTrace();
					}
				}
				if (failure != null) 
					fail("Error deleting test role: " + failure.getMessage());
			}
		}
	}	
	
	/******************************************************************************/
	/** Find user meals tests                                                    **/
	/******************************************************************************/
	
	@Test
	public void testFindUserMealsNullUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.find(null);
			fail("Null User ID should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a RepositoryExcpetion!");
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
			testMeal = TestMeals.getMeal(testUser, mealDate1, mealTime1);
			testMeal = TestMeals.createMeal(testMeal);
			// create a second Meal
			secondMeal = TestMeals.getMeal(testUser, mealDate2, mealTime2);
			secondMeal = TestMeals.createMeal(secondMeal);
			// create a third Meal
			thirdMeal = TestMeals.getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = TestMeals.createMeal(thirdMeal);
			// create a fourth Meal
			fourthMeal = TestMeals.getMeal(otherTestUser, mealDate2, mealTime2);
			fourthMeal = TestMeals.createMeal(fourthMeal);

			// find the user owning the meals
			User user = repository.find(invalidId);
			assertNull("User should be null", user);
		} catch (RepositoryException re) {
			fail("Find User meals should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					TestMeals.removeMeal(testMeal);
				}
				if (secondMeal != null) {
					TestMeals.removeMeal(secondMeal);
				}
				if (thirdMeal != null) {
					TestMeals.removeMeal(thirdMeal);
				}
				if (fourthMeal != null) {
					TestMeals.removeMeal(fourthMeal);
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
			testMeal = TestMeals.getMeal(testUser, mealDate1, mealTime1);
			testMeal = TestMeals.createMeal(testMeal);
			// create a second Meal
			secondMeal = TestMeals.getMeal(testUser, mealDate2, mealTime2);
			secondMeal = TestMeals.createMeal(secondMeal);
			// create a third Meal
			thirdMeal = TestMeals.getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = TestMeals.createMeal(thirdMeal);
			// create a fourth Meal
			fourthMeal = TestMeals.getMeal(otherTestUser, mealDate2, mealTime2);
			fourthMeal = TestMeals.createMeal(fourthMeal);
			
			User user = repository.find(thirdTestUser.getId());
			assertNotNull("Valid user should have been found", user);
			
			List<Meal> meals = user.getMeals();
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException re) {
			fail("Find User meals should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					TestMeals.removeMeal(testMeal);
				}
				if (secondMeal != null) {
					TestMeals.removeMeal(secondMeal);
				}
				if (thirdMeal != null) {
					TestMeals.removeMeal(thirdMeal);
				}
				if (fourthMeal != null) {
					TestMeals.removeMeal(fourthMeal);
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
			testMeal = TestMeals.getMeal(testUser, mealDate1, mealTime1);
			testMeal = TestMeals.createMeal(testMeal);
			// create a second Meal
			secondMeal = TestMeals.getMeal(testUser, mealDate2, mealTime2);
			secondMeal = TestMeals.createMeal(secondMeal);
			// create a third Meal
			thirdMeal = TestMeals.getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = TestMeals.createMeal(thirdMeal);
			
			User user = repository.find(otherTestUser.getId());
			assertNotNull("Valid user should have been found", user);
			
			List<Meal> meals = user.getMeals();
			assertNotNull("One meal should have been found", meals);
			assertEquals("One meal should have been found", 1, meals.size());
			assertTrue("Created meals were not found in the returned list", meals.contains(thirdMeal));
		} catch (RepositoryException re) {
			fail("Find User meals should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					TestMeals.removeMeal(testMeal);
				}
				if (secondMeal != null) {
					TestMeals.removeMeal(secondMeal);
				}
				if (thirdMeal != null) {
					TestMeals.removeMeal(thirdMeal);
				}
				if (fourthMeal != null) {
					TestMeals.removeMeal(fourthMeal);
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
			testMeal = TestMeals.getMeal(testUser, mealDate1, mealTime1);
			testMeal = TestMeals.createMeal(testMeal);
			// create a second Meal
			secondMeal = TestMeals.getMeal(testUser, mealDate2, mealTime2);
			secondMeal = TestMeals.createMeal(secondMeal);
			// create a third Meal
			thirdMeal = TestMeals.getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = TestMeals.createMeal(thirdMeal);
			// create a fourth Meal
			fourthMeal = TestMeals.getMeal(otherTestUser, mealDate2, mealTime2);
			fourthMeal = TestMeals.createMeal(fourthMeal);
			
			User user = repository.find(testUser.getId());
			assertNotNull("Valid user should have been found", user);
			
			List<Meal> meals = user.getMeals();
			assertNotNull("Two meals should have been found", meals);
			assertEquals("Two meals should have been found", 2, meals.size());
			assertTrue("Created meals were not found in the returned list", meals.contains(testMeal) && meals.contains(secondMeal));
		} catch (RepositoryException re) {
			fail("Find User meals should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					TestMeals.removeMeal(testMeal);
				}
				if (secondMeal != null) {
					TestMeals.removeMeal(secondMeal);
				}
				if (thirdMeal != null) {
					TestMeals.removeMeal(thirdMeal);
				}
				if (fourthMeal != null) {
					TestMeals.removeMeal(fourthMeal);
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
						
						meal = TestMeals.getMeal(user, mealTime, mealTime);
						meal = TestMeals.createMeal(meal);
						dateTimeRangesTestMeals.add(meal);
					}
				}
			}
		} catch (RepositoryException re) {
			logger.error("Error creating test meals for date range test", re);

			try {
				for (Meal toRemove: dateTimeRangesTestMeals) {
					TestMeals.removeMeal(toRemove);
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
				TestMeals.removeMeal(toRemove);
			} catch (RepositoryException re) {
				logger.error("Error removing test meals", re);
			}
		}
		dateTimeRangesTestMeals.clear();
	}
	
	/******************************************************************************/
	/** Actual tests 	                                                         **/
	/******************************************************************************/
	
	@Test
	public void testFindUserMealsInDateAndTimeRangesNullUser() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.findMealsInDateAndTimeRanges(null, new Date(0), new Date(), getTime(0,0), getTime(23,59));
			fail("Null User ID should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testFindUserMealsInDateAndTimeRangesNullFromDate() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.findMealsInDateAndTimeRanges(testUser.getId(), null, new Date(), getTime(0,0), getTime(23,59));
			fail("Null start date should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null end date should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testFindUserMealsInDateAndTimeRangesNullToDate() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.findMealsInDateAndTimeRanges(testUser.getId(), new Date(0), null, getTime(0,0), getTime(23,59));
			fail("Null end date should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null end date should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testFindUserMealsInDateAndTimeRangesNullFromTime() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.findMealsInDateAndTimeRanges(testUser.getId(), new Date(0), new Date(), null, getTime(23,59));
			fail("Null start time should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null start time  should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testFindUserMealsInDateAndTimeRangesNullToTime() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.findMealsInDateAndTimeRanges(testUser.getId(), new Date(0), new Date(), getTime(0,0), null);
			fail("Null end time should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null end time should have caused a RepositoryExcpetion!");
		}
	}
	
	@Test
	public void testFindUserMealsInDateAndTimeRangesStartDateAfterEndDate() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// start date = today, end date = yesterday
			repository.findMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(0, 0, 0), getDateDaysAgoAtTime(1, 0, 0), getTime(0,0), getTime(23,59));
			fail("Invalid start and end dates should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Invalid start and end dates should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testFindUserMealsInDateAndTimeRangesStartTimeAfterEndTime() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.findMealsInDateAndTimeRanges(testUser.getId(), new Date(0), new Date(), getTime(15,10), getTime(15,9));
			fail("Invalid start and end times should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Invalid start and end times should have caused a RepositoryExcpetion!");
		}
	}

	
	@Test
	public void testFindUserMealsInDateAndTimeRangesInvalidUser() {
		logger.debug("Running " + getCurrentMethodName());
		Meal testMeal = null;
		Meal secondMeal = null;
		Meal thirdMeal = null;
		Meal fourthMeal = null;
		try {
			// create test Meal
			testMeal = TestMeals.getMeal(testUser, mealDate1, mealTime1);
			testMeal = TestMeals.createMeal(testMeal);
			// create a second Meal
			secondMeal = TestMeals.getMeal(testUser, mealDate2, mealTime2);
			secondMeal = TestMeals.createMeal(secondMeal);
			// create a third Meal
			thirdMeal = TestMeals.getMeal(otherTestUser, mealDate1, mealTime1);
			thirdMeal = TestMeals.createMeal(thirdMeal);
			
			List<Meal> meals = repository.findMealsInDateAndTimeRanges(invalidId, new Date(0), new Date(), getTime(0,0), getTime(23,59));
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException re) {
			fail("testDateFuncsFourMeals should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("testDateFuncsFourMeals should NOT have caused an Exception!");
		} finally {
			try {
				if (testMeal != null) {
					TestMeals.removeMeal(testMeal);
				}
				if (secondMeal != null) {
					TestMeals.removeMeal(secondMeal);
				}
				if (thirdMeal != null) {
					TestMeals.removeMeal(thirdMeal);
				}
				if (fourthMeal != null) {
					TestMeals.removeMeal(fourthMeal);
				}
			} catch (Exception e) {
				logger.error("In Finally Block: Failed to remove entities", e);
			}
		}
	}

	
	@Test
	public void testFindUserMealsInDateAndTimeRangesNoMealsForUser() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = repository.findMealsInDateAndTimeRanges(thirdTestUser.getId(), new Date(0), new Date(), getTime(0,0), getTime(23,59));
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException re) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}

	@Test
	public void testFindUserMealsInDateAndTimeRangesNoMealsForDateRange() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = repository.findMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(62, 0, 0), getDateDaysAgoAtTime(61, 0, 0), getTime(0,0), getTime(23,59));
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException re) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}

	@Test
	public void testFindUserMealsInDateAndTimeRangesNoMealsForTimeRange() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = repository.findMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(60, 0, 0), getDateDaysAgoAtTime(0, 0, 0), getTime(0,0), getTime(8,30));
			assertNotNull("No meals should have been found", meals);
			assertEquals("No meals should have been found", 0, meals.size());
		} catch (RepositoryException re) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}
	
	@Test
	public void testFindUserMealsInDateAndTimeRangesStartEndDatesSame() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = repository.findMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(60, 0, 0), getDateDaysAgoAtTime(60, 0, 0), getTime(0,0), getTime(23,59));
			assertNotNull("Meals should have been found", meals);
			assertEquals("Unexpected number of meals found", 3, meals.size());
		} catch (RepositoryException re) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}
	
	@Test
	public void testFindUserMealsInDateAndTimeRangesStartEndTimesSame() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = repository.findMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(60, 0, 0), getDateDaysAgoAtTime(0, 0, 0), getTime(9,0), getTime(9,0));
			assertNotNull("Meals should have been found", meals);
			assertEquals("Unexpected number of meals found", 61, meals.size());
		} catch (RepositoryException re) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}
	
	@Test
	public void testFindUserMealsInDateAndTimeRangesAllMeals() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			// create test meals
			prepareForDateTimeRangeTests();
			
			List<Meal> meals = repository.findMealsInDateAndTimeRanges(testUser.getId(), getDateDaysAgoAtTime(60, 0, 0), getDateDaysAgoAtTime(0, 0, 0), getTime(0,0), getTime(23,0));
			assertNotNull("Meals should have been found", meals);
			assertEquals("Unexpected number of meals found", 183, meals.size());
		} catch (RepositoryException re) {
			fail("Find User meals In Date And Time Ranges should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Find User meals In Date And Time Ranges should NOT have caused an Exception!");
		} finally {
			afterDateTimeRangeTest();
		}
	}
	

	
	
	
//	public static void main(String[] args) throws Exception {
//		User user = repository.find(1157);
//		System.out.println(user);
//
//		System.out.println("User Meals: " + (user != null ? user.getMeals() : "null"));
//	}
}
