package com.epam.gym_crm.domain.service;

import java.util.List;

import com.epam.gym_crm.api.dto.request.UserActivationRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerCreateRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerUpdateRequest;
import com.epam.gym_crm.api.dto.response.TrainerInfoResponse;
import com.epam.gym_crm.api.dto.response.TrainerProfileResponse;
import com.epam.gym_crm.api.dto.response.UserRegistrationResponse;

public interface ITrainerService {

	TrainerProfileResponse findTrainerById(Long id);

	TrainerProfileResponse findTrainerByUsername(String username); 

	List<TrainerProfileResponse> getAllTrainers();
	
	List<TrainerInfoResponse> getUnassignedTrainersForTrainee(String traineeUsername);

	UserRegistrationResponse createTrainer(TrainerCreateRequest request);

	TrainerProfileResponse updateTrainer(TrainerUpdateRequest request);

	void activateDeactivateTrainer(UserActivationRequest request);

	void deleteTrainerById(Long id);

	void deleteTrainerByUsername(String username);
}
