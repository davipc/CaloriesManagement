package com.toptal.calories.resources.repository;

import javax.persistence.EntityManagerFactory;

import com.toptal.calories.resources.BaseEntity;
import com.toptal.calories.resources.BaseRepository;

public abstract class BaseCustomRepository<E extends BaseEntity> extends BaseRepository<E> {

	protected static String PERSISTENCE_UNIT_NAME = "Calories-JPA";

	protected static EntityManagerFactory emFactory;
	
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

	protected EntityManagerFactory getEntityManagerFactory() {
		return emFactory;
	}
	
}
