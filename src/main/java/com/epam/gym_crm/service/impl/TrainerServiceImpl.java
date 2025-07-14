package com.epam.gym_crm.service.impl;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.gym_crm.dao.ITrainerDAO;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.service.ITrainerService;
import com.epam.gym_crm.service.init.IdGenerator;
import com.epam.gym_crm.utils.EntityType;

@Service
public class TrainerServiceImpl implements ITrainerService {

	private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

	private static final int PASSWORD_LENGTH = 10;
	private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private final Random random = new SecureRandom();

	@Autowired
	private ITrainerDAO trainerDAO;

	private IdGenerator idGenerator;

	@Autowired
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		logger.info("IdGenerator successfully injected into TrainerServiceImpl via setter injection.");
	}

	@Override
	public Trainer findTrainerById(Long id) {
		if (id == null || id <= 0) {
			logger.error("Trainer ID for lookup cannot be null or non-positive: {}", id);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
				"Trainer ID must be a positive value. Provided: " + id));
		}

		Optional<Trainer> optTrainer = trainerDAO.findById(id);
		Trainer found = optTrainer.orElseThrow(() -> {
			logger.warn("Trainer not found with ID={}", id);
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
				"Trainer not found with ID: " + id));
		});

		logger.info("Finding Trainer by ID={} -> Found: {}", id, true);
		return found;
	}

	@Override
	public Trainer findTrainerByUsername(String username) {
		if (username == null || username.isBlank()) {
			logger.error("Trainer username for lookup cannot be null or empty.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
				"Trainer username must not be null or empty."));
		}

		Optional<Trainer> optTrainer = trainerDAO.findByUsername(username);
		Trainer found = optTrainer.orElseThrow(() -> {
			logger.warn("Trainer not found with username:{}", username);
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
				"Trainer not found with username: " + username));
		});

		logger.info("Finding Trainer by username='{}' -> Found: {}", username, true);
		return found;
	}

	@Override
	public List<Trainer> getAllTrainers() {
		List<Trainer> trainers = trainerDAO.findAll();
		logger.info("Retrieving all trainers -> Count: {}", trainers.size());
		return trainers;
	}

	@Override
	public Trainer create(Trainer trainer) {
		Long trainerId = idGenerator.getNextId(EntityType.TRAINER);
		trainer.setId(trainerId);

		String baseUsername = trainer.getFirstName() + "." + trainer.getLastName();
		String finalUsername = generateUniqueUsername(baseUsername);
		trainer.setUsername(finalUsername);

		String password = generateRandomPassword();
		trainer.setPassword(password);
		trainer.setActive(true);

		Trainer createdTrainer = trainerDAO.create(trainer);
		if (createdTrainer == null) {
			logger.error("Trainer creation failed at DAO layer for trainer with username: {}", finalUsername);
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION,
				"Failed to create trainer with username: " + finalUsername));
		}

		logger.info("Trainer created with ID={}, username={}", createdTrainer.getId(), createdTrainer.getUsername());
		return createdTrainer;
	}

	@Override
	public Trainer updateTrainer(Trainer trainer) {
		if (trainer.getId() == null || trainer.getId() <= 0) {
			logger.error("Trainer ID for update cannot be null or non-positive.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
				"Trainer ID must be a positive value. Provided: " + trainer.getId()));
		}

		Optional<Trainer> existingTrainerOpt = trainerDAO.findById(trainer.getId());
		Trainer existingTrainer = existingTrainerOpt.orElseThrow(() -> {
			logger.warn("Trainer with ID {} not found for update.", trainer.getId());
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
				"Trainer with ID " + trainer.getId() + " not found for update."));
		});

		if (trainer.getFirstName() != null) {
			existingTrainer.setFirstName(trainer.getFirstName());
		}
		if (trainer.getLastName() != null) {
			existingTrainer.setLastName(trainer.getLastName());
		}
		if (trainer.getSpecialization() != null) {
			existingTrainer.setSpecialization(trainer.getSpecialization());
		}
		existingTrainer.setActive(trainer.isActive());

		Trainer updated = trainerDAO.update(existingTrainer);
		if (updated == null) {
			logger.error("Trainer update failed at DAO layer for trainer ID: {}", trainer.getId());
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION,
				"Failed to update trainer with ID: " + trainer.getId()));
		}

		logger.info("Trainer updated: ID={}, username={}", updated.getId(), updated.getUsername());
		return updated;
	}

	@Override
	public boolean deleteTrainer(Long id) {
		if (id == null || id <= 0) {
			logger.error("Trainer ID for deletion cannot be null or non-positive: {}", id);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
				"Trainer ID for deletion must be a positive value. Provided: " + id));
		}

		boolean deleted = trainerDAO.delete(id);
		if (!deleted) {
			logger.warn("No trainer found to delete with ID={}", id);
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
				"No trainer found to delete with ID: " + id));
		}

		logger.info("Trainer deleted: ID={}", id);
		return true;
	}

	private String generateUniqueUsername(String baseUsername) {
		String username = baseUsername;
		int counter = 1;

		while (trainerDAO.findByUsername(username).isPresent()) {
			username = baseUsername + counter;
			counter++;
		}
		logger.debug("Generated unique username: {}", username);
		return username;
	}

	private String generateRandomPassword() {
		StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
		for (int i = 0; i < PASSWORD_LENGTH; i++) {
			password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
		}
		logger.debug("Generated password of length {}", PASSWORD_LENGTH);
		return password.toString();
	}
}
