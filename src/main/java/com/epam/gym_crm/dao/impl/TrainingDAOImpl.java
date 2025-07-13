package com.epam.gym_crm.dao.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.epam.gym_crm.dao.ITrainingDAO;
import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.model.TrainingType;
import com.epam.gym_crm.storage.Storage;
import com.epam.gym_crm.utils.EntityType;

@Repository
public class TrainingDAOImpl implements ITrainingDAO {

	private static final Logger logger = LoggerFactory.getLogger(TrainingDAOImpl.class);
	
	private Storage storage;
	
	@Autowired
	public void setStorage(Storage storage) {
		this.storage=storage;
		logger.info("TrainingDAOImpl initialized.");
	}
	
	private Map<Long, Training> getTrainingMap(){
		return storage.getStorageMap(EntityType.TRAINING);
	}
	

	@Override
	public Optional<Training> findById(Long id) {
		return Optional.ofNullable(getTrainingMap().get(id));
	}

	@Override
	public List<Training> findAll() {
		return new ArrayList<>(getTrainingMap().values());
	}
	
	@Override
	public Training create(Training training) {
		getTrainingMap().put(training.getId(), training);
        return training;
	}

	@Override
	public Training update(Training training) {
		getTrainingMap().put(training.getId(), training);
        return training;
	}

	@Override
	public boolean delete(Long id) {
		return getTrainingMap().remove(id) != null;
	}

	@Override
	public List<Training> findByTrainingName(String trainingName) {
		return getTrainingMap()
				.values()
				.stream()
				.filter(t ->trainingName !=null &&trainingName.equalsIgnoreCase(t.getTrainingName()))
				.collect(Collectors.toList());
	}

	@Override
	public List<Training> findByTrainingType(TrainingType trainingType) {
		return getTrainingMap()
				.values()
				.stream()
				.filter(t -> trainingType!=null && trainingType.equals(t.getTrainingType()))
				.collect(Collectors.toList());
	}

	@Override
	public List<Training> findByTrainingDate(LocalDate trainingDate) {
		return getTrainingMap().values()
				.stream()
				.filter(t ->trainingDate !=null && trainingDate.equals(t.getTrainingDate()))
				.collect(Collectors.toList());
	}

	@Override
	public List<Training> findByTraineeId(Long traineeId) {
		return getTrainingMap().values().stream()
				.filter(t-> traineeId !=null && traineeId.equals(t.getTrainee().getId()))
				.collect(Collectors.toList());
	}

	@Override
	public List<Training> findByTrainerId(Long trainerId) {
		return getTrainingMap().values()
				.stream()
				.filter(t -> trainerId !=null && trainerId.equals(t.getTrainer().getId()))
				.collect(Collectors.toList());
	}

}
