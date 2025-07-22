package com.epam.gym_crm.service;

import java.util.List;

import com.epam.gym_crm.dto.request.TraineeTrainingListRequest;
import com.epam.gym_crm.dto.request.TrainerTrainingListRequest;
import com.epam.gym_crm.dto.request.TrainingCreateRequest;
import com.epam.gym_crm.dto.request.TrainingUpdateRequest;
import com.epam.gym_crm.dto.response.TrainingResponse;

public interface ITrainingService {

	TrainingResponse getTrainingById(Long id);

	List<TrainingResponse> getAllTrainings();
    
    List<TrainingResponse> getTraineeTrainingsList(TraineeTrainingListRequest request);

    List<TrainingResponse> getTrainerTrainingsList(TrainerTrainingListRequest request);
    
    TrainingResponse createTraining(TrainingCreateRequest request);

    TrainingResponse updateTraining(TrainingUpdateRequest request);
	
	void deleteTrainingById(Long id);
}
	