package com.epam.gym_crm.service;

import java.util.List;

import com.epam.gym_crm.dto.request.TrainerCreateRequest;
import com.epam.gym_crm.dto.request.TrainerUpdateRequest;
import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.response.TrainerResponse;

public interface ITrainerService {

	TrainerResponse findTrainerById(Long id);

	TrainerResponse findTrainerByUsername(String username); 

	List<TrainerResponse> getAllTrainers();
	
	List<TrainerResponse> getUnassignedTrainersForTrainee(String traineeUsername);

	TrainerResponse createTrainer(TrainerCreateRequest request);

	TrainerResponse updateTrainer(TrainerUpdateRequest request);

	void activateDeactivateTrainer(UserActivationRequest request);

	void deleteTrainerById(Long id);

	void deleteTrainerByUsername(String username);
}
