package com.epam.gym_crm.facade;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.epam.gym_crm.dto.request.TraineeCreateRequest;
import com.epam.gym_crm.dto.request.TraineeTrainingListRequest;
import com.epam.gym_crm.dto.request.TraineeUpdateRequest;
import com.epam.gym_crm.dto.request.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.dto.request.TrainerCreateRequest;
import com.epam.gym_crm.dto.request.TrainerTrainingListRequest;
import com.epam.gym_crm.dto.request.TrainerUpdateRequest;
import com.epam.gym_crm.dto.request.TrainingCreateRequest;
import com.epam.gym_crm.dto.request.TrainingUpdateRequest;
import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.response.TraineeResponse;
import com.epam.gym_crm.dto.response.TrainerResponse;
import com.epam.gym_crm.dto.response.TrainingResponse;
import com.epam.gym_crm.service.ITraineeService;
import com.epam.gym_crm.service.ITrainerService;
import com.epam.gym_crm.service.ITrainingService;

import jakarta.validation.Valid;

@Component
@Validated
public class GymCRMFacade {

    private static final Logger logger = LoggerFactory.getLogger(GymCRMFacade.class);

    private final ITraineeService traineeService;
    private final ITrainerService trainerService;
    private final ITrainingService trainingService;

    public GymCRMFacade(ITraineeService traineeService, ITrainerService trainerService, ITrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        logger.info("GymCRMFacade initialized.");
    }

    // ------ Trainee Facade Methods ------

    public TraineeResponse createTrainee(@Valid TraineeCreateRequest request) {
        logger.info("Facade: Creating new trainee profile for first name: {}, last name: {}", request.getFirstName(), request.getLastName());
        return traineeService.createTrainee(request);
    }

    public TraineeResponse findTraineeById(Long id) {
        logger.info("Facade: Finding trainee by ID: {}", id);
        return traineeService.findTraineeById(id);
    }

    public TraineeResponse findTraineeByUsername(String username) {
        logger.info("Facade: Finding trainee by username: {}", username);
        return traineeService.findTraineeByUsername(username);
    }

    public List<TraineeResponse> getAllTrainees() {
        logger.info("Facade: Getting all trainees.");
        return traineeService.getAllTrainees();
    }

    public TraineeResponse updateTrainee(@Valid TraineeUpdateRequest request) {
        logger.info("Facade: Updating trainee profile for username: {}", request.getUsername());
        return traineeService.updateTrainee(request);
    }

    public TraineeResponse updateTraineeTrainers(@Valid TraineeUpdateTrainersRequest request) {
        logger.info("Facade: Updating trainers list for trainee: {}", request.getTraineeUsername());
        return traineeService.updateTraineeTrainersList(request);
    }

    public void activateDeactivateTrainee(@Valid UserActivationRequest request) {
        logger.info("Facade: {} trainee '{}'.", request.isActive() ? "Activating" : "Deactivating", request.getUsername());
        traineeService.activateDeactivateTrainee(request);
    }

    public void deleteTraineeById(Long id) {
        logger.warn("Facade: Deleting trainee by ID: {}. This is a hard delete operation.", id);
        traineeService.deleteTraineeById(id);
    }

    public void deleteTraineeByUsername(String username) {
        logger.warn("Facade: Deleting trainee by username: {}. This is a hard delete operation.", username);
        traineeService.deleteTraineeByUsername(username);
    }

    public List<TrainingResponse> getTraineeTrainingsList(@Valid TraineeTrainingListRequest request) {
        logger.info("Facade: Getting trainings list for trainee: {} with criteria.", request.getTraineeUsername());
        return trainingService.getTraineeTrainingsList(request);
    }


    // ------- Trainer Facade Methods -------

    public TrainerResponse createTrainer(@Valid TrainerCreateRequest request) {
        logger.info("Facade: Creating new trainer profile for first name: {}, last name: {}", request.getFirstName(), request.getLastName());
        return trainerService.createTrainer(request);
    }

    public TrainerResponse findTrainerById(Long id) {
        logger.info("Facade: Finding trainer by ID: {}", id);
        return trainerService.findTrainerById(id);
    }

    public TrainerResponse findTrainerByUsername(String username) {
        logger.info("Facade: Finding trainer by username: {}", username);
        return trainerService.findTrainerByUsername(username);
    }

    public List<TrainerResponse> getAllTrainers() {
        logger.info("Facade: Getting all trainers.");
        return trainerService.getAllTrainers();
    }

    public List<TrainerResponse> getUnassignedTrainersForTrainee(String traineeUsername) {
        logger.info("Facade: Getting unassigned trainers for trainee: {}", traineeUsername);
        return trainerService.getUnassignedTrainersForTrainee(traineeUsername);
    }

    public TrainerResponse updateTrainer(@Valid TrainerUpdateRequest request) {
        logger.info("Facade: Updating trainer profile for username: {}", request.getUsername());
        return trainerService.updateTrainer(request);
    }

    public void activateDeactivateTrainer(@Valid UserActivationRequest request) {
        logger.info("Facade: {} trainer '{}'.", request.isActive() ? "Activating" : "Deactivating", request.getUsername());
        trainerService.activateDeactivateTrainer(request);
    }

    public void deleteTrainerById(Long id) {
        logger.warn("Facade: Deleting trainer by ID: {}. This is a hard delete operation.", id);
        trainerService.deleteTrainerById(id);
    }

    public void deleteTrainerByUsername(String username) {
        logger.warn("Facade: Deleting trainer by username: {}. This is a hard delete operation.", username);
        trainerService.deleteTrainerByUsername(username);
    }

    public List<TrainingResponse> getTrainerTrainingsList(@Valid TrainerTrainingListRequest request) {
        logger.info("Facade: Getting trainings list for trainer: {} with criteria.", request.getTrainerUsername());
        return trainingService.getTrainerTrainingsList(request);
    }


    // ------ Training Facade Methods ------

    public TrainingResponse createTraining(@Valid TrainingCreateRequest request) {
        logger.info("Facade: Creating new training for trainee '{}' with trainer '{}'.", request.getTraineeUsername(), request.getTrainerUsername());
        return trainingService.createTraining(request);
    }

    public TrainingResponse getTrainingById(Long id) {
        logger.info("Facade: Getting training by ID: {}", id);
        return trainingService.getTrainingById(id);
    }

    public List<TrainingResponse> getAllTrainings() {
        logger.info("Facade: Getting all trainings.");
        return trainingService.getAllTrainings();
    }

    public TrainingResponse updateTraining(@Valid TrainingUpdateRequest request) {
        logger.info("Facade: Updating training with ID: {}.", request.getId());
        return trainingService.updateTraining(request);
    }

    public void deleteTrainingById(Long id) {
        logger.warn("Facade: Deleting training by ID: {}. This is a hard delete operation.", id);
        trainingService.deleteTrainingById(id);
    }
}