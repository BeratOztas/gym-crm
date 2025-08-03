package com.epam.gym_crm.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epam.gym_crm.auth.AuthManager;
import com.epam.gym_crm.db.entity.Trainee;
import com.epam.gym_crm.db.entity.Trainer;
import com.epam.gym_crm.db.entity.Training;
import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.TraineeRepository;
import com.epam.gym_crm.db.repository.TrainerRepository;
import com.epam.gym_crm.db.repository.TrainingRepository;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeCreateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.dto.response.TraineeProfileResponse;
import com.epam.gym_crm.dto.response.TrainerInfoResponse;
import com.epam.gym_crm.dto.response.UserRegistrationResponse;
// Exception imports
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.service.IAuthenticationService;
import com.epam.gym_crm.service.ITraineeService;

@Service
public class TraineeServiceImpl implements ITraineeService {

	private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

	private final TraineeRepository traineeRepository;
	private final TrainerRepository trainerRepository;
	private final TrainingRepository trainingRepository;
	private final IAuthenticationService authenticationService;
	private final UserRepository userRepository;
	private final AuthManager authManager;

	public TraineeServiceImpl(TraineeRepository traineeRepository, IAuthenticationService authenticationService,
			AuthManager authManager, UserRepository userRepository, TrainerRepository trainerRepository,TrainingRepository trainingRepository) {
		this.traineeRepository = traineeRepository;
		this.trainerRepository = trainerRepository;
		this.trainingRepository=trainingRepository;
		this.authenticationService = authenticationService;
		this.authManager = authManager;
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public TraineeProfileResponse findTraineeById(Long id) {

		User currentUser = authManager.getCurrentUser();

		logger.info("User '{}' attempting to find Trainee profile by ID '{}'.", currentUser.getUsername(), id);

		if (id == null || id <= 0) {
			logger.error("Trainee ID for lookup cannot be null or non-positive: {}", id);
			// Throw BaseException for invalid argument
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Trainee ID for lookup must be a positive value. Provided ID: " + id));
		}

		Optional<Trainee> optTrainee = traineeRepository.findById(id);

		Trainee foundTrainee = optTrainee.orElseThrow(() -> {
			logger.warn("Trainee not found with ID={}", id);
			return new BaseException(
					new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainee not found with ID: " + id));
		});
		logger.info("Finding Trainee by ID={} -> Found: {}", id, true);

		return new TraineeProfileResponse(foundTrainee);
	}

	@Override
	@Transactional(readOnly = true)
	public TraineeProfileResponse findTraineeByUsername(String username) {

		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to find Trainee profile for username '{}'.", currentUser.getUsername(),
				username);

		if (username == null || username.isBlank()) {
			logger.error("Trainee username for lookup cannot be null or empty.");
			// Throw BaseException for invalid argument
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Trainee username for lookup must not be null or empty."));
		}

		Optional<Trainee> optTrainee = traineeRepository.findByUserUsername(username);

		Trainee foundTrainee = optTrainee.orElseThrow(() -> {
			logger.warn("Trainee not found with username:{}", username);
			return new BaseException(
					new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainee not found with username: " + username));
		});

		logger.info("Finding Trainee by username='{}' -> Found: {}", username, true); // Always true if not thrown

		return new TraineeProfileResponse(foundTrainee);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TraineeProfileResponse> getAllTrainees() {
		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to retrieve all Trainees.", currentUser.getUsername());

		List<Trainee> trainees = traineeRepository.findAll();
		logger.info("Retrieving all trainees -> Count: {}", trainees.size());
		List<TraineeProfileResponse> returnList = trainees.stream().map(TraineeProfileResponse::new)
				.collect(Collectors.toList());

		logger.info("Converted {} Trainee entities to TraineeResponse DTOs.", returnList.size());
		return returnList;
	}

	@Override
	@Transactional()
	public UserRegistrationResponse createTrainee(TraineeCreateRequest request) {
		if (request == null) {
			logger.error("Trainee can not be null");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Trainee can not be null"));
		}

		User newUser = authenticationService.createAndSaveUser(request.getFirstName(), request.getLastName());

		logger.info("Received new transient User object for Trainee: Username = {}", newUser.getUsername());

		Trainee trainee = new Trainee();
		trainee.setDateOfBirth(request.getDateOfBirth());
		trainee.setAddress(request.getAddress());
		trainee.setUser(newUser);

		Trainee createdTrainee = traineeRepository.save(trainee);

		if (createdTrainee == null || createdTrainee.getId() == null) {
			logger.error("Failed to save Trainee entity to the database for user: {}", newUser.getUsername());
			throw new BaseException(
					new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Failed to create trainee profile."));
		}

		logger.info("Trainee profile created successfully for user: {}", newUser.getUsername());

		return new UserRegistrationResponse(newUser.getUsername(), newUser.getPassword());

	}

	@Override
	@Transactional()
	public TraineeProfileResponse updateTrainee(TraineeUpdateRequest request) {

		User currentUser = authManager.getCurrentUser();

		if (request == null || request.getUsername() == null || request.getUsername().isBlank()) {
			logger.error("Update request or username cannot be null/empty for Trainee profile.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Update request or username must not be null/empty."));
		}

		if (!currentUser.getUsername().equals(request.getUsername())) {
			logger.error("Unauthorized attempt to update Trainee profile for user '{}' by current user '{}'.",
					request.getUsername(), currentUser.getUsername());
			throw new BaseException(
					new ErrorMessage(MessageType.FORBIDDEN, "You are not authorized to update this Trainee profile."));
		}

		Optional<Trainee> optTrainee = traineeRepository.findByUserUsername(request.getUsername());

		if (optTrainee.isEmpty()) {
			logger.warn("Trainee profile not found for update with username: {}", request.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainee profile not found."));
		}

		Trainee traineeToUpdate = optTrainee.get();
		User userToUpdate = traineeToUpdate.getUser();

		// Username is used for authorization check only; it is not updated here.

		if (request.getFirstName() != null) {
			userToUpdate.setFirstName(request.getFirstName());
		}
		if (request.getLastName() != null) {
			userToUpdate.setLastName(request.getLastName());
		}

		if (request.getDateOfBirth() != null) {
			traineeToUpdate.setDateOfBirth(request.getDateOfBirth());
		}
		if (request.getAddress() != null && !request.getAddress().isBlank()) {
			traineeToUpdate.setAddress(request.getAddress());
		}

		User savedUser = userRepository.save(userToUpdate);

		traineeToUpdate.setUser(savedUser);

		Trainee updatedTrainee = traineeRepository.save(traineeToUpdate);

		logger.info("Trainee profile updated successfully for user: {}", userToUpdate.getUsername());

		return new TraineeProfileResponse(updatedTrainee);
	}


	@Override
	@Transactional
	public List<TrainerInfoResponse> updateTraineeTrainersList(TraineeUpdateTrainersRequest request) {
	    User currentUser = authManager.getCurrentUser();
	    
	    logger.info("User '{}' attempting to update trainers list for trainee '{}'. Requested trainers: {}.",
	            currentUser.getUsername(), request.getTraineeUsername(), request.getTrainerUsernames());

	    if (request == null || request.getTraineeUsername() == null || request.getTraineeUsername().isBlank()) {
	        throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
	                "Trainee username must not be null or empty in the update trainers list request."));
	    }
	    if (!currentUser.getUsername().equals(request.getTraineeUsername())) {
	        throw new BaseException(new ErrorMessage(MessageType.FORBIDDEN,
	                "You are not authorized to update trainers list for other trainees."));
	    }
	    Trainee traineeToUpdate = traineeRepository.findByUserUsername(request.getTraineeUsername()).orElseThrow(() -> {
	        return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
	                "Trainee with username " + request.getTraineeUsername() + " not found."));
	    });
	    if (!traineeToUpdate.getUser().isActive()) {
	        throw new BaseException(new ErrorMessage(MessageType.INVALID_STATE,
	                "Trainee " + request.getTraineeUsername() + " is not active. Cannot update their trainers list."));
	    }

	    // ----- Prepare New Trainers -----
	    
	    Set<Trainer> newTrainers = new HashSet<>();
	    if (request.getTrainerUsernames() != null && !request.getTrainerUsernames().isEmpty()) {
	        for (String trainerUsername : request.getTrainerUsernames()) {
	            Trainer trainer = trainerRepository.findByUserUsername(trainerUsername).orElseThrow(() -> {
	                return new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
	                        "Trainer with username " + trainerUsername + " not found."));
	            });
	            if (!trainer.getUser().isActive()) {
	                throw new BaseException(new ErrorMessage(MessageType.INVALID_STATE,
	                        "Trainer " + trainerUsername + " is not active. Cannot assign."));
	            }
	            newTrainers.add(trainer);
	        }
	    }

	    // ----- UPDATE trainee,trainer relations -----
	    
	    traineeToUpdate.setTrainers(newTrainers);
	    traineeRepository.save(traineeToUpdate);
	    logger.info("Successfully updated trainee_trainer join table for trainee '{}'.",
	            traineeToUpdate.getUser().getUsername());

	    // ------ UPDATE TRAINING DB -----
	    
	    // Delete old training for trainee
	    
	    trainingRepository.deleteByTraineeId(traineeToUpdate.getId());
	    logger.info("Deleted all old training records for trainee '{}' to synchronize with new trainers list.",
	            traineeToUpdate.getUser().getUsername());

	    // Create new trainigs for all new trainers  list
	    
	    for (Trainer trainer : newTrainers) {
	        TrainingType trainingType = trainer.getSpecialization();
	        
	        Training autoCreatedTraining = new Training();
	        autoCreatedTraining.setTrainee(traineeToUpdate);
	        autoCreatedTraining.setTrainer(trainer);
	        autoCreatedTraining.setTrainingType(trainingType);
	        autoCreatedTraining.setTrainingName(trainingType.getTrainingTypeName());
	        autoCreatedTraining.setTrainingDate(LocalDate.now());
	        autoCreatedTraining.setTrainingDuration(60); // Varsayılan süre
	        
	        trainingRepository.save(autoCreatedTraining);
	        logger.info("Created a new training record for trainee '{}' with trainer '{}'.",
	                traineeToUpdate.getUser().getUsername(), trainer.getUser().getUsername());
	    }

	    // ------ Response ------
	    
	    return new ArrayList<>(newTrainers).stream()
	            .map(TrainerInfoResponse::new)
	            .collect(Collectors.toList());
	}

	@Override
	@Transactional()
	public void activateDeactivateTrainee(UserActivationRequest request) {
		
		User currentUser = authManager.getCurrentUser();

		logger.info("User '{}' attempting to change activation status for Trainee '{}' to '{}'.",
				currentUser.getUsername(), request.getUsername(), request.getIsActive());

		if (request == null || request.getUsername() == null || request.getUsername().isBlank()) {
			logger.error("Activation request or username must not be null/empty.");
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Activation request or username must not be null/empty."));
		}
		Optional<Trainee> optTrainee = traineeRepository.findByUserUsername(request.getUsername());

		if (optTrainee.isEmpty()) {
			logger.warn("Trainee profile not found for activation status change with username: {}",
					request.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND,
					"Trainee with username '" + request.getUsername() + "' not found."));
		}

		Trainee traineeToUpdate = optTrainee.get();
		User userToUpdate = traineeToUpdate.getUser();

		// Check state of isActive if already same don't change it
		
		if (userToUpdate.isActive() == request.getIsActive()) {
			logger.info("Trainee '{}' is already in the requested state (isActive: {}). No change needed.",
					request.getUsername(), request.getIsActive());
			return;
		}

		userToUpdate.setActive(request.getIsActive());

		User savedUser = userRepository.save(userToUpdate);

		traineeToUpdate.setUser(savedUser);

		traineeRepository.save(traineeToUpdate);

		logger.info("Trainee '{}' activation status changed to {} by user '{}'.", userToUpdate.getUsername(),
				userToUpdate.isActive(), currentUser.getUsername());

	}

	@Override
	@Transactional()
	public void deleteTraineeById(Long id) {
		
		User currentUser = authManager.getCurrentUser();
		if (id == null || id <= 0) {
			logger.error("Trainee ID for deletion cannot be null or non-positive: {}", id);
			// Throw BaseException for invalid argument
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"Trainee ID for deletion must be a positive value. Provided ID: " + id));
		}

		Optional<Trainee> optTrainee = traineeRepository.findById(id);

		if (optTrainee.isEmpty()) {
			logger.warn("No trainee found to delete with ID={}", id);
			throw new BaseException(
					new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "No trainee found to delete with ID: " + id));
		}

		Trainee traineeToDelete = optTrainee.get();

		if (!currentUser.getUsername().equals(traineeToDelete.getUser().getUsername())) {
			logger.error("Unauthorized attempt to delete Trainee profile with ID '{}' by current user '{}'.", id,
					currentUser.getUsername());
			throw new BaseException(
					new ErrorMessage(MessageType.FORBIDDEN, "You are not authorized to delete this Trainee profile."));
		}

		traineeRepository.delete(traineeToDelete);

		logger.info(
				"Complete deletion of Trainee profile for ID '{}' (username: '{}') and all associated data (User, Trainings) performed successfully by user '{}'.",
				id, traineeToDelete.getUser().getUsername(), currentUser.getUsername());

	}

	@Override
	@Transactional()
	public void deleteTraineeByUsername(String username) {
		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to delete Trainee profile for username '{}'.", currentUser.getUsername(),
				username);

		if (username == null || username.isBlank()) {
			logger.error("Username cannot be null or empty for Trainee deletion.");
			throw new BaseException(
					new ErrorMessage(MessageType.INVALID_ARGUMENT, "Username must not be null or empty."));
		}

		if (!currentUser.getUsername().equals(username)) {
			logger.error("Unauthorized attempt to delete Trainee profile for user '{}' by current user '{}'.", username,
					currentUser.getUsername());
			throw new BaseException(
					new ErrorMessage(MessageType.FORBIDDEN, "You are not authorized to delete this Trainee profile."));
		}
		Optional<Trainee> optTrainee = traineeRepository.findByUserUsername(username);

		if (optTrainee.isEmpty()) {
			logger.warn("Trainee profile not found for deletion with username: {}", username);
			throw new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainee profile not found."));
		}

		Trainee traineeToDelete = optTrainee.get();

		traineeRepository.delete(traineeToDelete);

		logger.info(
				"Complete deletion of Trainee profile for username '{}' and all associated data (User, Trainings) performed successfully by user '{}'.",
				username, currentUser.getUsername());

	}

}