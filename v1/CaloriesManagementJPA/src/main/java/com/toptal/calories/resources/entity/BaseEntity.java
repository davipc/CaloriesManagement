package com.toptal.calories.resources.entity;

public abstract class BaseEntity {
	
	/**
	 * Needed for situations where the table ID is simple and not called ID.
	 * The ID is needed in the generic implementation accessing/handling the entities on the com.convergys.pda.phase2.dao.custom.schema level.
	 * @return The value of the entity ID.   
	 */
	public abstract Object getId();

	/**
	 * Used to compare objects (currently mainly used on equals() implementations, where both objects are simple. 
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	protected boolean areEqual(Object obj1, Object obj2) {
		return obj1 == null && obj2 == null || obj1 != null && obj1.equals(obj2);	
	}
}
