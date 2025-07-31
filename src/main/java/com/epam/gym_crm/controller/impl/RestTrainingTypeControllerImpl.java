package com.epam.gym_crm.controller.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym_crm.controller.ApiResponse;
import com.epam.gym_crm.controller.IRestTrainingTypeController;
import com.epam.gym_crm.model.TrainingType;
import com.epam.gym_crm.service.ITrainingTypeService;

@RestController
@RequestMapping("/api/training-types")
public class RestTrainingTypeControllerImpl implements IRestTrainingTypeController {

	private static final Logger logger = LoggerFactory.getLogger(RestTrainingTypeControllerImpl.class);

	private final ITrainingTypeService trainingTypeService;

	public RestTrainingTypeControllerImpl(ITrainingTypeService trainingTypeService) {
		this.trainingTypeService = trainingTypeService;
	}

	@Override
	@GetMapping(produces = "application/json")
	public ResponseEntity<ApiResponse<List<TrainingType>>> getTrainingTypes() {
		logger.info("Request received to get all training types.");
		return ResponseEntity.ok(ApiResponse.ok(trainingTypeService.getTrainingTypes()));
	}

}
