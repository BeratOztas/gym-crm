package com.epam.gym_crm.service.impl;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epam.gym_crm.auth.AuthManager;
import com.epam.gym_crm.dto.request.trainee.TraineeTrainingListRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerTrainingListRequest;
import com.epam.gym_crm.dto.request.training.TrainingCreateRequest;
import com.epam.gym_crm.dto.request.training.TrainingUpdateRequest;
import com.epam.gym_crm.dto.response.TraineeTrainingInfoResponse;
import com.epam.gym_crm.dto.response.TrainerTrainingInfoResponse;
import com.epam.gym_crm.dto.response.TrainingResponse;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.model.TrainingType;
import com.epam.gym_crm.model.User;
import com.epam.gym_crm.repository.TraineeRepository;
import com.epam.gym_crm.repository.TrainerRepository;
import com.epam.gym_crm.repository.TrainingRepository;
import com.epam.gym_crm.repository.TrainingTypeRepository;
import com.epam.gym_crm.service.ITrainingService;

@Service
public class TrainingServiceImpl implements ITrainingService {

	private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

	private final TrainingRepository trainingRepository;
	private final TraineeRepository traineeRepository;
	private final TrainerRepository trainerRepository;
	private final TrainingTypeRepository trainingTypeRepository;
	private final AuthManager authManager;

	public TrainingServiceImpl(TrainingRepository trainingRepository, TraineeRepository traineeRepository,
			TrainerRepository trainerRepository, TrainingTypeRepository trainingTypeRepository,
			AuthManager authManager) {
		this.trainingRepository = trainingRepository;
		this.traineeRepository = traineeRepository;
		this.trainerRepository = trainerRepository;
		this.trainingTypeRepository = trainingTypeRepository;
		this.authManager = authManager;
	}

	@Override
	@Transactional(readOnly = true)
	public TrainingResponse getTrainingById(Long id) {
		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to retrieve training with ID: {}.", currentUser.getUsername(), id);

		if (id == null || id <= 0) {
			logger.error("Training ID for lookup cannot be null or non-positive: {}. User: {}", id,
					currentUser.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Training ID for lookup must be a positive value. Provided ID: " + id));
		}

		Optional<Training> optTraining = trainingRepository.findById(id);

		Training foundTraining = optTraining.orElseThrow(() -> {
			logger.warn("Training not found with ID={} for user '{}'.", id, currentUser.getUsername());
			return new BaseException(
					new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Training not found with ID: " + id));
		});

		boolean isAssociatedTrainee = foundTraining.getTrainee().getUser().getUsername()
				.equals(currentUser.getUsername());
		boolean isAssociatedTrainer = foundTraining.getTrainer().getUser().getUsername()
				.equals(currentUser.getUsername());

		if (!isAssociatedTrainee && !isAssociatedTrainer) {
			logger.warn("Access Denied: User '{}' attempted to access training with ID {} not associated with them.",
					currentUser.getUsername(), id);
			throw new BaseException(
					new ErrorMessage(MessageType.UNAUTHORIZED, "You are not authorized to view this training."));
		}

		logger.info("Successfully retrieved training with ID: {} for user '{}'.", id, currentUser.getUsername());

		return new TrainingResponse(foundTraining);

	}

	@Override
	@Transactional(readOnly = true)
	public List<TrainingResponse> getAllTrainings() {
		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to retrieve all trainings.", currentUser.getUsername());

		List<Training> trainings = trainingRepository.findAll();

		List<TrainingResponse> responseList = trainings.stream().map(TrainingResponse::new)
				.collect(Collectors.toList());

		logger.info("Successfully retrieved {} trainings for user '{}'.", responseList.size(),
				currentUser.getUsername());

		return responseList;

	}

	@Override
	@Transactional(readOnly = true)
	public List<TraineeTrainingInfoResponse> getTraineeTrainingsList(String username,TraineeTrainingListRequest request) {
		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to retrieve trainings list for trainee '{}' with criteria: {}",
				currentUser.getUsername(), username, request);

		if (!currentUser.getUsername().equals(username)) {
			logger.warn("Access Denied: User '{}' attempted to access trainings of trainee '{}'.",
					currentUser.getUsername(), username);
			throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED,
					"You are not authorized to view trainings for other trainees."));
		}

