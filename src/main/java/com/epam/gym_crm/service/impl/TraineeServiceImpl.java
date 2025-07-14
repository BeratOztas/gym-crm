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
// Exception imports
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.User;
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
		
		logger.info("IdGenerator successfully injected into TraineeServiceImpl via setter injection.");
	}

	@Override
	public Trainee findTraineeById(Long id) {
		if (id == null || id <= 0) {
			logger.error("Trainee ID for lookup cannot be null or non-positive: {}", id);
			// Throw BaseException for invalid argument
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, 
                                     "Trainee ID for lookup must be a positive value. Provided ID: " + id));
		}

		Optional<Trainee> optTrainee = traineeDAO.findById(id);
		
		Trainee foundTrainee = optTrainee.orElseThrow(() -> {
			logger.warn("Trainee not found with ID={}", id);
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, 
                                      "Trainee not found with ID: " + id));
		});
		logger.info("Finding Trainee by ID={} -> Found: {}", id, true); // Always true if not thrown
		return foundTrainee;
	}

	@Override
	public Trainee findTraineeByUsername(String username) {
		if (username == null || username.isBlank()) {
			logger.error("Trainee username for lookup cannot be null or empty.");
			// Throw BaseException for invalid argument
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, 
                                     "Trainee username for lookup must not be null or empty."));
		}

		Optional<Trainee> optTrainee = traineeDAO.findByUsername(username);
		
		Trainee foundTrainee = optTrainee.orElseThrow(() -> {
			logger.warn("Trainee not found with username:{}", username);
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, 
                                      "Trainee not found with username: " + username));
		});
		logger.info("Finding Trainee by username='{}' -> Found: {}", username, true); // Always true if not thrown
		return foundTrainee;
	}

	@Override
	public List<Trainee> getAllTrainees() {
		List<Trainee> trainees = traineeDAO.findAll();
		logger.info("Retrieving all trainees -> Count: {}", trainees.size());
		return trainees;
	}

	@Override
	public Trainee create(Trainee trainee) {
		User userProfile =trainee.getUser();
		
		Long traineeId = idGenerator.getNextId(EntityType.TRAINEE);
		userProfile.setId(traineeId);

		String baseUsername = userProfile.getFirstName() + "." + userProfile.getLastName();
		String finalUsername = generateUniqueUsername(baseUsername); // This method already checks for duplicates
		userProfile.setUsername(finalUsername);

		String password = generateRandomPassword();
		userProfile.setPassword(password);
		userProfile.setActive(true);

		Trainee createdTrainee = traineeDAO.create(trainee);
		// If DAO returns null, it indicates a failure at the DAO layer
		if (createdTrainee == null) {
			logger.error("Trainee creation failed at DAO layer for trainee with username: {}", finalUsername);
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, 
                                     "Failed to create trainee with username: " + finalUsername + " at DAO layer."));
		}
		User createdTraineeUser =createdTrainee.getUser();
		logger.info("Trainee created with ID={}, username={}", createdTraineeUser.getId(), createdTraineeUser.getUsername());
		return createdTrainee;
	}

	@Override
	public Trainee updateTrainee(Trainee trainee) {
		User userProfile =trainee.getUser();
		if (userProfile.getId() == null || userProfile.getId() <= 0) {
			logger.error("Trainee object or ID for update cannot be null or non-positive.");
			// Throw BaseException for invalid argument
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, 
                                     "Trainee object and a valid ID must be provided for update. Provided ID: " + userProfile.getId()));
		}

		Optional<Trainee> existingTraineeOpt = traineeDAO.findById(userProfile.getId());
		
		Trainee existingTrainee = existingTraineeOpt.orElseThrow(() -> {
			logger.warn("Trainee with ID {} not found for update.", userProfile.getId());
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, 
                                      "Trainee with ID " + userProfile.getId() + " not found for update."));
		});

		User existingTraineeUser =existingTrainee.getUser();
		
		if (userProfile.getFirstName() != null) {
			existingTraineeUser.setFirstName(userProfile.getFirstName());
		}
		if (userProfile.getLastName() != null) {
			existingTraineeUser.setLastName(userProfile.getLastName());
		}
		// Assuming isActive can be updated to false, so no null check
		existingTraineeUser.setActive(userProfile.isActive()); 

		if (trainee.getDateOfBirth() != null) {
			existingTrainee.setDateOfBirth(trainee.getDateOfBirth());
		}
		if (trainee.getAddress() != null) {
			existingTrainee.setAddress(trainee.getAddress());
		}

		Trainee updated = traineeDAO.update(existingTrainee);
		if (updated == null) {
			logger.error("Trainee update failed at DAO layer for trainee ID: {}", userProfile.getId());
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, 
					"Failed to update trainee with ID: " + userProfile.getId() + " at DAO layer."));
		}
		User updatedUser =updated.getUser();
		// If DAO returns null, it indicates a failure at the DAO layer
		logger.info("Trainee updated: ID={}, username={}", updatedUser.getId(), updatedUser.getUsername());
		return updated;
	}

	@Override
	public boolean deleteTrainee(Long id) {
		if (id == null || id <= 0) {
			logger.error("Trainee ID for deletion cannot be null or non-positive: {}", id);
			// Throw BaseException for invalid argument
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, 
                                     "Trainee ID for deletion must be a positive value. Provided ID: " + id));
		}

		boolean deleted = traineeDAO.delete(id);
		if (deleted) {
			logger.info("Trainee deleted: ID={}", id);
		} else {
			logger.warn("No trainee found to delete with ID={}", id);
			// Throw BaseException if trainee not found for deletion
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, 
                                     "No trainee found to delete with ID: " + id));
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