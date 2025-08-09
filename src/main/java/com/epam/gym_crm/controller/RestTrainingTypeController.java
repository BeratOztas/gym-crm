package com.epam.gym_crm.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.service.ITrainingTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Training Type ", description = "Operations related to retrieving training types.")
@RestController
@RequestMapping("/api/training-types")
public class RestTrainingTypeController {

	private static final Logger logger = LoggerFactory.getLogger(RestTrainingTypeController.class);

	private final ITrainingTypeService trainingTypeService;

	public RestTrainingTypeController(ITrainingTypeService trainingTypeService) {
		this.trainingTypeService = trainingTypeService;
	}

	@Operation(
		summary = "Get all training types",
		description = "Retrieves a list of all available training types."
	)
	@ApiResponse(responseCode = "200", description = "Training types retrieved successfully",
		content = @Content(schema = @Schema(implementation = List.class)))
	@GetMapping(produces = "application/json")
	public ResponseEntity<List<TrainingType>> getTrainingTypes() {
		logger.info("Request received to get all training types.");
		return ResponseEntity.ok(trainingTypeService.getTrainingTypes());
	}

}
