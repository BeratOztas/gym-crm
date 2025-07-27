package com.epam.gym_crm.service;

import java.util.List;

import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerCreateRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerUpdateRequest;
import com.epam.gym_crm.dto.response.TrainerProfileResponse;

public interface ITrainerService {

	TrainerProfileResponse findTrainerById(Long id);

	TrainerProfileResponse findTrainerByUsername(String username); 

	List<TrainerProfileResponse> getAllTrainers();
	
	List<TrainerProfileResponse> getUnassignedTrainersForTrainee(String traineeUsername);

	TrainerProfileResponse createTrainer(TrainerCreateRequest request);

	TrainerProfileResponse updateTrainer(TrainerUpdateRequest request);

	void activateDeactivateTrainer(UserActivationRequest request);

	void deleteTrainerById(Long id);

	void deleteTrainerByUsername(String username);
}
