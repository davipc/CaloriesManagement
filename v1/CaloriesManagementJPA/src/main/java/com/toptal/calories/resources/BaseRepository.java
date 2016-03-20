package com.toptal.calories.resources;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class BaseRepository<E extends BaseEntity> {
	private static Logger logger = LoggerFactory.getLogger(BaseRepository.class.getName());

	// Max time for DB operation to be performed. If an operation takes more than that, a WARN message will be sent to the log file.
	public static int MAX_TIME_DB_OPER_MS = 200;
	
	protected Class<E> associatedEntityType;
	

	public BaseRepository() {
		associatedEntityType = getGenericClassTypeValue(); 
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
	
	
	protected static EntityManagerFactory init(EntityManagerFactory emFactory, String persUnitName) {
		logger.info("Initializing DB com.toptal.calories.resources.entity");
		long startTime = System.currentTimeMillis();
		
		try {
			if (emFactory != null) {
				emFactory.close();
			}
			logger.debug("Trying to create a EntityManagerFactory instance for " + persUnitName);
			emFactory = Persistence.createEntityManagerFactory(persUnitName);
			if (emFactory == null) {
				logger.error("Error creating a EntityManagerFactory instance for " + persUnitName + " after " + (System.currentTimeMillis() - startTime));
			}
			logger.debug("Created a EntityManagerFactory instance for " + persUnitName);
		} catch (Exception e) {
			logger.error("Error initializing DB com.toptal.calories.resources.entity after " + (System.currentTimeMillis() - startTime) + " ms", e);
			throw new RuntimeException(e);
		}
		logger.warn("Finished initializing DB com.toptal.calories.resources.entity for " + persUnitName + " after " + (System.currentTimeMillis() - startTime) + " ms");
		return emFactory;
	}

	protected synchronized static void closeAllConnections(EntityManagerFactory emFactory, String persUnitName) {
		logger.warn(" entity manager factory closing all connections for " + persUnitName);
		long startTime = System.currentTimeMillis();
		if ( emFactory != null && emFactory.isOpen() ) {
			emFactory.close();
		}
		logger.warn(" closed ALL connections for " + persUnitName + " in " + (System.currentTimeMillis()-startTime) + " ms");
	}
	
	/**
	 * Let the schema specific subclass return the entity manager factory (it has the factory associated 
	 * with the right persistence unit)  
	 * @return The entity manager factory
	 */
	protected abstract EntityManagerFactory getEntityManagerFactory();

	/**
	 * Logs the end of method message using the appropriate level - if it took too long, as a warning, otherwise using standardLevel.
	 * @param msgTemplate The end of method message
	 * @param startTime The time the method started being executed
	 * @param standardLevel The level to use in case the method didn't take too long to execute. 
	 */
	protected void logEnd(String action, long startTime) {
		long endTime = System.currentTimeMillis();
		String msg = " finished " + action + " after " + (System.currentTimeMillis() - startTime) + " ms";
		
		if (endTime - startTime > MAX_TIME_DB_OPER_MS) {
			logger.warn(msg);
		} else {
			logger.info(msg);
		}
		
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
	 * Returns the entity manager
	 * @return
	 */
	protected EntityManager getEntityManager() {
		logger.debug(" creating entity manager ");
		long startTime = System.currentTimeMillis();
		EntityManager em = getEntityManagerFactory().createEntityManager();
		logEnd("creating entity manager", startTime);
		return em;
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
				EntityManager em = null; 
				try {
					em = getEntityManager();
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
						em.close();
					}
				}
				logEnd("creating " + entity, startTime);
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
				EntityManager em = null; 
				createdOrUpdatedList = new ArrayList<E>(entities.size());
				E entity = null;
				try {
					em = getEntityManager();
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
						em.close();
					}
				}
				logEnd(" creating or updating batch containing " + (entities == null || entities.size() == 0 ? "0" : entities.size()) + " entities", startTime);
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
				EntityManager em = null; 
				try {
					em = getEntityManager();
					entity = find(id, em, modelEntityType);
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error in find for " + className +  " with ID " + id + ": " + pe.getMessage(), pe);
				} finally {
					if (em != null && em.isOpen()) {
						if (em.getTransaction().isActive()) {
							em.getTransaction().rollback();
						}
						em.close();
					}
				}
			}
			logEnd(" finding " + className + " with ID \"" + id + "\"", startTime);
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
	protected E find(Object id, EntityManager em, Class<E> entityType) throws RepositoryException {
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
		
		EntityManager em = null;
		try{
			em = getEntityManager();
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
				em.close();
			}
		}
		logEnd(" finding all " + className + "s", startTime);
		
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
				EntityManager em = null; 
				try {
					em = getEntityManager();
					em.getTransaction().begin();
					deletedEntries = em.createNamedQuery(className+".deleteAll")
										.executeUpdate();
					em.getTransaction().commit();
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error in delete all for "+className, pe);
				} finally {
					if (em != null && em.isOpen()) {
						em.close();
					}
				}
				logEnd(" deleting all " + className + "s: " + deletedEntries + " entries deleted", startTime);
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
	public void remove(Object id) throws RepositoryException {
		long startTime = System.currentTimeMillis();

		Class<E> modelEntityType = associatedEntityType;
		String className = modelEntityType.getSimpleName();
		logger.info("Removing " + className + " with ID \"" + id + "\"");
		try {
			if (id == null) {
				throw new RepositoryException("Null " + className + " id!!");
			} else {
				EntityManager em = null;
				try {
					em = getEntityManager();
					E entity = find(id, em, modelEntityType);
					if (entity == null) {
						throw new RepositoryException("Invalid " + className + " ID: " + id);
					}
					em.getTransaction().begin();
					
					// give subclasses a chance to do something before the entity is removed
					beforeDelete(entity);
					
					em.remove(entity);
					em.getTransaction().commit();
				} catch (PersistenceException pe) {
					throw new RepositoryException("Error in remove of " + className + " with ID " + id + ": " + pe.getMessage(), pe);
				} finally {
					if (em != null && em.isOpen()) {
						if (em.getTransaction().isActive()) {
							em.getTransaction().rollback();
						}
						em.close();
					}
				}
				logEnd(" removing " + className + " with ID \"" + id + "\"", startTime);
			}
		} catch (RepositoryException e) {
			logger.error(e.getMessage() + getWrapupMsg(startTime) + "; rootCause: " + ExceptionUtils.getRootCauseMessage(e));
			throw e;
		}
	}
	
}