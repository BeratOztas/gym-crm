package com.epam.gym_crm.service;

import java.util.List;

import com.epam.gym_crm.dto.request.TraineeCreateRequest;
import com.epam.gym_crm.dto.request.TraineeUpdateRequest;
import com.epam.gym_crm.dto.request.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.response.TraineeResponse;

public interface ITraineeService {

	TraineeResponse findTraineeById(Long id);

	TraineeResponse findTraineeByUsername(String username);

	List<TraineeResponse> getAllTrainees();

	TraineeResponse createTrainee(TraineeCreateRequest request);

	TraineeResponse updateTrainee(TraineeUpdateRequest trainee);
	
	TraineeResponse updateTraineeTrainersList(TraineeUpdateTrainersRequest request);
	
	void activateDeactivateTrainee(UserActivationRequest request);

	void deleteTraineeById(Long id);
	
	void deleteTraineeByUsername(String username);
}
