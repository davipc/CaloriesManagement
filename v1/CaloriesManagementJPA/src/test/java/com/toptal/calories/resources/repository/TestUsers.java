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
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.TestDBBase;
import com.toptal.calories.resources.entity.Gender;
import com.toptal.calories.resources.entity.Role;
import com.toptal.calories.resources.entity.User;

public class TestUsers extends TestDBBase {

	public static Logger logger = LoggerFactory.getLogger(TestUsers.class);

	protected static Users repository = new RepositoryFactory().createRepository(Users.class);

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
}
