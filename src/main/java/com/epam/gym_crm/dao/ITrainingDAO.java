package com.epam.gym_crm.dao;

import java.time.LocalDate;
import java.util.List;

import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.model.TrainingType;

public interface ITrainingDAO extends BaseDAO<Training> {

	List<Training> findByTrainingName(String trainingName);

	List<Training> findByTrainingType(TrainingType trainingType);

	List<Training> findByTrainingDate(LocalDate trainingDate);

	List<Training> findByTraineeId(Long traineeId);

	List<Training> findByTrainerId(Long trainerId);
}
