package com.epam.gym_crm.domain.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epam.gym_crm.api.dto.UserCreationResult;
import com.epam.gym_crm.api.dto.request.UserActivationRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerCreateRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerUpdateRequest;
import com.epam.gym_crm.api.dto.response.TrainerInfoResponse;
import com.epam.gym_crm.api.dto.response.TrainerProfileResponse;
import com.epam.gym_crm.api.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.db.entity.Trainee;
import com.epam.gym_crm.db.entity.Trainer;
import com.epam.gym_crm.db.entity.Training;
import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.TraineeRepository;
import com.epam.gym_crm.db.repository.TrainerRepository;
import com.epam.gym_crm.db.repository.TrainingRepository;
import com.epam.gym_crm.db.repository.TrainingTypeRepository;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.ErrorMessage;
import com.epam.gym_crm.domain.exception.MessageType;
import com.epam.gym_crm.domain.service.IAuthenticationService;
import com.epam.gym_crm.domain.service.ITrainerService;
import com.epam.gym_crm.monitoring.metric.AppMetrics;

import io.micrometer.core.annotation.Timed;

@Service
public class TrainerServiceImpl implements ITrainerService {

	private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

	private final IAuthenticationService authenticationService;
	private final TrainerRepository trainerRepository;
	private final TrainingTypeRepository trainingTypeRepository;
	private final TrainingRepository trainingRepository;
	private final TraineeRepository traineeRepository;
	private final UserRepository userRepository;
	private final AuthenticationInfoService authenticationInfoService;
	private final AppMetrics appMetrics;

	
	public TrainerServiceImpl(IAuthenticationService authenticationService, TrainerRepository trainerRepository,
			TrainingTypeRepository trainingTypeRepository,  UserRepository userRepository,AuthenticationInfoService authenticationInfoService,
			TrainingRepository trainingRepository, TraineeRepository traineeRepository,AppMetrics appMetrics) {
		this.authenticationService = authenticationService;
		this.trainerRepository = trainerRepository;
		this.trainingTypeRepository = trainingTypeRepository;
		this.userRepository = userRepository;
		this.authenticationInfoService=authenticationInfoService;
		this.trainingRepository = trainingRepository;
		this.traineeRepository = traineeRepository;
		this.appMetrics=appMetrics;
	}

	@Override
	@Transactional(readOnly = true)
	public TrainerProfileResponse findTrainerById(Long id) {
		String currentUsername =authenticationInfoService.getCurrentUsername();
		logger.info("User '{}' attempting to find Trainer profile by ID '{}'.", currentUsername, id);

		if (id == null || id <= 0) {
			logger.error("Trainer ID for lookup cannot be null or non-positive: {}", id);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Trainer ID must be a positive value. Provided: " + id));
		}

		Optional<Trainer> optTrainer = trainerRepository.findById(id);
		Trainer foundTrainer = optTrainer.orElseThrow(() -> {
			logger.warn("Trainer not found with ID={}", id);
			return new BaseException(
					new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainer not found with ID: " + id));
		});

		logger.info("Finding Trainer by ID={} -> Found: {}", id, true);
		return new TrainerProfileResponse(foundTrainer);
	}

	@Override
	@Transactional(readOnly = true)
	public TrainerProfileResponse findTrainerByUsername(String username) {

		String currentUsername =authenticationInfoService.getCurrentUsername();

		logger.info("User '{}' attempting to find Trainer profile for username '{}'.", currentUsername,
				username);

		if (username == null || username.isBlank()) {
			logger.error("Trainer username for lookup cannot be null or empty.");
			throw new BaseException(
					new ErrorMessage(MessageType.INVALID_ARGUMENT, "Trainer username must not be null or empty."));
		}

		Optional<Trainer> optTrainer = trainerRepository.findByUserUsername(username);

		Trainer foundTrainer = optTrainer.orElseThrow(() -> {
			logger.warn("Trainer not found with username:{}", username);
			return new BaseException(
					new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainer not found with username: " + username));
		});

		logger.info("Finding Trainer by username='{}' -> Found: {}", username, true);
		return new TrainerProfileResponse(foundTrainer);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TrainerProfileResponse> getAllTrainers() {
		String currentUsername =authenticationInfoService.getCurrentUsername();
		logger.info("User '{}' attempting to retrieve all Trainers.", currentUsername);

		List<Trainer> trainers = trainerRepository.findAll();
		logger.info("Retrieved {} trainers from the database.", trainers.size());

		List<TrainerProfileResponse> returnList = trainers.stream().map(TrainerProfileResponse::new)
				.collect(Collectors.toList());

		logger.info("Converted {} Trainer entities to TrainerResponse DTOs.", returnList.size());
		return returnList;
	}

	@Override
	@Transactional(readOnly = true)
	public List<TrainerInfoResponse> getUnassignedTrainersForTrainee(String traineeUsername) {
		String currentUsername =authenticationInfoService.getCurrentUsername();
		logger.info("User '{}' attempting to retrieve unassigned and active trainers for trainee '{}'.",
				currentUsername, traineeUsername);

		if (!currentUsername.equals(traineeUsername)) {
			logger.warn("Access Denied: User '{}' attempted to get unassigned trainers for trainee '{}'.",
					currentUsername, traineeUsername);
			throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED,
					"You are not authorized to view unassigned trainers for other trainees."));
		}

		Trainee foundTrainee = traineeRepository.findByUserUsername(traineeUsername).orElseThrow(() -> {
			logger.warn("Trainee with username '{}' not found when trying to get unassigned trainers.",
					traineeUsername);
			return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
					"Trainee with username " + traineeUsername + " not found."));
		});

