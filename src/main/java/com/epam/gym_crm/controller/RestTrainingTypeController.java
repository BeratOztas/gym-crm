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

@RestController
@RequestMapping("/api/training-types")
public class RestTrainingTypeController {

	private static final Logger logger = LoggerFactory.getLogger(RestTrainingTypeController.class);

	private final ITrainingTypeService trainingTypeService;

	public RestTrainingTypeController(ITrainingTypeService trainingTypeService) {
		this.trainingTypeService = trainingTypeService;
	}

	@GetMapping(produces = "application/json")
	public ResponseEntity<List<TrainingType>> getTrainingTypes() {
		logger.info("Request received to get all training types.");
		return ResponseEntity.ok(trainingTypeService.getTrainingTypes());
	}

}
