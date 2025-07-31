package com.epam.gym_crm.service;

import java.util.List;

import com.epam.gym_crm.dto.request.trainee.TraineeTrainingListRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerTrainingListRequest;
import com.epam.gym_crm.dto.request.training.TrainingCreateRequest;
import com.epam.gym_crm.dto.request.training.TrainingUpdateRequest;
import com.epam.gym_crm.dto.response.TraineeTrainingInfoResponse;
import com.epam.gym_crm.dto.response.TrainerTrainingInfoResponse;
import com.epam.gym_crm.dto.response.TrainingResponse;

public interface ITrainingService {

	TrainingResponse getTrainingById(Long id);

	List<TrainingResponse> getAllTrainings();
    
    List<TraineeTrainingInfoResponse> getTraineeTrainingsList(String username,TraineeTrainingListRequest request);

    List<TrainerTrainingInfoResponse> getTrainerTrainingsList(String username,TrainerTrainingListRequest request);
    
    TrainingResponse createTraining(TrainingCreateRequest request);

    TrainingResponse updateTraining(TrainingUpdateRequest request);
	
	void deleteTrainingById(Long id);
}
	