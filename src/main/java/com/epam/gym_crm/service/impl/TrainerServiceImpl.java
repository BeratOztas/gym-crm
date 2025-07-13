package com.epam.gym_crm.service.impl;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

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
		this.idGenerator=idGenerator;
	}

	@Override
	public Trainer findTrainerById(Long id) {
		Optional<Trainer> optTrainer = trainerDAO.findById(id);
		if (optTrainer.isEmpty()) {
			logger.error("Trainer not found with ID={}",id);
			// throw new BaseException()new IllegalArgumentException("Trainer not found with
			// ID: " + id))
		}
		logger.info("Finding Trainer by ID={} -> Found: {}", id, optTrainer.isPresent());
		return optTrainer.get();
	}

	@Override
	public Trainer findTrainerByUsername(String username) {
		Optional<Trainer> optTrainer = trainerDAO.findByUsername(username);
		if (optTrainer.isEmpty()) {
			logger.error("Trainer not found with username:{}",username);
			// throw new BaseException()"Trainer not found with username: " + username)
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
		if (trainer == null || trainer.getFirstName() == null || trainer.getLastName() == null) {
			logger.error("Trainer creation failed: first name or last name is null");
			// throw new BaseException() first name or last name must not be null"
		}
		if (trainer.getSpecialization() == null) {
			logger.error("Trainer creation failed:trainer specialization can not be null");
			// throw new BaseException trainer specialization can not be null
		}
		
		Long trainerId =idGenerator.getNextId(EntityType.TRAINER);
		trainer.setId(trainerId);
		
		String baseUsername = trainer.getFirstName() + "." + trainer.getLastName();
        String finalUsername = generateUniqueUsername(baseUsername);
        trainer.setUsername(finalUsername);

        String password = generateRandomPassword();
        trainer.setPassword(password);
        trainer.setActive(true);
        
        Trainer createdTrainer = trainerDAO.create(trainer);
        logger.info("Trainer created with ID={}, username={}", createdTrainer.getId(), createdTrainer.getUsername());
        return createdTrainer;

	}

	@Override
	public Trainer updateTrainer(Trainer trainer) {
		if (trainer == null || trainer.getId() == null) {
			logger.error("Trainer update failed: trainer or ID is null");
			// throw new BaseException ("Trainer and ID must not be null");
		}
		Trainer updated = trainerDAO.update(trainer);
        logger.info("Trainer updated: ID={}, username={}", updated.getId(), updated.getUsername());
        return updated;
	}

	@Override
	public boolean deleteTrainer(Long id) {
		if (id == null) {
			logger.error("Delete failed: ID is null");
			// throw new BaseException ("ID must not be null");
		}

		boolean deleted = trainerDAO.delete(id);
		if (deleted) {
			logger.info("Trainer deleted: ID={}", id);
		} else {
			logger.warn("No trainer found to delete with ID={}", id);
			// throw new BaseException no trainer found to delete id
		}
		return deleted;
	}

	private String generateUniqueUsername(String baseUsername) {
		List<String> existingUsernames = trainerDAO.findAll().stream().map(Trainer::getUsername)
				.collect(Collectors.toList());

		String username = baseUsername;
		int counter = 1;
		while (existingUsernames.contains(username)) {
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
