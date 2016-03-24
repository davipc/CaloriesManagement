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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.TestDBBase;
import com.toptal.calories.resources.entity.Role;

public class TestRoles extends TestDBBase {

	public static Logger logger = LoggerFactory.getLogger(TestRoles.class);

	protected static Roles repository = new RepositoryFactory().createRepository(Roles.class, TestDBBase.CURRENT_TEST_ID);

	private static SimpleDateFormat sdf = new SimpleDateFormat("HHmmssSSS");
	private static String name = sdf.format(new Date());
	private static String otherName = "2_" + name.substring(2);
	
	private static int invalidId = -1;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		logger.warn("Current Test: " + TestDBBase.CURRENT_TEST_ID);
	}
	
	/******************************************************************************/
	/**  		            Helper Methods                                       **/											
	/******************************************************************************/

	public static Role getRole(String name) {
		Role role = new Role();
		role.setName(name);
		
		return role;
	}

	public static Role createRole(Role testRole) throws RepositoryException {
		Role role = null;
		if (testRole == null) {
			throw new RepositoryException("Null test Role received for create!"); 
		}
		role = repository.createOrUpdate(testRole);
		if (role == null) {
			throw new RepositoryException("Null Role returned from create!"); 
		}
		return role;
	}

	public static void removeRole(Role testRole) throws RepositoryException {
		if (testRole == null || testRole.getId() == null ) {
			throw new RepositoryException("Null entity or Id received: " + testRole);
		}
		repository.remove(testRole.getId());
	}
	
	public static void removeIfNotNull(Role...roles) 
	throws RepositoryException {
		for (Role role: roles) {
			if (role != null && role.getId() != null) {
				removeRole(role);
			}
		}
	}

	/******************************************************************************/
	/******************************************************************************/
	/**  		            ROLE ONLY tests                                  **/											
	/******************************************************************************/
	/******************************************************************************/
	
	/******************************************************************************/
	/**  		            CRUD tests                                           **/											
	/******************************************************************************/
	
	@Test
	public void testSuccessEnd2End() {
		logger.debug("Running " + getCurrentMethodName());
		Role testRole = getRole(name);
		boolean removed = false;
		try {
			testRole = repository.createOrUpdate(testRole);

			Role role = repository.find(testRole.getId());
			assertNotNull("Customer was expected to be found: " + testRole, role);
			assertEquals("Inserted and found roles didn't match: ", testRole, role);
			
			role.setName(otherName);
			repository.createOrUpdate(role);
			// Update the value on local object to compare after DB update
			role = repository.find(testRole.getId());
			assertNotNull("Role was expected to be found: " + role, role);
			assertEquals("Role name should have been updated: ", testRole, role);
			
			removed = repository.remove(testRole.getId());
			assertTrue("Entity should have been removed!", removed);
			role = repository.find(testRole.getId());
			assertNull("Role was expected NOT to be found: " + testRole, role);
		} catch (Exception e) {
			printException(e);
			fail(e.getMessage());
		} finally {
			if (!removed && testRole != null && testRole.getId() != null) {
				try {
					removeRole(testRole);
				} catch (Exception e) {
					logger.error("In Finally Block: Failed to remove entity", e);
				}
			}
		}
	}
	
	@Test
	public void testGetAllRoles() {
		logger.debug("Running " + getCurrentMethodName());
		Role testRole = null;
		Role otherRole = null;
		try {
			// create test Role
			testRole = getRole(name);
			testRole = repository.createOrUpdate(testRole);
			// create a second Role
			otherRole = getRole(otherName);
			otherRole = repository.createOrUpdate(otherRole);
			
			List<Role> Roles = repository.findAll();
			assertNotNull("At least two roles should have been found", Roles);
			assertTrue("At least two roles should have been found", Roles.size() >= 2);
			assertTrue("Created roles were not found in the returned list", Roles.contains(testRole) && Roles.contains(otherRole));
		} catch (RepositoryException re) {
			fail("Get All Roles should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Get All Roles should NOT have caused an Exception!");
		} finally {
			try {
				if (testRole != null) {
					removeRole(testRole);
				}
				if (otherRole != null) {
					removeRole(otherRole);
				}
			} catch (Exception e) {
				logger.error("In Finally Block: Failed to remove entities", e);
			}
		}
	}
	
	/**
	 * Tests null Role sent for creation 
	 */
	@Test
	public void testCreateRoleNullRole() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.createOrUpdate(null);
			fail("Null Role should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role should have caused a RepositoryExcpetion!");
		}
	}

	/**
	 * Tests null Role name sent for creation 
	 */
	@Test
	public void testCreateRoleNullName() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Role role = getRole(null);
			repository.createOrUpdate(role);
			fail("Null Role Name should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role Name should have caused a RepositoryExcpetion!");
		}
	}
	
	@Test
	public void testFindRoleNullRoleId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.find(null);
			fail("Null Role ID should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role ID should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testFindRoleInvalidRoleId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Role role = (Role)repository.find(invalidId);
			assertNull("No Roles should have been found for invalid Role ID", role);
		} catch (RepositoryException re) {
			fail("Invalid Role ID should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Invalid Role ID should NOT have caused an Exception!");
		}
	}
	
	@Test
	public void testUpdateRoleNullRole() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.createOrUpdate(null);
			fail("Null Role should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testUpdateRoleNullRoleName() {
		logger.debug("Running " + getCurrentMethodName());
		Role role = getRole(name);
		try {
			// create Role for updating
			role = repository.createOrUpdate(role);
			role.setName(null);
			role = repository.createOrUpdate(role);
			// update call will be made successfully, but won't cause the column to change (it's not updatable in the entity)
		} catch (RepositoryException re) {
			fail("Null Role name should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Null Role name should NOT have caused a RepositoryExcpetion!");
		} finally {
			if (role != null && role.getId() != null) {
				try {
					removeRole(role);
				} catch (Exception e) {
					printException(e);
				}
			}
		}
	}
	
	@Test
	public void testRemoveRoleNullRoleId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			repository.remove(null);
			fail("Null Role ID should have caused a RepositoryExcpetion!");
		} catch (RepositoryException re) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role ID should have caused a RepositoryExcpetion!");
		}
	}

	@Test
	public void testRemoveRoleInvalidRoleId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			boolean removed = repository.remove(invalidId);
			assertTrue("No entity should have been removed", !removed);
		} catch (RepositoryException re) {
			printException(re);
			fail("Invalid Role ID should NOT have caused a RepositoryExcpetion!");
		} catch (Exception e) {
			printException(e);
			fail("Invalid Role ID should NOT have caused a RepositoryExcpetion!");
		}
	}
	
}
