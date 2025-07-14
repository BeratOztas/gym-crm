package com.epam.gym_crm.service;

import java.time.LocalDate;
import java.util.List;

import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.model.TrainingType;

public interface ITrainingService {

	Training findById(Long id);

    List<Training> getAllTrainings();

    Training create(Training training);

    Training update(Training training);

    boolean delete(Long id);
    
	List<Training> findByTrainingName(String trainingName);

	List<Training> findByTrainingType(TrainingType trainingType);

	List<Training> findByTrainingDate(LocalDate trainingDate);

	List<Training> findByTraineeId(Long traineeId);

	List<Training> findByTrainerId(Long trainerId);
}
	