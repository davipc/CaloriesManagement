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

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.toptal.calories.resources.RepositoryException;
import com.toptal.calories.resources.TestDBBase;
import com.toptal.calories.resources.entity.Gender;
import com.toptal.calories.resources.entity.Role;
import com.toptal.calories.resources.entity.User;
import com.toptal.calories.resources.repository.Users;

public class TestUsers extends TestDBBase {

	public static Logger logger = Logger.getLogger(TestUsers.class);

	protected static Users model = new Users();

	private static SimpleDateFormat sdf = new SimpleDateFormat("HHmmssSSS");
	private static String login = sdf.format(new Date());
	private static String otherLogin = "2_" + login.substring(2);

	private static int invalidId = -1;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
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
			} catch (RepositoryException me) {
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
		user = model.createOrUpdate(testUser);
		if (user == null) {
			throw new RepositoryException("Null User returned from create!"); 
		}
		return user;
	}

	public static void removeUser(User testUser) throws RepositoryException {
		if (testUser == null || testUser.getId() == null ) {
			throw new RepositoryException("Null entity or Id received: " + testUser);
		}
		model.remove(testUser.getId());
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
			testUser = model.createOrUpdate(testUser);

			User user = model.find(testUser.getId());
			assertNotNull("Customer was expected to be found: " + testUser, user);
			assertEquals("Inserted and found users didn't match: ", testUser, user);
			
			user.setName(otherLogin);
			User updUser = model.createOrUpdate(user);
			// Update the value on local object to compare after DB update
			user = model.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			model.remove(testUser.getId());
			removed = true;
			user = model.find(testUser.getId());
			assertNull("User was expected NOT to be found: " + testUser, user);
		} catch (Exception e) {
			printException(e);
			fail(e.getMessage());
		} finally {
			if (!removed && testUser != null && testUser.getId() != null) {
				try {
					model.remove(testUser.getId());
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
			testUser = model.createOrUpdate(testUser);
			// create a second User
			otherUser = getUser(otherLogin);
			otherUser = model.createOrUpdate(otherUser);
			
			List<User> Users = model.findAll();
			assertNotNull("At least two users should have been found", Users);
			assertTrue("At least two users should have been found", Users.size() >= 2);
			assertTrue("Created users were not found in the returned list", Users.contains(testUser) && Users.contains(otherUser));
		} catch (RepositoryException me) {
			fail("Get All Users should NOT have caused a ModelException!");
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
			model.createOrUpdate(null);
			fail("Null User should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User should have caused a ModelException!");
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
			model.createOrUpdate(user);
			fail("Null User Login should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User Login should have caused a ModelException!");
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
			model.createOrUpdate(user);
			fail("Null User Name should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User Name should have caused a ModelException!");
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
			model.createOrUpdate(user);
			fail("Null User Password should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User Password should have caused a ModelException!");
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
			model.createOrUpdate(user);
			fail("Null User Gender should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User Gender should have caused a ModelException!");
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
			model.createOrUpdate(user);
			fail("Null User daily calories should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User daily calories should have caused a ModelException!");
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
			model.createOrUpdate(user);
			fail("Null User creation date should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User creation date should have caused a ModelException!");
		}
	}

	@Test
	public void testFindUserNullUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.find(null);
			fail("Null User ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a ModelException!");
		}
	}

	@Test
	public void testFindUserInvalidUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			User user = (User)model.find(invalidId);
			assertNull("No Users should have been found for invalid User ID", user);
		} catch (RepositoryException me) {
			fail("Invalid User ID should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Invalid User ID should NOT have caused an Exception!");
		}
	}
	
	@Test
	public void testUpdateUserNullUser() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.createOrUpdate(null);
			fail("Null User should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User should have caused a ModelException!");
		}
	}

	@Test
	public void testUpdateUserNullUserLogin() {
		logger.debug("Running " + getCurrentMethodName());
		User user = getUser(login);
		try {
			// create User for updating
			user = model.createOrUpdate(user);
			user.setLogin(null);
			user = model.createOrUpdate(user);
			fail("Null User ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a ModelException!");
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
			user = model.createOrUpdate(user);
			user.setName(null);
			user = model.createOrUpdate(user);
			fail("Null User name should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User name should have caused a ModelException!");
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
			user = model.createOrUpdate(user);
			user.setPassword(null);
			user = model.createOrUpdate(user);
			fail("Null User Password should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User Password should have caused a ModelException!");
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
			user = model.createOrUpdate(user);
			user.setGender(null);
			user = model.createOrUpdate(user);
			fail("Null User gender should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User gender should have caused a ModelException!");
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
			user = model.createOrUpdate(user);
			user.setDailyCalories(null);
			user = model.createOrUpdate(user);
			fail("Null User daily calories should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User daily calories should have caused a ModelException!");
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
			user = model.createOrUpdate(user);
			user.setCreationDt(null);
			user = model.createOrUpdate(user);
			fail("Null User creation date should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User creation date should have caused a ModelException!");
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
			model.remove(null);
			fail("Null User ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null User ID should have caused a ModelException!");
		}
	}

	@Test
	public void testRemoveUserInvalidUserId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.remove(invalidId);
			fail("Invalid User ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Invalid User ID should have caused a ModelException!");
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
			testUser = model.createOrUpdate(testUser);

			rolesToRemove = new ArrayList<>(testUser.getRoles());
			
			User user = model.find(testUser.getId());
			assertNotNull("User was expected to be found: " + testUser, user);
			assertEquals("Inserted and found users didn't match: ", testUser, user);
			
			user.setName(login+" with other name");
			User updUser = model.createOrUpdate(user);
			// Update the value on local object to compare after DB update
			user = model.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			// now test adding a role
			updUser = user;

			Role testRole = TestRoles.getRole("r" + (numRoles+1) + login.substring(2));
			try {
				testRole = TestRoles.createRole(testRole);
				updUser.getRoles().add(testRole);
				rolesToRemove.add(testRole);
			} catch (RepositoryException me) {
				if (testRole != null && testRole.getId() != null) {
					TestRoles.removeRole(testRole);
				}
			}
			
			updUser = model.createOrUpdate(updUser);
			user = model.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);

			// now test removing a role
			updUser.getRoles().remove(0);
			updUser = model.createOrUpdate(updUser);
			user = model.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			// now test removing all roles
			updUser.setRoles(null);
			updUser = model.createOrUpdate(updUser);
			user = model.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			// finally test removal of the entire user hierarchy		
			model.remove(testUser.getId());
			removed = true;
			user = model.find(testUser.getId());
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
			testUser = model.createOrUpdate(testUser);

			rolesToRemove = new ArrayList<>(testUser.getRoles());
			
			User user = model.find(testUser.getId());
			assertNotNull("User was expected to be found: " + testUser, user);
			assertEquals("Inserted and found users didn't match: ", testUser, user);
			
			user.setName(login+" with other name");
			User updUser = model.createOrUpdate(user);
			// Update the value on local object to compare after DB update
			user = model.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			// now test adding a role
			updUser = user;

			Role testRole = TestRoles.getRole("r"+ (numRoles+1) + login.substring(2));
			try {
				testRole = TestRoles.createRole(testRole);
				updUser.getRoles().add(testRole);
				rolesToRemove.add(testRole);
			} catch (RepositoryException me) {
				if (testRole != null && testRole.getId() != null) {
					TestRoles.removeRole(testRole);
				}
			}
			
			updUser = model.createOrUpdate(updUser);
			user = model.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);

			// now test removing a role
			updUser.getRoles().remove(0);
			updUser = model.createOrUpdate(updUser);
			user = model.find(testUser.getId());
			assertNotNull("User was expected to be found: " + user, user);
			assertEquals("Updated and found users didn't match: ", updUser, user);
			
			// finally test removal of the entire user hierarchy (while still having roles)		
			model.remove(testUser.getId());
			removed = true;
			user = model.find(testUser.getId());
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
	
}
