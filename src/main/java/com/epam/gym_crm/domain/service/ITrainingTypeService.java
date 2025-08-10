package com.epam.gym_crm.domain.service;

import java.util.List;

import com.epam.gym_crm.db.entity.TrainingType;

public interface ITrainingTypeService {
	List<TrainingType> getTrainingTypes();
}
