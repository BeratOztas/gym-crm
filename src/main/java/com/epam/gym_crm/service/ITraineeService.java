package com.epam.gym_crm.service;

import java.util.List;

import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeCreateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.dto.response.TraineeProfileResponse;

public interface ITraineeService {

	TraineeProfileResponse findTraineeById(Long id);

	TraineeProfileResponse findTraineeByUsername(String username);

	List<TraineeProfileResponse> getAllTrainees();

	TraineeProfileResponse createTrainee(TraineeCreateRequest request);

	TraineeProfileResponse updateTrainee(TraineeUpdateRequest trainee);
	
	TraineeProfileResponse updateTraineeTrainersList(TraineeUpdateTrainersRequest request);
	
	void activateDeactivateTrainee(UserActivationRequest request);

	void deleteTraineeById(Long id);
	
	void deleteTraineeByUsername(String username);
}
