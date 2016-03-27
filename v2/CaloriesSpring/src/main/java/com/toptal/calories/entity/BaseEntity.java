package com.toptal.calories.entity;

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
	

	/**
	 * Validates the input String, returning the errors in case it is not valid
	 * @param attr The attribute to validate
	 * @param required True if the attribute is required (not null)
	 * @param maxSize The string max size
	 * @param attrName The attribute name (for reporting purposes)
	 * @return An empty String if there are no errors, or the errors if there are any. 
	 */
	public String validateString(String attr, boolean required, int maxSize, String attrName) {
		StringBuilder sb = new StringBuilder();
		
		if (attr == null) {
			if (required) { 
				sb.append(attrName + " is null; ");
			}
		} else if (attr.length() > maxSize) {
			sb.append(attrName + " size is bigger than " + maxSize + "; ");
		}
		
		return sb.toString();
	}
	

	/**
	 * Validates the input Object, returning the errors in case it is not valid
	 * @param attr The attribute to validate
	 * @param required True if the attribute is required (not null)
	 * @param attrName The attribute name (for reporting purposes)
	 * @return An empty String if there are no errors, or the errors if there are any. 
	 */
	public String validateForNull(Object attr, boolean required, String attrName) {
		StringBuilder sb = new StringBuilder();
		
		if (attr == null && required) { 
			sb.append(attrName + " is null; ");
		}
		
		return sb.toString();
	}
}
