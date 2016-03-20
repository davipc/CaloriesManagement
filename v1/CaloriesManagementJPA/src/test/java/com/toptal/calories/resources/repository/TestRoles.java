package com.toptal.calories.resources.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.toptal.calories.resources.RepositoryException;
import com.toptal.calories.resources.TestDBBase;
import com.toptal.calories.resources.entity.Role;
import com.toptal.calories.resources.repository.Roles;

public class TestRoles extends TestDBBase {

	public static Logger logger = Logger.getLogger(TestRoles.class);

	protected static Roles model = new Roles();

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
		role = model.createOrUpdate(testRole);
		if (role == null) {
			throw new RepositoryException("Null Role returned from create!"); 
		}
		return role;
	}

	public static void removeRole(Role testRole) throws RepositoryException {
		if (testRole == null || testRole.getId() == null ) {
			throw new RepositoryException("Null entity or Id received: " + testRole);
		}
		model.remove(testRole.getId());
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
			testRole = model.createOrUpdate(testRole);

			Role role = model.find(testRole.getId());
			assertNotNull("Customer was expected to be found: " + testRole, role);
			assertEquals("Inserted and found roles didn't match: ", testRole, role);
			
			role.setName(otherName);
			Role updRole = model.createOrUpdate(role);
			// Update the value on local object to compare after DB update
			role = model.find(testRole.getId());
			assertNotNull("Role was expected to be found: " + role, role);
			assertEquals("Updated and found roles didn't match: ", updRole, role);
			
			model.remove(testRole.getId());
			removed = true;
			role = model.find(testRole.getId());
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
			testRole = model.createOrUpdate(testRole);
			// create a second Role
			otherRole = getRole(otherName);
			otherRole = model.createOrUpdate(otherRole);
			
			List<Role> Roles = model.findAll();
			assertNotNull("At least two roles should have been found", Roles);
			assertTrue("At least two roles should have been found", Roles.size() >= 2);
			assertTrue("Created roles were not found in the returned list", Roles.contains(testRole) && Roles.contains(otherRole));
		} catch (RepositoryException me) {
			fail("Get All Roles should NOT have caused a ModelException!");
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
			model.createOrUpdate(null);
			fail("Null Role should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role should have caused a ModelException!");
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
			model.createOrUpdate(role);
			fail("Null Role Name should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role Name should have caused a ModelException!");
		}
	}
	
	@Test
	public void testFindRoleNullRoleId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.find(null);
			fail("Null Role ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role ID should have caused a ModelException!");
		}
	}

	@Test
	public void testFindRoleInvalidRoleId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			Role role = (Role)model.find(invalidId);
			assertNull("No Roles should have been found for invalid Role ID", role);
		} catch (RepositoryException me) {
			fail("Invalid Role ID should NOT have caused a ModelException!");
		} catch (Exception e) {
			printException(e);
			fail("Invalid Role ID should NOT have caused an Exception!");
		}
	}
	
	@Test
	public void testUpdateRoleNullRole() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.createOrUpdate(null);
			fail("Null Role should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role should have caused a ModelException!");
		}
	}

	@Test
	public void testUpdateRoleNullRoleName() {
		logger.debug("Running " + getCurrentMethodName());
		Role role = getRole(name);
		try {
			// create Role for updating
			role = model.createOrUpdate(role);
			role.setName(null);
			role = model.createOrUpdate(role);
			fail("Null Role name should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role name should have caused a ModelException!");
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
			model.remove(null);
			fail("Null Role ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Null Role ID should have caused a ModelException!");
		}
	}

	@Test
	public void testRemoveRoleInvalidRoleId() {
		logger.debug("Running " + getCurrentMethodName());
		try {
			model.remove(invalidId);
			fail("Invalid Role ID should have caused a ModelException!");
		} catch (RepositoryException me) {
			// All good
		} catch (Exception e) {
			printException(e);
			fail("Invalid Role ID should have caused a ModelException!");
		}
	}
	
}
