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
import com.epam.gym_crm.dto.request.trainer.TrainerCreateRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerTrainingListRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerUpdateRequest;
import com.epam.gym_crm.dto.response.TrainerProfileResponse;
import com.epam.gym_crm.dto.response.TrainerTrainingInfoResponse;
import com.epam.gym_crm.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.service.ITrainerService;
import com.epam.gym_crm.service.ITrainingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trainers")
public class RestTrainerController  {

	private static final Logger logger = LoggerFactory.getLogger(RestTrainerController.class);

	private final ITrainerService trainerService;
	private final ITrainingService trainingService;

	public RestTrainerController(ITrainerService trainerService, ITrainingService trainingService) {
		this.trainerService = trainerService;
		this.trainingService = trainingService;
	}

	@GetMapping("/{username}")
	public ResponseEntity<TrainerProfileResponse> findTrainerByUsername(@PathVariable String username) {
		logger.info("Request to find trainer by username: {}", username);
		return ResponseEntity.ok(trainerService.findTrainerByUsername(username));
	}

	@GetMapping("/{username}/trainings")
	public ResponseEntity<List<TrainerTrainingInfoResponse>> getTrainerTrainingsList(
			@PathVariable String username, @Valid TrainerTrainingListRequest request) {
		logger.info("Request to get trainings for trainer: {} with filters", username);
		return ResponseEntity.ok(trainingService.getTrainerTrainingsList(username,request));
	}

	@PostMapping
	public ResponseEntity<UserRegistrationResponse> createTrainer(
			@RequestBody @Valid TrainerCreateRequest request) {
		logger.info("Request to create new trainer: {}.{}", request.getFirstName(), request.getLastName());
		UserRegistrationResponse response = trainerService.createTrainer(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{username}")
	public ResponseEntity<TrainerProfileResponse> updateTrainer(@PathVariable String username,
			@RequestBody @Valid TrainerUpdateRequest request) {
		if (!username.equals(request.getUsername())) {
			logger.warn("Path-Body Mismatch: URL username '{}' does not match request body username '{}'.", username,
					request.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.PATH_BODY_MISMATCH,
					"URL username and request body username must match."));
		}
		logger.info("Request to update profile for trainer: {}", username);
		return ResponseEntity.ok(trainerService.updateTrainer(request));
	}

	@PatchMapping("/{username}/status")
	public ResponseEntity<?> activateDeactivateTrainer(@PathVariable String username,
			@RequestBody @Valid UserActivationRequest request) {
		if (!username.equals(request.getUsername())) {
			logger.warn("Path-Body Mismatch: URL username '{}' does not match request body username '{}'.", username,
					request.getUsername());
			throw new BaseException(new ErrorMessage(MessageType.PATH_BODY_MISMATCH,
					"URL username and request body username must match."));
		}
		logger.info("Request to update status for trainer: {}", username);
		trainerService.activateDeactivateTrainer(request);
		return ResponseEntity.ok("Trainer status updated successfully.");
	}

	@DeleteMapping("/{username}")
	public ResponseEntity<?> deleteTrainerByUsername(@PathVariable String username) {
		logger.info("Request to delete trainer: {}", username);
		trainerService.deleteTrainerByUsername(username);
		return ResponseEntity.noContent().build();
	}
}