		if (!foundTrainee.getUser().isActive()) {
			logger.warn("Trainee '{}' is not active. Cannot retrieve unassigned trainers.", traineeUsername);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_STATE,
					"Trainee " + traineeUsername + " is not active. Cannot retrieve unassigned trainers."));
		}

		List<Trainer> allActiveTrainers = trainerRepository.findByUserIsActive(true);

		Set<Trainer> assignedTrainers = foundTrainee.getTrainers();

		List<Trainer> unassignedTrainers = allActiveTrainers.stream()
				.filter(trainer -> !assignedTrainers.contains(trainer)).collect(Collectors.toList());

		List<TrainerInfoResponse> unassignedTrainerList = unassignedTrainers.stream().map(TrainerInfoResponse::new)
				.collect(Collectors.toList());

		logger.info("Successfully retrieved {} unassigned and active trainers for trainee '{}' for user '{}'.",
				unassignedTrainerList.size(), traineeUsername, currentUsername);

		return unassignedTrainerList;
	}

	@Override
	@Transactional()
	@Timed(value = "gym_crm_api_duration_seconds", extraTags = { "endpoint", "create_trainer" })
	public UserRegistrationResponse createTrainer(TrainerCreateRequest request) {
		if (request == null) {
			logger.error("Trainer must not be null");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Trainer must not be null"));
		}

		UserCreationResult creationResult = authenticationService.prepareUserWithCredentials(request.getFirstName(), request.getLastName());

		User newUser =creationResult.userToPersist();
		
		String rawPassword =creationResult.rawPassword();
		
		TrainingType specialization = trainingTypeRepository
				.findByTrainingTypeNameIgnoreCase(request.getSpecialization()).orElseThrow(() -> {
					logger.error("Training type not found: {}", request.getSpecialization());
					return new BaseException(new ErrorMessage(MessageType.ENTITY_NOT_FOUND,
							"Training type not found: " + request.getSpecialization()));
				});

		Trainer trainer = new Trainer();
		trainer.setSpecialization(specialization);
		trainer.setUser(newUser);

		Trainer savedTrainer = trainerRepository.save(trainer);

		if (savedTrainer == null || savedTrainer.getId() == null) {
			logger.error("Failed to save Trainer entity to the database for user: {}", newUser.getUsername());
			throw new BaseException(
					new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Failed to create trainer profile."));
		}
		
		String accessToken =authenticationService.createAccessToken(newUser);
		

		logger.info("Trainer profile created successfully for user: {}", newUser.getUsername());
		
		appMetrics.incrementTrainerCreation();

		return new UserRegistrationResponse(newUser.getUsername(), rawPassword,accessToken);

	}

	@Override
	@Transactional()
	public TrainerProfileResponse updateTrainer(TrainerUpdateRequest request) {
		String currentUsername =authenticationInfoService.getCurrentUsername();

		if (request == null || request.getUsername() == null || request.getUsername().isBlank()) {
			logger.error("Update request or username cannot be null/empty for Trainer profile.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Update request or username must not be null/empty."));
		}
		if (!currentUsername.equals(request.getUsername())) {
			logger.error("Unauthorized attempt to update Trainer profile for user '{}' by current user '{}'.",
					request.getUsername(), currentUsername);
			throw new BaseException(
					new ErrorMessage(MessageType.FORBIDDEN, "You are not authorized to update this Trainer profile."));
		}

		Optional<Trainer> optTrainer = trainerRepository.findByUserUsername(request.getUsername());

		if (optTrainer.isEmpty()) {
			logger.warn("Trainer profile not found for update with username: {}", request.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainer profile not found."));
		}

		Trainer trainerToUpdate = optTrainer.get();
		User userToUpdate = trainerToUpdate.getUser();

		// Username is used for authorization check only; it is not updated here.

		if (request.getFirstName() != null) {
			userToUpdate.setFirstName(request.getFirstName());
		}
		if (request.getLastName() != null) {
			userToUpdate.setLastName(request.getLastName());
		}

		if (request.getSpecialization() != null && !request.getSpecialization().isBlank()) {
			TrainingType newSpecialization = trainingTypeRepository
					.findByTrainingTypeNameIgnoreCase(request.getSpecialization()).orElseThrow(() -> {
						logger.warn("Invalid training type for update: {}", request.getSpecialization());
						return new BaseException(new ErrorMessage(MessageType.ENTITY_NOT_FOUND,
								"Training type not found: " + request.getSpecialization()));
					});
			trainerToUpdate.setSpecialization(newSpecialization);
		}
		Trainer updatedTrainer = trainerRepository.save(trainerToUpdate);

		logger.info("Trainer profile updated successfully for user: {}", userToUpdate.getUsername());

		return new TrainerProfileResponse(updatedTrainer);
	}

	@Override
	@Transactional()
	public void activateDeactivateTrainer(UserActivationRequest request) {
		String currentUsername =authenticationInfoService.getCurrentUsername();

		logger.info("User '{}' attempting to change activation status for Trainer '{}' to '{}'.",
				currentUsername, request.getUsername(), request.getIsActive());

		if (request == null || request.getUsername() == null || request.getUsername().isBlank()) {
			logger.error("Activation request or username cannot be null/empty.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Activation request or username must not be null/empty."));
		}

		Optional<Trainer> optTrainer = trainerRepository.findByUserUsername(request.getUsername());

		if (optTrainer.isEmpty()) {
			logger.warn("Trainer profile not found for activation status change with username: {}",
					request.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainer profile not found."));
		}

		Trainer trainerToUpdate = optTrainer.get();
		User userToUpdate = trainerToUpdate.getUser();

		if (userToUpdate.isActive() == request.getIsActive()) {
			logger.info("Trainer '{}' is already in the requested state (isActive: {}). No change needed.",
					request.getUsername(), request.getIsActive());
			return;
		}

		userToUpdate.setActive(request.getIsActive());

		User savedUser = userRepository.save(userToUpdate);
		trainerToUpdate.setUser(savedUser);
		trainerRepository.save(trainerToUpdate);

		logger.info("Trainer '{}' activation status changed to {} by user '{}'.", userToUpdate.getUsername(),
				userToUpdate.isActive(), currentUsername);

	}

	@Override
	@Transactional()
	public void deleteTrainerById(Long id) {
		String currentUsername =authenticationInfoService.getCurrentUsername();
		if (id == null || id <= 0) {
			logger.error("Trainer ID for deletion cannot be null or non-positive: {}", id);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Trainer ID for deletion must be a positive value. Provided ID: " + id));
		}

		Optional<Trainer> optTrainer = trainerRepository.findById(id);

		if (optTrainer.isEmpty()) {
			logger.warn("No trainer found to delete with ID={}", id);
			throw new BaseException(
					new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "No trainer found to delete with ID: " + id));
		}

		Trainer trainerToDelete = optTrainer.get();

		if (!currentUsername.equals(trainerToDelete.getUser().getUsername())) {
			logger.error("Unauthorized attempt to delete Trainer profile with ID '{}' by current user '{}'.", id,
					currentUsername);
			throw new BaseException(
					new ErrorMessage(MessageType.FORBIDDEN, "You are not authorized to delete this Trainer profile."));
		}

		Set<Training> associatedTrainings = trainerToDelete.getTrainings();
		if (associatedTrainings != null && !associatedTrainings.isEmpty()) {
			logger.info("Disassociating {} trainings from Trainer '{}' (ID: {}).", associatedTrainings.size(),
					trainerToDelete.getUser().getUsername(), id);
			for (Training training : associatedTrainings) {
				training.setTrainer(null);
				trainingRepository.save(training);
			}
		}

		trainerRepository.delete(trainerToDelete);
		logger.info(
				"Complete deletion of Trainer profile for ID '{}' (username: '{}') and associated User data performed successfully by user '{}'. Trainings disassociated.",
				id, trainerToDelete.getUser().getUsername(), currentUsername);

	}

	@Override
	@Transactional()
	public void deleteTrainerByUsername(String username) {
		String currentUsername =authenticationInfoService.getCurrentUsername();

		logger.info("User '{}' attempting to delete Trainer profile for username '{}'.", currentUsername,
				username);

		if (username == null || username.isBlank()) {
			logger.error("Username cannot be null or empty for Trainer deletion.");
			throw new BaseException(
					new ErrorMessage(MessageType.INVALID_ARGUMENT, "Username must not be null or empty."));
		}

		if (!currentUsername.equals(username)) {
			logger.error("Unauthorized attempt to delete Trainer profile for user '{}' by current user '{}'.", username,
					currentUsername);
			throw new BaseException(
					new ErrorMessage(MessageType.FORBIDDEN, "You are not authorized to delete this Trainer profile."));
		}

		Optional<Trainer> optTrainer = trainerRepository.findByUserUsername(username);

		if (optTrainer.isEmpty()) {
			logger.warn("Trainer profile not found for deletion with username: {}", username);
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainer profile not found."));
		}

		Trainer trainerToDelete = optTrainer.get();

		Set<Training> associatedTrainings = trainerToDelete.getTrainings();
		if (associatedTrainings != null && !associatedTrainings.isEmpty()) {
			logger.info("Disassociating {} trainings from Trainer '{}'.", associatedTrainings.size(), username);
			for (Training training : associatedTrainings) {
				training.setTrainer(null);
				trainingRepository.save(training);
			}
		}

		trainerRepository.delete(trainerToDelete);
		logger.info(
				"Complete deletion of Trainer profile for username '{}' and associated User data performed successfully by user '{}'. Trainings disassociated.",
				username, currentUsername);
	}

}
