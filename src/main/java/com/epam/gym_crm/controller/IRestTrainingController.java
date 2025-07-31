package com.epam.gym_crm.controller;

import org.springframework.http.ResponseEntity;

import com.epam.gym_crm.dto.request.training.TrainingCreateRequest;
import com.epam.gym_crm.dto.request.training.TrainingUpdateRequest;
import com.epam.gym_crm.dto.response.TrainingResponse; 

public interface IRestTrainingController {

    ResponseEntity<ApiResponse<TrainingResponse>> getTrainingById(Long id);

    ResponseEntity<ApiResponse<?>> createTraining(TrainingCreateRequest request);

    ResponseEntity<ApiResponse<TrainingResponse>> updateTraining(Long id, TrainingUpdateRequest request);

    ResponseEntity<ApiResponse<?>> deleteTrainingById(Long id);
}
