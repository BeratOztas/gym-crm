package com.epam.gym_crm.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.epam.gym_crm.dao.ITrainerDAO;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.storage.Storage;
import com.epam.gym_crm.utils.EntityType;

@Repository
public class TrainerDAOImpl implements ITrainerDAO {

	private static final Logger logger = LoggerFactory.getLogger(TrainerDAOImpl.class);

	private Storage storage;

	@Autowired
	public void setStorage(Storage storage) {
		this.storage=storage;
		logger.info("TrainerDAOImpl initialized.");
	}

	private Map<Long, Trainer> getTrainerMap() {
		return storage.getStorageMap(EntityType.TRAINER);
	}

	@Override
	public Trainer create(Trainer trainer) {
		 getTrainerMap().put(trainer.getId(), trainer);
		 return trainer;
	}

	@Override
	public Optional<Trainer> findById(Long id) {
		return Optional.ofNullable(getTrainerMap().get(id));
	}

	@Override
	public Optional<Trainer> findByUsername(String username) {
		return getTrainerMap()
				.values().stream()
				.filter(trainer -> username.equals(trainer.getUsername())).findFirst();
	}

	@Override
	public List<Trainer> findAll() {
		return new ArrayList<>(getTrainerMap().values());
	}

	@Override
	public Trainer update(Trainer trainer) {
		return getTrainerMap().put(trainer.getId(), trainer);
	}

	@Override
	public boolean delete(Long id) {
		return getTrainerMap().remove(id) != null;
	}

}
