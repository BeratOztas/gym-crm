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
		logger.info("IdGenerator, TrainerServiceImpl'e setter enjeksiyonu ile dahil edildi.");
	}

	@Override
	public Trainer findTrainerById(Long id) {
		if (id == null || id <= 0) {
			logger.error("Trainer ID for lookup cannot be null or non-positive: {}", id);
			// throw new BaseException("Trainer ID for lookup must be a positive value.");
		}

		Optional<Trainer> optTrainer = trainerDAO.findById(id);
		if (optTrainer.isEmpty()) {
			logger.warn("Trainer not found with ID={}", id);
			// throw new BaseException("Trainer not found with ID: " + id);
		}
		logger.info("Finding Trainer by ID={} -> Found: {}", id, optTrainer.isPresent());
		return optTrainer.get();
	}

	@Override
	public Trainer findTrainerByUsername(String username) {

		if (username == null || username.isBlank()) {
			logger.error("Trainer username for lookup cannot be null or empty.");
			// throw new BaseException("Trainer username for lookup must not be null or
			// empty.");
		}

		Optional<Trainer> optTrainer = trainerDAO.findByUsername(username);
		if (optTrainer.isEmpty()) {
			logger.warn("Trainer not found with username:{}", username); 
			// throw new BaseException("Trainer not found with username: " + username);
		}
		logger.info("Finding Trainer by username='{}' -> Found: {}", username, optTrainer.isPresent());
		return optTrainer.get();
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
		trainer.setActive(true); // Default

		Trainer createdTrainer = trainerDAO.create(trainer);
		if (createdTrainer == null) {
			logger.error("Trainer creation failed at DAO layer for trainer with username: {}", finalUsername);
			//throw new BaseException("Failed to create trainer.");
		}
		logger.info("Trainer created with ID={}, username={}", createdTrainer.getId(), createdTrainer.getUsername());
		return createdTrainer;
	}

	@Override
	public Trainer updateTrainer(Trainer trainer) {

		if (trainer.getId() == null || trainer.getId() <= 0) {
			logger.error("Trainer ID for update cannot be null or non-positive.");
			// throw new BaseException("Trainer ID for update must be a positive value.");
		}

		Optional<Trainer> existingTrainerOpt = trainerDAO.findById(trainer.getId());
		if (existingTrainerOpt.isEmpty()) {
			logger.warn("Trainer with ID {} not found for update.", trainer.getId());
			// throw new BaseException("Trainer with ID " + trainer.getId() + " not found
			// for update.");
		}

		Trainer existingTrainer = existingTrainerOpt.get();

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
			//throw new BaseException("Failed to update trainer due to a DAO layer issue.");
		}
		logger.info("Trainer updated: ID={}, username={}", updated.getId(), updated.getUsername());
		return updated;
	}

	@Override
	public boolean deleteTrainer(Long id) {
		if (id == null || id <= 0) {
			logger.error("Trainer ID for deletion cannot be null or non-positive: {}", id);
			//throw new BaseException("Trainer ID for deletion must be a positive value.");
		}

		boolean deleted = trainerDAO.delete(id);
		if (deleted) {
			logger.info("Trainer deleted: ID={}", id);
		} else {
			logger.warn("No trainer found to delete with ID={}", id);
			//throw new BaseException("No trainer found to delete with ID: " + id);
		}
		return deleted;
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