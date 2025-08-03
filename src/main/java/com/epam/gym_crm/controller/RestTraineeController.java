package com.epam.gym_crm.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeCreateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeTrainingListRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.dto.response.TraineeProfileResponse;
import com.epam.gym_crm.dto.response.TraineeTrainingInfoResponse;
import com.epam.gym_crm.dto.response.TrainerInfoResponse;
import com.epam.gym_crm.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.service.ITraineeService;
import com.epam.gym_crm.service.ITrainerService;
import com.epam.gym_crm.service.ITrainingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trainees")
public class RestTraineeController {

	private static final Logger logger = LoggerFactory.getLogger(RestTraineeController.class);

	private final ITraineeService traineeService;
	private final ITrainerService trainerService;
	private final ITrainingService trainingService;

	public RestTraineeController(ITraineeService traineeService, ITrainerService trainerService,
			ITrainingService trainingService) {
		this.traineeService = traineeService;
		this.trainerService = trainerService;
		this.trainingService = trainingService;
	}

	@GetMapping("/{username}")
	public ResponseEntity<TraineeProfileResponse> findTraineeByUsername(@PathVariable String username) {
		logger.info("Request to find trainee by username: {}", username);
		return ResponseEntity.ok(traineeService.findTraineeByUsername(username));
	}

	@GetMapping("/{username}/unassigned-trainers")
	public ResponseEntity<List<TrainerInfoResponse>> getUnassignedTrainersForTrainee(
			@PathVariable("username") String traineeUsername) {
		logger.info("Request to get unassigned trainers for trainee: {}", traineeUsername);
		return ResponseEntity.ok(trainerService.getUnassignedTrainersForTrainee(traineeUsername));
	}

	@GetMapping("/{username}/trainings")
	public ResponseEntity<List<TraineeTrainingInfoResponse>> getTraineeTrainingsList(@PathVariable String username,
			@Valid TraineeTrainingListRequest request) {

		logger.info("Request to get trainings for trainee: {} with filters", username);
		return ResponseEntity.ok(trainingService.getTraineeTrainingsList(username, request));
	}

	@PostMapping
	public ResponseEntity<UserRegistrationResponse> createTrainee(@RequestBody @Valid TraineeCreateRequest request) {
		logger.info("Request to create new trainee: {}.{}", request.getFirstName(), request.getLastName());
		UserRegistrationResponse response = traineeService.createTrainee(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{username}")
	public ResponseEntity<TraineeProfileResponse> updateTrainee(@PathVariable String username,
			@RequestBody @Valid TraineeUpdateRequest request) {
		if (!username.equals(request.getUsername())) {
			logger.warn("Path-Body Mismatch: URL username '{}' does not match request body username '{}'.", username,
					request.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.PATH_BODY_MISMATCH,
					"URL username and request body username must match."));
		}
		logger.info("Request to update profile for trainee: {}", username);
		return ResponseEntity.ok(traineeService.updateTrainee(request));
	}

	@PutMapping("/{username}/trainers")
	public ResponseEntity<List<TrainerInfoResponse>> updateTraineeTrainersList(@PathVariable String username,
			@RequestBody @Valid TraineeUpdateTrainersRequest request) {

		if (!username.equals(request.getTraineeUsername())) {
			logger.warn("Path-Body Mismatch: URL username '{}' does not match request body username '{}'.", username,
					request.getTraineeUsername());
			throw new BaseException(new ErrorMessage(MessageType.PATH_BODY_MISMATCH,
					"URL username and request body username must match."));
		}

		logger.info("Request to update trainers list for trainee: {}", username);
		return ResponseEntity.ok(traineeService.updateTraineeTrainersList(request));
	}

	@PatchMapping("/{username}/status")
	public ResponseEntity<?> activateDeactivateTrainee(@PathVariable String username,
			@RequestBody @Valid UserActivationRequest request) {

		if (!username.equals(request.getUsername())) {
			logger.warn("Path-Body Mismatch: URL username '{}' does not match request body username '{}'.", username,
					request.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.PATH_BODY_MISMATCH,
					"URL username and request body username must match."));
		}

		logger.info("Request to update status for trainee: {}", username);
		traineeService.activateDeactivateTrainee(request);
		return ResponseEntity.ok("Trainee status updated successfully.");
	}

	@DeleteMapping("/{username}")
	public ResponseEntity<?> deleteTraineeByUsername(@PathVariable String username) {

		logger.info("Request to delete trainee: {}", username);
		traineeService.deleteTraineeByUsername(username);
		return ResponseEntity.noContent().build();
	}

}