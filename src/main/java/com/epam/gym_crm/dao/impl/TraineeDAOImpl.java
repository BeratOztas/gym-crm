package com.epam.gym_crm.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.epam.gym_crm.dao.ITraineeDAO;
import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.storage.Storage;
import com.epam.gym_crm.utils.EntityType;

@Repository
public class TraineeDAOImpl implements ITraineeDAO {

	private static final Logger logger = LoggerFactory.getLogger(TraineeDAOImpl.class);

	private Storage storage;

	@Autowired
	public void setStorage(Storage storage) {
		this.storage = storage;
		logger.info("TraineeDAOImpl initialized.");
	}

	private Map<Long, Trainee> getTraineeMap() {
		return storage.getStorageMap(EntityType.TRAINEE);
	}

	@Override
	public Optional<Trainee> findById(Long id) {
		return Optional.ofNullable(getTraineeMap().get(id));
	}

	@Override
	public Optional<Trainee> findByUsername(String username) {
		return getTraineeMap()
				.values()
				.stream()
				.filter(trainee -> username.equals(trainee.getUser().getUsername())).findFirst();
	}

	@Override
	public List<Trainee> findAll() {
		return new ArrayList<>(getTraineeMap().values());
	}

	@Override
	public Trainee create(Trainee trainee) {
		getTraineeMap().put(trainee.getUser().getId(), trainee);
		return trainee;
	}

	@Override
	public Trainee update(Trainee trainee) {
		getTraineeMap().put(trainee.getUser().getId(), trainee);
		return trainee;
	}

	@Override
	public boolean delete(Long id) {
		return getTraineeMap().remove(id) != null;
	}

}
