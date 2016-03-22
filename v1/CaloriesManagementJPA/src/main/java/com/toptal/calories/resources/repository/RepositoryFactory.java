package com.toptal.calories.resources.repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toptal.calories.resources.util.LogUtil;

public class RepositoryFactory {

	private static Logger logger = LoggerFactory.getLogger(RepositoryFactory.class);
	
	private static String PERSISTENCE_UNIT_NAME = "Calories-JPA";

	private static EntityManagerFactory emFactory;
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				closeAllConnections(emFactory, PERSISTENCE_UNIT_NAME);
			}
		});
		// if the factory is not created yet, create it
		emFactory = init(emFactory, PERSISTENCE_UNIT_NAME);
	}

	private static EntityManagerFactory init(EntityManagerFactory emFactory, String persUnitName) {
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
	
	private static void closeAllConnections(EntityManagerFactory emFactory, String persUnitName) {
		logger.warn(" entity manager factory closing all connections for " + persUnitName);
		long startTime = System.currentTimeMillis();
		if ( emFactory != null && emFactory.isOpen() ) {
			emFactory.close();
		}
		logger.warn(" closed ALL connections for " + persUnitName + " in " + (System.currentTimeMillis()-startTime) + " ms");
	}
	
	/** 
	 * Returns the entity manager
	 * @return
	 */
	private EntityManager getEntityManager() {
		logger.debug("Creating entity manager ");
		long startTime = System.currentTimeMillis();
		EntityManager em = emFactory.createEntityManager();
		LogUtil.logEnd(logger, "creating entity manager", startTime);
		return em;
	}
	
	public <R extends BaseRepository<?>> R createRepository(Class<R> clazz) 
	throws IllegalArgumentException {
		R repository = null;
		try {
			repository = clazz.newInstance();
			repository.setEntityManager(getEntityManager());
		} catch (InstantiationException|IllegalAccessException e ) {
			logger.error("Error creating new repository for " + clazz.getName(), e);
			throw new IllegalArgumentException("Failed creating repository of provided type " + clazz, e);
		}
		
		return repository;
	}
}