		Trainee foundTrainee = traineeRepository.findByUserUsername(username).orElseThrow(() -> {
			logger.warn("Trainee with username '{}' not found when trying to retrieve their trainings.",
					username);
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
					"Trainee with username " +username + " not found."));
		});

		if (!foundTrainee.getUser().isActive()) {
			logger.warn("Trainee '{}' is not active. Cannot retrieve their trainings.", username);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_STATE,
					"Trainee " + username + " is not active. Cannot retrieve their trainings."));
		}

		List<Object[]> results = trainingRepository
                .findTraineeTrainingsByCriteria(username, request.getFromDate(),
                        request.getToDate(), request.getTrainerName(), request.getTrainingTypeName());

        if (results.isEmpty()) {
            logger.info("No trainings found for trainee '{}' with specified criteria.", username);
            return List.of();
        }

        // to DTO converter
        
        List<TraineeTrainingInfoResponse> filteredTraineeTrainingList = results.stream()
            .map(row -> new TraineeTrainingInfoResponse(
                (String) row[0], // training_name
                ((Date) row[1]).toLocalDate(), // training_date
                (String) row[2], // training_type_name
                (Integer) row[3], // training_duration
                (String) row[4]  // trainerName
            ))
            .collect(Collectors.toList());

        logger.info("Successfully retrieved and converted {} trainings for trainee '{}'.",
                filteredTraineeTrainingList.size(), username);

        return filteredTraineeTrainingList;
	}

	@Override
	@Transactional(readOnly = true)
	public List<TrainerTrainingInfoResponse> getTrainerTrainingsList(String username, TrainerTrainingListRequest request) {
		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to retrieve trainings list for trainer '{}' with criteria: {}",
				currentUser.getUsername(), username, request);

		if (!currentUser.getUsername().equals(username)) {
			logger.warn("Access Denied: User '{}' attempted to access trainings of trainer '{}'.",
					currentUser.getUsername(), username);
			throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED,
					"You are not authorized to view trainings for other trainers."));
		}

		Trainer foundTrainer = trainerRepository.findByUserUsername(username).orElseThrow(() -> {
			logger.warn("Trainer with username '{}' not found when trying to retrieve their trainings.",
					username);
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
					"Trainer with username " + username + " not found."));
		});

		if (!foundTrainer.getUser().isActive()) {
			logger.warn("Trainer '{}' is not active. Cannot retrieve their trainings.", username);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_STATE,
					"Trainer " + username + " is not active. Cannot retrieve their trainings."));
		}

		List<Object[]> results = trainingRepository
                .findTrainerTrainingsByCriteria(username, request.getFromDate(),
                        request.getToDate(), request.getTraineeName());

        if (results.isEmpty()) {
            logger.info("No trainings found for trainer '{}' with specified criteria.", username);
            return List.of();
        }

        // to DTO converter
        
        List<TrainerTrainingInfoResponse> filteredTrainerTrainingList = results.stream()
            .map(row -> new TrainerTrainingInfoResponse(
                (String) row[0], // training_name
                ((Date) row[1]).toLocalDate(), // training_date
                (String) row[2], // training_type_name
                (Integer) row[3], // training_duration
                (String) row[4]  // traineeName
            ))
            .collect(Collectors.toList());

        logger.info("Successfully retrieved and converted {} trainings for trainer '{}'.",
                filteredTrainerTrainingList.size(), username);

        return filteredTrainerTrainingList;

	}

	@Override
	@Transactional
	public TrainingResponse createTraining(TrainingCreateRequest request) {
		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to create a new training, Training creation request: {}",
				currentUser.getUsername(), request);

		Trainer foundTrainer = trainerRepository.findByUserUsername(request.getTrainerUsername()).orElseThrow(() -> {
			logger.warn("Trainer with username '{}' not found for training creation by user '{}'.",
					request.getTrainerUsername(), currentUser.getUsername());
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
					"Trainer with username " + request.getTrainerUsername() + " not found."));
		});

		if (!foundTrainer.getUser().isActive()) {
			logger.warn("Trainer '{}' is not active. Cannot create training.", request.getTrainerUsername());
			throw new BaseException(new ErrorMessage(MessageType.INVALID_STATE,
					"Trainer " + request.getTrainerUsername() + " is not active. Cannot create training."));
		}

		Trainee foundTrainee = traineeRepository.findByUserUsername(request.getTraineeUsername()).orElseThrow(() -> {
			logger.warn("Trainee with username '{}' not found for training creation by user '{}'.",
					request.getTraineeUsername(), currentUser.getUsername());
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
					"Trainee with username " + request.getTraineeUsername() + " not found."));
		});

		if (!foundTrainee.getUser().isActive()) {
			logger.warn("Trainee '{}' is not active. Cannot create training.", request.getTraineeUsername());
			throw new BaseException(new ErrorMessage(MessageType.INVALID_STATE,
					"Trainee " + request.getTraineeUsername() + " is not active. Cannot create training."));
		}

		TrainingType trainingType = trainingTypeRepository.findByTrainingTypeNameIgnoreCase(request.getTrainingName())
				.orElseThrow(() -> {
					logger.warn("Training  with name '{}' not found for training creation by user '{}'.",
							request.getTrainingName(), currentUser.getUsername());
					return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
							"Resource not found. : Training Type " + request.getTrainingName() + " not found."));
				});

		Training newTraining = new Training();
		newTraining.setTrainingName(request.getTrainingName());
		newTraining.setTrainingDate(request.getTrainingDate());
		newTraining.setTrainingDuration(request.getTrainingDuration());
		newTraining.setTrainer(foundTrainer);
		newTraining.setTrainee(foundTrainee);
		newTraining.setTrainingType(trainingType);

		Training savedTraining = trainingRepository.save(newTraining);

		if (!foundTrainee.getTrainers().contains(foundTrainer)) {
			foundTrainee.getTrainers().add(foundTrainer);
			foundTrainer.getTrainees().add(foundTrainee);

			traineeRepository.save(foundTrainee);
			logger.info("Assigned Trainer '{}' to Trainee '{}' as part of training creation.",
					foundTrainer.getUser().getUsername(), foundTrainee.getUser().getUsername());
		}

		logger.info("Training '{}' created successfully with ID: {} by user '{}'.", savedTraining.getTrainingName(),
				savedTraining.getId(), currentUser.getUsername());

		return new TrainingResponse(savedTraining);

	}

	@Override
	@Transactional
	public TrainingResponse updateTraining(TrainingUpdateRequest request) {
		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to update training with ID: {}. Update request: {}",
				currentUser.getUsername(), request.getId(), request);

		if (request.getId() == null || request.getId() <= 0) {
			logger.error("Training ID for update cannot be null or non-positive: {}. User: {}", request.getId(),
					currentUser.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Training ID for update must be a positive value. Provided ID: " + request.getId()));
		}

		Training existingTraining = trainingRepository.findById(request.getId()).orElseThrow(() -> {
			logger.warn("Training with ID {} not found for update by user '{}'.", request.getId(),
					currentUser.getUsername());
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
					"Training with ID " + request.getId() + " not found."));
		});

		boolean isAssociatedTrainee = existingTraining.getTrainee().getUser().getUsername()
				.equals(currentUser.getUsername());
		boolean isAssociatedTrainer = existingTraining.getTrainer().getUser().getUsername()
				.equals(currentUser.getUsername());

		if (!isAssociatedTrainee && !isAssociatedTrainer) {
			logger.warn("Access Denied: User '{}' attempted to update training with ID {} not associated with them.",
					currentUser.getUsername(), request.getId());
			throw new BaseException(
					new ErrorMessage(MessageType.UNAUTHORIZED, "You are not authorized to update this training."));
		}

		if (request.getTrainerUsername() != null && !request.getTrainerUsername().isEmpty()) {
			Trainer updatedTrainer = trainerRepository.findByUserUsername(request.getTrainerUsername())
					.orElseThrow(() -> {
						logger.warn(
								"New Trainer with username '{}' not found for updating training ID {} by user '{}'.",
								request.getTrainerUsername(), request.getId(), currentUser.getUsername());
						return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
								"New Trainer with username '" + request.getTrainerUsername() + "' not found.")); // Changed
																													// to
																													// English
					});
			if (!updatedTrainer.getUser().isActive()) {
				logger.warn("New Trainer '{}' is not active. Cannot update training.", request.getTrainerUsername());
				throw new BaseException(new ErrorMessage(MessageType.INVALID_STATE,
						"New Trainer '" + request.getTrainerUsername() + "' is not active. Cannot update training."));
			}
			existingTraining.setTrainer(updatedTrainer); // Update the trainer of the existing training
		}

		if (request.getTraineeUsername() != null && !request.getTraineeUsername().isEmpty()) {
			Trainee updatedTrainee = traineeRepository.findByUserUsername(request.getTraineeUsername())
					.orElseThrow(() -> {
						logger.warn(
								"New Trainee with username '{}' not found for updating training ID {} by user '{}'.",
								request.getTraineeUsername(), request.getId(), currentUser.getUsername());
						return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
								"New Trainee with username '" + request.getTraineeUsername() + "' not found.")); // Changed
																													// to
																													// English
					});
			if (!updatedTrainee.getUser().isActive()) {
				logger.warn("New Trainee '{}' is not active. Cannot update training.", request.getTraineeUsername());
				throw new BaseException(new ErrorMessage(MessageType.INVALID_STATE,
						"New Trainee '" + request.getTraineeUsername() + "' is not active. Cannot update training."));
			}
			existingTraining.setTrainee(updatedTrainee); // Update the trainee of the existing training
		}

		if (request.getTrainingTypeName() != null && !request.getTrainingTypeName().isEmpty()) {
			TrainingType updatedTrainingType = trainingTypeRepository
					.findByTrainingTypeNameIgnoreCase(request.getTrainingTypeName()).orElseThrow(() -> {
						logger.warn(
								"New Training Type with name '{}' not found for updating training ID {} by user '{}'.",
								request.getTrainingTypeName(), request.getId(), currentUser.getUsername());
						return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
								"New Training Type '" + request.getTrainingTypeName() + "' not found."));
					});
			existingTraining.setTrainingType(updatedTrainingType);
		}

		if (request.getTrainingName() != null) {
			existingTraining.setTrainingName(request.getTrainingName());
		}
		if (request.getTrainingDate() != null) {
			existingTraining.setTrainingDate(request.getTrainingDate());
		}
		if (request.getTrainingDuration() != null) {
			existingTraining.setTrainingDuration(request.getTrainingDuration());
		}

		Training updatedTraining = trainingRepository.save(existingTraining);

		logger.info("Training with ID: {} updated successfully by user '{}'.", updatedTraining.getId(),
				currentUser.getUsername());

		return new TrainingResponse(updatedTraining);
	}

	@Override
	@Transactional
	public void deleteTrainingById(Long id) {
		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to delete training with ID: {}.", currentUser.getUsername(), id);

		if (id == null || id <= 0) {
			logger.error("Training ID for deletion cannot be null or non-positive: {}. User: {}", id,
					currentUser.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Training ID for deletion must be a positive value. Provided ID: " + id));
		}

		Training trainingToDelete = trainingRepository.findById(id).orElseThrow(() -> {
			logger.warn("Training with ID {} not found for deletion by user '{}'.", id, currentUser.getUsername());
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
					"Training with ID " + id + " not found. Cannot delete."));
		});

		boolean isAssociatedTrainee = trainingToDelete.getTrainee().getUser().getUsername()
				.equals(currentUser.getUsername());
		boolean isAssociatedTrainer = trainingToDelete.getTrainer().getUser().getUsername()
				.equals(currentUser.getUsername());

		if (!isAssociatedTrainee && !isAssociatedTrainer) {
			logger.warn("Access Denied: User '{}' attempted to delete training with ID {} not associated with them.",
					currentUser.getUsername(), id);
			throw new BaseException(
					new ErrorMessage(MessageType.UNAUTHORIZED, "You are not authorized to delete this training."));
		}

		trainingRepository.delete(trainingToDelete);

		logger.info("Training with ID: {} deleted successfully by user '{}'.", id, currentUser.getUsername());

	}
}
