package com.epam.gym_crm.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.epam.gym_crm.api.dto.request.UserActivationRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerCreateRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerTrainingListRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerUpdateRequest;
import com.epam.gym_crm.api.dto.response.TrainerProfileResponse;
import com.epam.gym_crm.api.dto.response.TrainerTrainingInfoResponse;
import com.epam.gym_crm.api.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.ErrorMessage;
import com.epam.gym_crm.domain.exception.MessageType;
import com.epam.gym_crm.domain.service.ITrainerService;
import com.epam.gym_crm.domain.service.ITrainingService;

import java.util.List;

@Tag(name = "Trainer ", description = "Operations related to Trainer accounts and their trainings.")
@RestController
@RequestMapping("/api/trainers")
public class RestTrainerController {

	private static final Logger logger = LoggerFactory.getLogger(RestTrainerController.class);

	private final ITrainerService trainerService;
	private final ITrainingService trainingService;

	public RestTrainerController(ITrainerService trainerService, ITrainingService trainingService) {
		this.trainerService = trainerService;
		this.trainingService = trainingService;
	}

	@Operation(
		summary = "Retrieve a trainer profile by username",
		description = "Fetches the detailed profile of a trainer using their unique username."
	)
	@ApiResponse(responseCode = "200", description = "Trainer profile found successfully",
		content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class)))
	@ApiResponse(responseCode = "404", description = "Trainer not found")
	@GetMapping("/{username}")
	public ResponseEntity<TrainerProfileResponse> findTrainerByUsername(
			@Parameter(description = "Username of the trainer to retrieve", required = true)
			@PathVariable String username) {
		logger.info("Request to find trainer by username: {}", username);
		return ResponseEntity.ok(trainerService.findTrainerByUsername(username));
	}

	@Operation(
		summary = "Get a trainer's training list",
		description = "Retrieves a list of trainings for a trainer, with optional filtering by date and trainee name."
	)
	@ApiResponse(responseCode = "200", description = "Trainer training list returned successfully",
		content = @Content(schema = @Schema(implementation = List.class)))
	@ApiResponse(responseCode = "404", description = "Trainer not found")
	@GetMapping("/{username}/trainings")
	public ResponseEntity<List<TrainerTrainingInfoResponse>> getTrainerTrainingsList(
			@Parameter(description = "Username of the trainer", required = true)
			@PathVariable String username,
			@Parameter(hidden = true)
			@Valid TrainerTrainingListRequest request) {
		logger.info("Request to get trainings for trainer: {} with filters", username);
		return ResponseEntity.ok(trainingService.getTrainerTrainingsList(username, request));
	}

	@Operation(
		summary = "Create a new trainer profile",
		description = "Registers a new trainer with their first name, last name, and specialization. Returns the generated username and password."
	)
	@ApiResponse(responseCode = "201", description = "Trainer created successfully",
		content = @Content(schema = @Schema(implementation = UserRegistrationResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request - Validation error or invalid input",
		content = @Content(schema = @Schema(implementation = String.class)))
	@PostMapping
	public ResponseEntity<UserRegistrationResponse> createTrainer(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Trainer creation details", required = true)
			@RequestBody @Valid TrainerCreateRequest request) {
		logger.info("Request to create new trainer: {}.{}", request.getFirstName(), request.getLastName());
		UserRegistrationResponse response = trainerService.createTrainer(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(
		summary = "Update a trainer profile",
		description = "Updates the profile details of an existing trainer. The URL username must match the username in the request body."
	)
	@ApiResponse(responseCode = "200", description = "Trainer profile updated successfully",
		content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request - Validation error or username mismatch")
	@ApiResponse(responseCode = "404", description = "Trainer not found")
	@PutMapping("/{username}")
	public ResponseEntity<TrainerProfileResponse> updateTrainer(
			@Parameter(description = "Username of the trainer to update", required = true)
			@PathVariable String username,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated trainer details", required = true)
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

	@Operation(
		summary = "Activate or deactivate a trainer",
		description = "Changes the active status of a trainer's account. The URL username must match the username in the request body."
	)
	@ApiResponse(responseCode = "200", description = "Trainer status updated successfully")
	@ApiResponse(responseCode = "400", description = "Bad Request - Validation error or username mismatch")
	@ApiResponse(responseCode = "404", description = "Trainer not found")
	@PatchMapping("/{username}/status")
	public ResponseEntity<?> activateDeactivateTrainer(
			@Parameter(description = "Username of the trainer", required = true)
			@PathVariable String username,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Activation status details", required = true)
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

	@Operation(
		summary = "Delete a trainer profile",
		description = "Deletes a trainer account by their username."
	)
	@ApiResponse(responseCode = "204", description = "Trainer deleted successfully (No Content)")
	@ApiResponse(responseCode = "404", description = "Trainer not found")
	@DeleteMapping("/{username}")
	public ResponseEntity<?> deleteTrainerByUsername(
			@Parameter(description = "Username of the trainer to delete", required = true)
			@PathVariable String username) {
		logger.info("Request to delete trainer: {}", username);
		trainerService.deleteTrainerByUsername(username);
		return ResponseEntity.noContent().build();
	}
}
