package com.epam.gym_crm.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym_crm.controller.ApiResponse;
import com.epam.gym_crm.controller.IRestTrainingController;
import com.epam.gym_crm.dto.request.training.TrainingCreateRequest;
import com.epam.gym_crm.dto.request.training.TrainingUpdateRequest;
import com.epam.gym_crm.dto.response.TrainingResponse;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.service.ITrainingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trainings")
public class RestTrainingControllerImpl implements IRestTrainingController {

	private static final Logger logger = LoggerFactory.getLogger(RestTrainingControllerImpl.class);

	private final ITrainingService trainingService;

	public RestTrainingControllerImpl(ITrainingService trainingService) {
		this.trainingService = trainingService;
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<TrainingResponse>> getTrainingById(@PathVariable Long id) {
		logger.info("Request to find training by ID: {}", id);
		return ResponseEntity.ok(ApiResponse.ok(trainingService.getTrainingById(id)));
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<?>> createTraining(@RequestBody @Valid TrainingCreateRequest request) {
		logger.info("Request to create a new training for trainee: {}", request.getTraineeUsername());
		trainingService.createTraining(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Training created successfully."));
	}

	@Override
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<TrainingResponse>> updateTraining(@PathVariable Long id,
			@RequestBody @Valid TrainingUpdateRequest request) {
		if (!id.equals(request.getId())) {
			logger.warn("Path-Body Mismatch: URL ID '{}' does not match request body ID '{}'.", id, request.getId());
			throw new BaseException(
					new ErrorMessage(MessageType.PATH_BODY_MISMATCH, "URL ID and request body ID must match."));
		}
		logger.info("Request to update training with ID: {}", id);
		return ResponseEntity.ok(ApiResponse.ok(trainingService.updateTraining(request)));
	}

	@Override
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<?>> deleteTrainingById(@PathVariable Long id) {
		logger.info("Request to delete training with ID: {}", id);
		trainingService.deleteTrainingById(id);
		return ResponseEntity.noContent().build();
	}
}