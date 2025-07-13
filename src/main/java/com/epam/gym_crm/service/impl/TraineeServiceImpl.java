package com.epam.gym_crm.service.impl;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.gym_crm.dao.ITraineeDAO;
import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.service.ITraineeService;
import com.epam.gym_crm.service.init.IdGenerator;
import com.epam.gym_crm.utils.EntityType;

@Service
public class TraineeServiceImpl implements ITraineeService {

	private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

	private static final int PASSWORD_LENGTH = 10;
	private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private final Random random = new SecureRandom();

	@Autowired
	private ITraineeDAO traineeDAO;

	private IdGenerator idGenerator;

	@Autowired
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		logger.info("IdGenerator, TraineeServiceImpl'e setter enjeksiyonu ile dahil edildi.");
	}

	@Override
	public Trainee findTraineeById(Long id) {

		if (id == null || id <= 0) {
			logger.error("Trainee ID for lookup cannot be null or non-positive: {}", id);
			// throw new BaseException("Trainee ID for lookup must be a positive value.");
		}

		Optional<Trainee> optTrainee = traineeDAO.findById(id);
		if (optTrainee.isEmpty()) {
			logger.warn("Trainee not found with ID={}", id);
			// throw new BaseException("Trainee not found with ID: " + id);
		}
		logger.info("Finding Trainee by ID={} -> Found: {}", id, optTrainee.isPresent());
		return optTrainee.get();
	}

	@Override
	public Trainee findTraineeByUsername(String username) {
		if (username == null || username.isBlank()) {
			logger.error("Trainee username for lookup cannot be null or empty.");
			throw new IllegalArgumentException("Trainee username for lookup must not be null or empty.");
		}

		Optional<Trainee> optTrainee = traineeDAO.findByUsername(username);
		if (optTrainee.isEmpty()) {
			logger.warn("Trainee not found with username:{}", username);
			// throw new BaseException("Trainee not found with username: " + username);
		}
		logger.info("Finding Trainee by username='{}' -> Found: {}", username, optTrainee.isPresent());
		return optTrainee.get();
	}

	@Override
	public List<Trainee> getAllTrainees() {
		List<Trainee> trainees = traineeDAO.findAll();
		logger.info("Retrieving all trainees -> Count: {}", trainees.size());
		return trainees;
	}

	@Override
	public Trainee create(Trainee trainee) {

		Long traineeId = idGenerator.getNextId(EntityType.TRAINEE);
		trainee.setId(traineeId);

		String baseUsername = trainee.getFirstName() + "." + trainee.getLastName();
		String finalUsername = generateUniqueUsername(baseUsername);
		trainee.setUsername(finalUsername);

		String password = generateRandomPassword();
		trainee.setPassword(password);
		trainee.setActive(true);

		Trainee createdTrainee = traineeDAO.create(trainee);
		if (createdTrainee == null) {
			logger.error("Trainee creation failed at DAO layer for trainee with username: {}", finalUsername);
			// throw new BaseException("Failed to create trainee)
			;
		}
		logger.info("Trainee created with ID={}, username={}", createdTrainee.getId(), createdTrainee.getUsername());
		return createdTrainee;
	}

	@Override
	public Trainee updateTrainee(Trainee trainee) {

		if (trainee.getId() == null || trainee.getId() <= 0) {
			logger.error("Trainee object or ID for update cannot be null or non-positive.");
			// throw new BaseException("Trainee object and a valid ID must be provided for
			// update.");
		}

		Optional<Trainee> existingTraineeOpt = traineeDAO.findById(trainee.getId());
		if (existingTraineeOpt.isEmpty()) {
			logger.warn("Trainee with ID {} not found for update.", trainee.getId());
			// throw new BaseException("Trainee with ID " + trainee.getId() + " not found
			// for update.");
		}

		Trainee existingTrainee = existingTraineeOpt.get();

		if (trainee.getFirstName() != null) {
			existingTrainee.setFirstName(trainee.getFirstName());
		}
		if (trainee.getLastName() != null) {
			existingTrainee.setLastName(trainee.getLastName());
		}
		existingTrainee.setActive(trainee.isActive());

		if (trainee.getDateOfBirth() != null) {
			existingTrainee.setDateOfBirth(trainee.getDateOfBirth());
		}
		if (trainee.getAddress() != null) {
			existingTrainee.setAddress(trainee.getAddress());
		}

		Trainee updated = traineeDAO.update(existingTrainee);
		if (updated == null) {
			logger.error("Trainee update failed at DAO layer for trainee ID: {}", trainee.getId());
			// throw new BaseException("Failed to update trainee due to a DAO layer
			// issue.");
		}
		logger.info("Trainee updated: ID={}, username={}", updated.getId(), updated.getUsername());
		return updated;
	}

	@Override
	public boolean deleteTrainee(Long id) {
		if (id == null || id <= 0) {
			logger.error("Trainee ID for deletion cannot be null or non-positive: {}", id);
			// throw new BaseException("Trainee ID for deletion must be a positive value.");
		}

		boolean deleted = traineeDAO.delete(id);
		if (deleted) {
			logger.info("Trainee deleted: ID={}", id);
		} else {
			logger.warn("No trainee found to delete with ID={}", id);
			// throw new BaseException("No trainee found to delete with ID: " + id);
		}
		return deleted;
	}

	private String generateUniqueUsername(String baseUsername) {
		String username = baseUsername;
		int counter = 1;
		while (traineeDAO.findByUsername(username).isPresent()) {
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