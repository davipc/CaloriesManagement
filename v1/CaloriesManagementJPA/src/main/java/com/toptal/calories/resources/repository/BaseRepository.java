package com.toptal.calories.resources.repository;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.entity.BaseEntity;
import com.toptal.calories.resources.util.LogUtil;


public abstract class BaseRepository<E extends BaseEntity> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	protected Class<E> associatedEntityType;
	protected EntityManager em;
	
	public BaseRepository() {
		associatedEntityType = getGenericClassTypeValue();
	}

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}
	
	/**
	 * Finds the actual type of E for any subclasses defining it. 
	 * @return The actual type of E (see this class declaration).
	 */
	@SuppressWarnings("unchecked")
	public Class<E> getGenericClassTypeValue() {
		ParameterizedType parameterizedType =
			(ParameterizedType) getClass().getGenericSuperclass();
		return (Class<E>) parameterizedType.getActualTypeArguments()[0];
    }	
	
	/**
	 * Returns a consistent "time-elapsed" message suffix. 
	 * NOT A STATIC METHOD, TO AVOID SYNCHRONIZATION ISSUES AND MAINTAIN PERFORMANCE.
	 * 
	 * @param startTime
	 * @return
	 */
	protected String getWrapupMsg(long startTime) {
		return " after " + (System.currentTimeMillis() - startTime) + " ms";
	}

	/**
	 * Creates and stores an entity into the database.
	 * @param entity The entity to store. 
	 */
	public E createOrUpdate(E entity) throws RepositoryException {
		logger.info("Creating or updating " + entity);
		long startTime = System.currentTimeMillis();
		E createdOrUpdated = null;
		try {
			if (entity == null) {
				throw new RepositoryException("Null object received for create or update!!");
			} else {
				try {
					em.getTransaction().begin();
					createdOrUpdated = em.merge(entity);
					em.getTransaction().commit();
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error in create or update of " + entity, pe);
				} finally {
					if (em != null && em.isOpen()) {
						if (em.getTransaction().isActive()) {
							em.getTransaction().rollback();
						}
					}
				}
				LogUtil.logEnd(logger, "creating " + entity, startTime);
				return createdOrUpdated;				
			}
		} catch (RepositoryException e) {
			// even though this is known as an anti-pattern, we are willing to have 
			// all JPA exceptions logged on the same file (severe.log file) 
			// replicating try-catch-log-rethrow logic on every method for improved readability 
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
	}

	/**
	 * Creates and stores an entity into the database.
	 * @param entities The collection of entities to store.
	 * @param commitFrequency After how many inserts a commit will be made. 
	 */
	public Collection<E> batchCreateOrUpdate(Collection<E> entities, int commitFrequency) throws RepositoryException {
		logger.info("Creating or updating batch with " + (entities == null || entities.size() == 0 ? "0" : entities.size()) + " entities");
		long startTime = System.currentTimeMillis();
		List<E> createdOrUpdatedList = null;
		try {
			if (entities == null || entities.size() == 0) {
				throw new RepositoryException("Null or empty collection received for create or update!!");
			} else {
				createdOrUpdatedList = new ArrayList<E>(entities.size());
				E entity = null;
				try {
					int operationsSinceLastCommit = 0;
					E createdOrUpdated = null;
					em.getTransaction().begin();
					for (Iterator<E> it = entities.iterator(); it.hasNext();) {
						entity = it.next();
						createdOrUpdated = em.merge(entity);
						createdOrUpdatedList.add(createdOrUpdated);
						operationsSinceLastCommit++;
						if (operationsSinceLastCommit >= commitFrequency) {
							logger.debug("Commiting... operations since last commit = " + operationsSinceLastCommit + " ( == threshold)");
							em.getTransaction().commit();
							operationsSinceLastCommit = 0;
							// just start a new transaction if we are adding any more entries
							if (it.hasNext()) {
								em.getTransaction().begin();
							}
						}
					}
					// if a transaction was started, commit the operations
					if (em.getTransaction().isActive()) { 
						em.getTransaction().commit();
					}
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error in create or update of " + entity, pe);
				} finally {
					if (em != null && em.isOpen()) {
						if (em.getTransaction().isActive()) {
							em.getTransaction().rollback();
						}
					}
				}
				LogUtil.logEnd(logger, " creating or updating batch containing " + (entities == null || entities.size() == 0 ? "0" : entities.size()) + " entities", startTime);
				return createdOrUpdatedList;				
			}
		} catch (RepositoryException e) {
			// even though this is known as an anti-pattern, we are willing to have 
			// all JPA exceptions logged on the same file (severe.log file) 
			// replicating try-catch-log-rethrow logic on every method for improved readability 
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
	}
	
	/**
	 * Finds and returns the entity with the informed ID.
	 * @param id The entity ID.
	 * @return The entity associated with the input ID.
	 */
	public E find(Object id) throws RepositoryException {
		long startTime = System.currentTimeMillis();
		Class<E> modelEntityType = associatedEntityType;
		String className = modelEntityType.getSimpleName();
		logger.info("Finding " + className + "  with ID \"" + id + "\"");
		try {
			E entity = null;
			if (id == null) {
				throw new RepositoryException("Null ID received for find " + className);
			} else {
				try {
					entity = find(id, modelEntityType);
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error in find for " + className +  " with ID " + id + ": " + pe.getMessage(), pe);
				} finally {
					if (em != null && em.isOpen()) {
						if (em.getTransaction().isActive()) {
							em.getTransaction().rollback();
						}
					}
				}
			}
			LogUtil.logEnd(logger, " finding " + className + " with ID \"" + id + "\"", startTime);
			return entity;
		} catch (RepositoryException e) {
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
	}
	
	/**
	 * Finds and returns the entity with the informed ID.
	 * @param id The entity ID.
	 * @param em The entity manager.
	 * @param entityType The type of the entity to return.
	 * @return The entity associated with the input ID.
	 */
	protected E find(Object id, Class<E> entityType) throws RepositoryException {
		E entity = null; 
		try{
			entity = em.find(entityType, id);
		} catch(javax.persistence.NoResultException nre) {
			// do nothing, will just return null 
		} catch (PersistenceException pe) {
			throw new RepositoryException("Error in find for " + entityType.getSimpleName() + " with ID " + id + ": " + pe.getMessage(), pe);
		}
		return entity;
	}
	
	/**
	 * Returns all the input Entity's objects. A named query in the format <EntityName>.findAll 
	 * must be defined in the entity class for this method to work. 
	 * @return All the entity objects.
	 */
	public List<E> findAll() throws RepositoryException {
		Class<E> modelEntityType = associatedEntityType;
		List<E> entities = null;
		entities = findAll(modelEntityType);
		return entities;
	}
	
	/**
	 * Returns all the input Entity's objects. A named query in the format <EntityName>.findAll 
	 * must be defined in the entity class for this method to work.
	 * ** Using a more generic type variable because we want this method to be usable for retrieving 
	 * any entities while under any subclasses (see DBReferenceModel). 
	 * @return All the entity objects.
	 */
	@SuppressWarnings("unchecked")
	public <T extends BaseEntity> List<T> findAll(Class<T> entityType) throws RepositoryException {
		long startTime = System.currentTimeMillis();
		String className = entityType.getSimpleName();
		List<T> entities = null;

		logger.info("Finding all " + className + "s");
		
		try{
			entities = em.createNamedQuery(className + ".findAll").getResultList();
		} catch(javax.persistence.NoResultException nre) {
			// do nothing, will just return null 
		} catch (PersistenceException pe) {
			throw new RepositoryException("Error in find all " + className + "s: " + pe.getMessage() + getWrapupMsg(startTime), pe);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
			}
		}
		LogUtil.logEnd(logger, " finding all " + className + "s", startTime);
		
		return entities;
	}
	
	/**
	 * Deletes all entries in the associated table.
	 * @return The number of deleted entries
	 * @throws RepositoryException If any error happens during the deletion operation
	 */
	public int deleteAll() throws RepositoryException {
		Class<E> modelEntityType = associatedEntityType;
		int deleted = deleteAll(modelEntityType);
		return deleted;
	}
	
	/**
	 * Deletes all entries in the associated table.
	 * @return The number of deleted entries
	 * @throws RepositoryException If any error happens during the deletion operation
	 */
	public <T extends BaseEntity> int deleteAll(Class<T> entityType) throws RepositoryException {
		long startTime = System.currentTimeMillis();
		
		String className = entityType.getSimpleName();

		logger.info("Deleting all " + className + "s");
		int deletedEntries = 0;
		try {
				try {
					em.getTransaction().begin();
					deletedEntries = em.createNamedQuery(className+".deleteAll")
										.executeUpdate();
					em.getTransaction().commit();
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error in delete all for "+className, pe);
				}
				LogUtil.logEnd(logger, " deleting all " + className + "s: " + deletedEntries + " entries deleted", startTime);
				return deletedEntries;
		} catch (RepositoryException e) {
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
	}
	
	
	
	/**
	 * Gives the subclass a change to do something before the entity is removed. 
	 * @param <X>
	 * @param entity
	 */
	protected void beforeDelete(E entity) {
	}
	
	/**
	 * Removes the entity with the informed ID from the database.
	 * @param id The entity's ID.
	 */
	public boolean remove(Object id) throws RepositoryException {
		long startTime = System.currentTimeMillis();

		boolean removed = false;
		
		Class<E> modelEntityType = associatedEntityType;
		String className = modelEntityType.getSimpleName();
		logger.info("Removing " + className + " with ID \"" + id + "\"");
		try {
			if (id == null) {
				throw new RepositoryException("Null " + className + " id!!");
			} else {
				try {
					E entity = find(id, modelEntityType);
					if (entity != null) {
						em.getTransaction().begin();
						
						// give subclasses a chance to do something before the entity is removed
						beforeDelete(entity);
						
						em.remove(entity);
						em.getTransaction().commit();
						
						removed = true;
					}
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error in remove of " + className + " with ID " + id + ": " + pe.getMessage(), pe);
				} finally {
					if (em != null && em.isOpen()) {
						if (em.getTransaction().isActive()) {
							em.getTransaction().rollback();
						}
					}
				}
				LogUtil.logEnd(logger, " removing " + className + " with ID \"" + id + "\"", startTime);
			}
		} catch (RepositoryException e) {
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
		
		return removed;
	}
	
}