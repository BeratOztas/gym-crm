package com.epam.gym_crm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.epam.gym_crm.dto.request.training.TrainingCreateRequest;
import com.epam.gym_crm.dto.request.training.TrainingUpdateRequest;
import com.epam.gym_crm.dto.response.TrainingResponse;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.service.ITrainingService;

import jakarta.validation.Valid;

@Tag(name = "Training ", description = "Operations related to creating, updating, and deleting trainings.")
@RestController
@RequestMapping("/api/trainings")
public class RestTrainingController {

	private static final Logger logger = LoggerFactory.getLogger(RestTrainingController.class);

	private final ITrainingService trainingService;

	public RestTrainingController(ITrainingService trainingService) {
		this.trainingService = trainingService;
	}

	@Operation(
		summary = "Retrieve a training by its ID",
		description = "Fetches the details of a specific training using its unique ID."
	)
	@ApiResponse(responseCode = "200", description = "Training found successfully",
		content = @Content(schema = @Schema(implementation = TrainingResponse.class)))
	@ApiResponse(responseCode = "404", description = "Training not found")
	@GetMapping("/{id}")
	public ResponseEntity<TrainingResponse> getTrainingById(
			@Parameter(description = "The unique ID of the training", required = true)
			@PathVariable Long id) {
		logger.info("Request to find training by ID: {}", id);
		return ResponseEntity.ok(trainingService.getTrainingById(id));
	}

	@Operation(
		summary = "Create a new training",
		description = "Creates a new training session by linking a trainee and a trainer, with details like training name and date."
	)
	@ApiResponse(responseCode = "201", description = "Training created successfully",
		content = @Content(schema = @Schema(implementation = String.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request - Validation error or invalid input")
	@ApiResponse(responseCode = "404", description = "Trainee or Trainer not found")
	@PostMapping
	public ResponseEntity<?> createTraining(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Training creation details", required = true)
			@RequestBody @Valid TrainingCreateRequest request) {
		logger.info("Request to create a new training for trainee: {}", request.getTraineeUsername());
		trainingService.createTraining(request);
		return ResponseEntity.status(HttpStatus.CREATED).body("Training created successfully.");
	}

	@Operation(
		summary = "Update an existing training",
		description = "Updates the details of a training session. The URL ID must match the ID in the request body."
	)
	@ApiResponse(responseCode = "200", description = "Training updated successfully",
		content = @Content(schema = @Schema(implementation = TrainingResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request - Validation error or ID mismatch")
	@ApiResponse(responseCode = "404", description = "Training not found")
	@PutMapping("/{id}")
	public ResponseEntity<TrainingResponse> updateTraining(
			@Parameter(description = "The unique ID of the training to update", required = true)
			@PathVariable Long id,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated training details", required = true)
			@RequestBody @Valid TrainingUpdateRequest request) {
		if (!id.equals(request.getId())) {
			logger.warn("Path-Body Mismatch: URL ID '{}' does not match request body ID '{}'.", id, request.getId());
			throw new BaseException(
					new ErrorMessage(MessageType.PATH_BODY_MISMATCH, "URL ID and request body ID must match."));
		}
		logger.info("Request to update training with ID: {}", id);
		return ResponseEntity.ok(trainingService.updateTraining(request));
	}

	@Operation(
		summary = "Delete a training by ID",
		description = "Deletes a specific training session by its unique ID."
	)
	@ApiResponse(responseCode = "204", description = "Training deleted successfully (No Content)")
	@ApiResponse(responseCode = "404", description = "Training not found")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteTrainingById(
			@Parameter(description = "The unique ID of the training to delete", required = true)
			@PathVariable Long id) {
		logger.info("Request to delete training with ID: {}", id);
		trainingService.deleteTrainingById(id);
		return ResponseEntity.noContent().build();
	}
}
