package com.epam.gym_crm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.epam.gym_crm.model.TrainingType;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
	Optional<TrainingType> findByTrainingTypeNameIgnoreCase(String trainingTypeName);
}
