package com.epam.gym_crm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerCreateRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerTrainingListRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerUpdateRequest;
import com.epam.gym_crm.dto.response.TrainerProfileResponse;
import com.epam.gym_crm.dto.response.TrainerTrainingInfoResponse;
import com.epam.gym_crm.dto.response.UserRegistrationResponse;

public interface IRestTrainerController {

	ResponseEntity<ApiResponse<TrainerProfileResponse>> findTrainerByUsername(String username);

	ResponseEntity<ApiResponse<List<TrainerTrainingInfoResponse>>> getTrainerTrainingsList(String username,
			TrainerTrainingListRequest request);

	ResponseEntity<ApiResponse<UserRegistrationResponse>> createTrainer(TrainerCreateRequest request);

	ResponseEntity<ApiResponse<TrainerProfileResponse>> updateTrainer(String username, TrainerUpdateRequest request);

	ResponseEntity<ApiResponse<?>> activateDeactivateTrainer(String username, UserActivationRequest request);

	ResponseEntity<ApiResponse<?>> deleteTrainerByUsername(String username);

}