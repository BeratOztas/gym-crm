package com.epam.gym_crm.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.gym_crm.dao.ITraineeDAO;
import com.epam.gym_crm.dao.ITrainerDAO;
import com.epam.gym_crm.dao.ITrainingDAO;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.model.TrainingType;
import com.epam.gym_crm.model.User;
import com.epam.gym_crm.service.ITrainingService;
import com.epam.gym_crm.service.init.IdGenerator;
import com.epam.gym_crm.utils.EntityType;

@Service
public class TrainingServiceImpl implements ITrainingService {

	private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

	@Autowired
	private ITrainingDAO trainingDAO;
	@Autowired
	private ITraineeDAO traineeDAO;
	@Autowired
	private ITrainerDAO trainerDAO;

	private IdGenerator idGenerator;

	@Autowired
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		logger.info("IdGenerator successfully injected into TrainingServiceImpl via setter injection.");
	}

	@Override
	public Training findById(Long id) {
		if (id == null || id <= 0) {
			logger.error("Training ID for lookup cannot be null or non-positive: {}", id);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Training ID must be a positive value. Provided: " + id));
		}
		Optional<Training> optTraining = trainingDAO.findById(id);
		if (optTraining.isEmpty()) {
			logger.warn("Training not found with ID={}", id);
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Training not found with ID: " + id));
		}
		logger.info("Training found by ID: {}", id);
		return optTraining.get();
	}

	@Override
	public List<Training> getAllTrainings() {
		List<Training> trainings = trainingDAO.findAll();
		logger.info("Retrieving all trainings -> Count: {}", trainings.size());
		return trainings;
	}

	@Override
	public Training create(Training training) {
		if (training == null) {
			logger.error("Training object for creation cannot be null.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Training object cannot be null."));
		}
		User traineeUser =training.getTrainee().getUser();
		User trainerUser =training.getTrainer().getUser();
		if (training.getTrainee() == null || traineeUser.getId() == null) {
			logger.error("Trainee object with ID must be provided for Training creation.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Trainee with a valid ID must be provided for Training creation."));
		}
		if (training.getTrainer() == null || trainerUser.getId() == null) {
			logger.error("Trainer object with ID must be provided for Training creation.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Trainer with a valid ID must be provided for Training creation."));
		}
		Optional<Trainee> existingTrainee = traineeDAO.findById(traineeUser.getId());
		if (existingTrainee.isEmpty()) {
			logger.error("Trainee with ID {} not found for training creation.", traineeUser.getId());
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainee with ID " + traineeUser.getId() + " not found."));
		}
		Optional<Trainer> existingTrainer = trainerDAO.findById(trainerUser.getId());
		if (existingTrainer.isEmpty()) {
			logger.error("Trainer with ID {} not found for training creation.", trainerUser.getId());
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainer with ID " + trainerUser.getId() + " not found."));
		}
		training.setTrainee(existingTrainee.get());
		training.setTrainer(existingTrainer.get());
		training.setId(idGenerator.getNextId(EntityType.TRAINING));
		Training createdTraining = trainingDAO.create(training);
		if (createdTraining == null) {
			logger.error("Training creation failed at DAO layer for training ID: {}", training.getId());
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Failed to create training due to DAO error."));
		}
		logger.info("Training created with ID={}, name='{}' for Trainee ID {} and Trainer ID {}",
				createdTraining.getId(), createdTraining.getTrainingName(), createdTraining.getTrainee().getUser().getId(), createdTraining.getTrainer().getUser().getId());
		return createdTraining;
	}

	@Override
	public Training update(Training training) {
		User traineeUser =training.getTrainee().getUser();
		User trainerUser =training.getTrainer().getUser();
		if (training == null || training.getId() == null || training.getId() <= 0) {
			logger.error("Training object or ID for update cannot be null or non-positive.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Training object and a valid ID must be provided for update."));
		}
		Optional<Training> existingTrainingOpt = trainingDAO.findById(training.getId());
		if (existingTrainingOpt.isEmpty()) {
			logger.warn("Training with ID {} not found for update.", training.getId());
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Training with ID " + training.getId() + " not found for update."));
		}
		Training existingTraining = existingTrainingOpt.get();
		if (training.getTrainingName() != null && !training.getTrainingName().isBlank()) {
			existingTraining.setTrainingName(training.getTrainingName());
		}
		if (training.getTrainingType() != null) {
			existingTraining.setTrainingType(training.getTrainingType());
		}
		if (training.getTrainingDate() != null) {
			existingTraining.setTrainingDate(training.getTrainingDate());
		}
		if (training.getTrainingDuration() > 0) {
			existingTraining.setTrainingDuration(training.getTrainingDuration());
		}
		if (training.getTrainee() != null && traineeUser.getId() != null) {
			Optional<Trainee> newTrainee = traineeDAO.findById(traineeUser.getId());
			if (newTrainee.isEmpty()) {
				logger.error("New Trainee with ID {} not found for training update.", traineeUser.getId());
				throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "New Trainee with ID " + traineeUser.getId() + " not found for update."));
			}
			existingTraining.setTrainee(newTrainee.get());
		}
		if (training.getTrainer() != null && trainerUser.getId() != null) {
			Optional<Trainer> newTrainer = trainerDAO.findById(trainerUser.getId());
			if (newTrainer.isEmpty()) {
				logger.error("New Trainer with ID {} not found for training update.", trainerUser.getId());
				throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "New Trainer with ID " + trainerUser.getId() + " not found for update."));
			}
			existingTraining.setTrainer(newTrainer.get());
		}
		Training updatedTraining = trainingDAO.update(existingTraining);
		if (updatedTraining == null) {
			logger.error("Training update failed at DAO layer for training ID: {}", training.getId());
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Failed to update training due to DAO error."));
		}
		logger.info("Training updated: ID={}, name='{}'", updatedTraining.getId(), updatedTraining.getTrainingName());
		return updatedTraining;
	}

	@Override
	public boolean delete(Long id) {
		if (id == null || id <= 0) {
			logger.error("Training ID for deletion cannot be null or non-positive: {}", id);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Training ID for deletion must be a positive value. Provided: " + id));
		}
		boolean deleted = trainingDAO.delete(id);
		if (!deleted) {
			logger.warn("No training found to delete with ID={}", id);
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "No training found to delete with ID: " + id));
		}
		logger.info("Training deleted: ID={}", id);
		return true;
	}

	@Override
	public List<Training> findByTrainingName(String trainingName) {
		if (trainingName == null || trainingName.isBlank()) {
			logger.error("Training name for lookup cannot be null or empty.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Training name for lookup must not be null or empty."));
		}
		List<Training> trainings = trainingDAO.findByTrainingName(trainingName);
		logger.info("Found {} trainings with name '{}'.", trainings.size(), trainingName);
		return trainings;
	}

	@Override
	public List<Training> findByTrainingType(TrainingType trainingType) {
		if (trainingType == null) {
			logger.error("Training type for lookup cannot be null.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Training type must not be null."));
		}
		List<Training> trainings = trainingDAO.findByTrainingType(trainingType);
		logger.info("Found {} trainings with type '{}'.", trainings.size(), trainingType);
		return trainings;
	}

	@Override
	public List<Training> findByTrainingDate(LocalDate trainingDate) {
		if (trainingDate == null) {
			logger.error("Training date for lookup cannot be null.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Training date must not be null."));
		}
		List<Training> trainings = trainingDAO.findByTrainingDate(trainingDate);
		logger.info("Found {} trainings on date '{}'.", trainings.size(), trainingDate);
		return trainings;
	}

	@Override
	public List<Training> findByTraineeId(Long traineeId) {
		if (traineeId == null || traineeId <= 0) {
			logger.error("Trainee ID for training lookup cannot be null or non-positive: {}", traineeId);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Trainee ID must be a positive value. Provided: " + traineeId));
		}
		List<Training> trainings = trainingDAO.findByTraineeId(traineeId);
		logger.info("Found {} trainings for Trainee ID {}.", trainings.size(), traineeId);
		return trainings;
	}

	@Override
	public List<Training> findByTrainerId(Long trainerId) {
		if (trainerId == null || trainerId <= 0) {
			logger.error("Trainer ID for training lookup cannot be null or non-positive: {}", trainerId);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Trainer ID must be a positive value. Provided: " + trainerId));
		}
		List<Training> trainings = trainingDAO.findByTrainerId(trainerId);
		logger.info("Found {} trainings for Trainer ID {}.", trainings.size(), trainerId);
		return trainings;
	}
} 
