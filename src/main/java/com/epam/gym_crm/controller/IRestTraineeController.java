package com.epam.gym_crm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeCreateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeTrainingListRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.dto.response.TraineeProfileResponse;
import com.epam.gym_crm.dto.response.TraineeTrainingInfoResponse;
import com.epam.gym_crm.dto.response.TrainerInfoResponse;
import com.epam.gym_crm.dto.response.UserRegistrationResponse;

public interface IRestTraineeController {

	ResponseEntity<ApiResponse<TraineeProfileResponse>> findTraineeByUsername(String username);

	ResponseEntity<ApiResponse<List<TrainerInfoResponse>>> getUnassignedTrainersForTrainee(String traineeUsername);

	ResponseEntity<ApiResponse<List<TraineeTrainingInfoResponse>>> getTraineeTrainingsList(String username,
			TraineeTrainingListRequest request);

	ResponseEntity<ApiResponse<UserRegistrationResponse>> createTrainee(TraineeCreateRequest request);

	ResponseEntity<ApiResponse<TraineeProfileResponse>> updateTrainee(String username, TraineeUpdateRequest request);

	ResponseEntity<ApiResponse<List<TrainerInfoResponse>>> updateTraineeTrainersList(String username,
			TraineeUpdateTrainersRequest request);

	ResponseEntity<ApiResponse<?>> activateDeactivateTrainee(String username, UserActivationRequest request);

	ResponseEntity<ApiResponse<?>> deleteTraineeByUsername(String username);
}
