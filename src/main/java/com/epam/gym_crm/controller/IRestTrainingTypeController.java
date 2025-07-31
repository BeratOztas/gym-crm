package com.epam.gym_crm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.epam.gym_crm.model.TrainingType;

public interface IRestTrainingTypeController {
	ResponseEntity<ApiResponse<List<TrainingType>>> getTrainingTypes();
}
