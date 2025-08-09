package com.epam.gym_crm.controller;

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

import java.util.List;

@Tag(name = "Trainee ", description = "Operations related to Trainee accounts and their trainings.")
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

	@Operation(
		summary = "Retrieve a trainee profile by username",
		description = "Fetches the detailed profile of a trainee using their unique username."
	)
	@ApiResponse(responseCode = "200", description = "Trainee profile found successfully",
		content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class)))
	@ApiResponse(responseCode = "404", description = "Trainee not found")
	@GetMapping("/{username}")
	public ResponseEntity<TraineeProfileResponse> findTraineeByUsername(
			@Parameter(description = "Username of the trainee to retrieve", required = true)
			@PathVariable String username) {
		logger.info("Request to find trainee by username: {}", username);
		return ResponseEntity.ok(traineeService.findTraineeByUsername(username));
	}

	@Operation(
		summary = "Get unassigned trainers for a trainee",
		description = "Retrieves a list of trainers who are not assigned to the specified trainee."
	)
	@ApiResponse(responseCode = "200", description = "List of unassigned trainers returned successfully",
		content = @Content(schema = @Schema(implementation = List.class)))
	@ApiResponse(responseCode = "404", description = "Trainee not found")
	@GetMapping("/{username}/unassigned-trainers")
	public ResponseEntity<List<TrainerInfoResponse>> getUnassignedTrainersForTrainee(
			@Parameter(description = "Username of the trainee", required = true)
			@PathVariable("username") String traineeUsername) {
		logger.info("Request to get unassigned trainers for trainee: {}", traineeUsername);
		return ResponseEntity.ok(trainerService.getUnassignedTrainersForTrainee(traineeUsername));
	}

	@Operation(
		summary = "Get a trainee's training list",
		description = "Retrieves a list of trainings for a trainee, with optional filtering by date, trainer, and training type."
	)
	@ApiResponse(responseCode = "200", description = "Trainee training list returned successfully",
		content = @Content(schema = @Schema(implementation = List.class)))
	@ApiResponse(responseCode = "404", description = "Trainee not found")
	@GetMapping("/{username}/trainings")
	public ResponseEntity<List<TraineeTrainingInfoResponse>> getTraineeTrainingsList(
			@Parameter(description = "Username of the trainee", required = true)
			@PathVariable String username,
			@Parameter(hidden = true)
			@Valid TraineeTrainingListRequest request) {
		logger.info("Request to get trainings for trainee: {} with filters", username);
		return ResponseEntity.ok(trainingService.getTraineeTrainingsList(username, request));
	}

	@Operation(
		summary = "Create a new trainee profile",
		description = "Registers a new trainee with their first name, last name, and other details. Returns the generated username and password."
	)
	@ApiResponse(responseCode = "201", description = "Trainee created successfully",
		content = @Content(schema = @Schema(implementation = UserRegistrationResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request - Validation error or invalid input",
		content = @Content(schema = @Schema(implementation = String.class)))
	@PostMapping
	public ResponseEntity<UserRegistrationResponse> createTrainee(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Trainee creation details", required = true)
			@RequestBody @Valid TraineeCreateRequest request) {
		logger.info("Request to create new trainee: {}.{}", request.getFirstName(), request.getLastName());
		UserRegistrationResponse response = traineeService.createTrainee(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(
		summary = "Update a trainee profile",
		description = "Updates the profile details of an existing trainee. The URL username must match the username in the request body."
	)
	@ApiResponse(responseCode = "200", description = "Trainee profile updated successfully",
		content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request - Validation error or username mismatch")
	@ApiResponse(responseCode = "404", description = "Trainee not found")
	@PutMapping("/{username}")
	public ResponseEntity<TraineeProfileResponse> updateTrainee(
			@Parameter(description = "Username of the trainee to update", required = true)
			@PathVariable String username,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated trainee details", required = true)
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

	@Operation(
		summary = "Update a trainee's trainers list",
		description = "Updates the list of trainers assigned to a specific trainee. The URL username must match the username in the request body."
	)
	@ApiResponse(responseCode = "200", description = "Trainee trainers list updated successfully",
		content = @Content(schema = @Schema(implementation = List.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request - Validation error or username mismatch")
	@ApiResponse(responseCode = "404", description = "Trainee not found")
	@PutMapping("/{username}/trainers")
	public ResponseEntity<List<TrainerInfoResponse>> updateTraineeTrainersList(
			@Parameter(description = "Username of the trainee", required = true)
			@PathVariable String username,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of trainer usernames to assign to the trainee", required = true)
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

	@Operation(
		summary = "Activate or deactivate a trainee",
		description = "Changes the active status of a trainee's account. The URL username must match the username in the request body."
	)
	@ApiResponse(responseCode = "200", description = "Trainee status updated successfully")
	@ApiResponse(responseCode = "400", description = "Bad Request - Validation error or username mismatch")
	@ApiResponse(responseCode = "404", description = "Trainee not found")
	@PatchMapping("/{username}/status")
	public ResponseEntity<?> activateDeactivateTrainee(
			@Parameter(description = "Username of the trainee", required = true)
			@PathVariable String username,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Activation status details", required = true)
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

	@Operation(
		summary = "Delete a trainee profile",
		description = "Deletes a trainee account by their username."
	)
	@ApiResponse(responseCode = "204", description = "Trainee deleted successfully (No Content)")
	@ApiResponse(responseCode = "404", description = "Trainee not found")
	@DeleteMapping("/{username}")
	public ResponseEntity<?> deleteTraineeByUsername(
			@Parameter(description = "Username of the trainee to delete", required = true)
			@PathVariable String username) {
		logger.info("Request to delete trainee: {}", username);
		traineeService.deleteTraineeByUsername(username);
		return ResponseEntity.noContent().build();
	}

}
