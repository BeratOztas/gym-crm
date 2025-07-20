package com.epam.gym_crm.facade;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.service.ITraineeService;
import com.epam.gym_crm.service.ITrainerService;
import com.epam.gym_crm.service.ITrainingService;

import jakarta.validation.Valid;

@Component
@Validated
public class GymCRMFacade {
	private static final Logger logger = LoggerFactory.getLogger(GymCRMFacade.class);

	private final ITrainerService trainerService;
	private final ITraineeService traineeService;
	private final ITrainingService trainingService;

	public GymCRMFacade(ITrainerService trainerService, ITraineeService traineeService,ITrainingService trainingService) {
		this.trainerService = trainerService;
		this.traineeService = traineeService;
		this.trainingService=trainingService;
		logger.info("GymCRMFacade initialized.");
	}

	// ================= TRAINER ===================

	public Trainer createTrainer(@Valid Trainer trainer) {
		logger.info("Facade: Creating trainer...");
		return trainerService.create(trainer);
	}

	public Trainer getTrainerById(Long id) {
		logger.info("Facade: Fetching trainer by ID={}", id);
		return trainerService.findTrainerById(id);
	}

	public Trainer getTrainerByUsername(String username) {
		logger.info("Facade: Fetching trainer by Username={}", username);
		return trainerService.findTrainerByUsername(username);
	}

	public Trainer updateTrainer(@Valid Trainer trainer) {
		logger.info("Facade: Updating trainer...");
		return trainerService.updateTrainer(trainer);
	}

	public List<Trainer> getAllTrainers() {
		logger.info("Facade: Fetching all trainers");
		return trainerService.getAllTrainers();
	}

	public boolean deleteTrainer(Long id) {
		logger.info("Facade: Deleting trainer with ID={}", id);
		return trainerService.deleteTrainer(id);
	}

	// ============= TRAINEE ==================

	public Trainee createTrainee(@Valid Trainee trainee) {
		logger.info("Facade: Creating trainee...");
		return traineeService.create(trainee);
	}

	public Trainee getTraineeById(Long id) {
		logger.info("Facade: Fetching trainee by ID={}", id);
		return traineeService.findTraineeById(id);
	}

	public Trainee getTraineeByUsername(String username) {
		logger.info("Facade: Fetching trainee by Username={}", username);
		return traineeService.findTraineeByUsername(username);
	}

	public Trainee updateTrainee(@Valid Trainee trainee) {
		logger.info("Facade: Updating trainee...");
		return traineeService.updateTrainee(trainee);
	}

	public List<Trainee> getAllTrainees() {
		logger.info("Facade: Fetching all trainees");
		return traineeService.getAllTrainees();
	}

	public boolean deleteTrainee(Long id) {
		logger.info("Facade: Deleting trainee with ID={}", id);
		return traineeService.deleteTrainee(id);
	}
	// ============= TRAINING ==================
	
	public Training createTraining(@Valid Training training) {
		logger.info("Facade: Creating training...");
		return trainingService.create(training);
	}

	public Training getTrainingById(Long id) {
		logger.info("Facade: Fetching training by ID={}", id);
		return trainingService.findById(id);
	}

	public Training updateTraining(@Valid Training training) {
		logger.info("Facade: Updating training...");
		return trainingService.update(training);
	}

	public List<Training> getAllTrainings() {
		logger.info("Facade: Fetching all trainings");
		return trainingService.getAllTrainings();
	}

	public boolean deleteTraining(Long id) {
		logger.info("Facade: Deleting training with ID={}", id);
		return trainingService.delete(id);
	}

	public List<Training> getTrainingsByTrainingName(String trainingName) {
		logger.info("Facade: Fetching trainings by name={}", trainingName);
		return trainingService.findByTrainingName(trainingName);
	}

//	public List<Training> getTrainingsByTrainingType(TrainingType trainingType) {
//		logger.info("Facade: Fetching trainings by type={}", trainingType);
//		return trainingService.findByTrainingType(trainingType);
//	}

	public List<Training> getTrainingsByTrainingDate(LocalDate trainingDate) {
		logger.info("Facade: Fetching trainings by date={}", trainingDate);
		return trainingService.findByTrainingDate(trainingDate);
	}

	public List<Training> getTrainingsByTraineeId(Long traineeId) {
		logger.info("Facade: Fetching trainings by Trainee ID={}", traineeId);
		return trainingService.findByTraineeId(traineeId);
	}

	public List<Training> getTrainingsByTrainerId(Long trainerId) {
		logger.info("Facade: Fetching trainings by Trainer ID={}", trainerId);
		return trainingService.findByTrainerId(trainerId);
	}
}
