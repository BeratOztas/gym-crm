package com.epam.gym_crm.db.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epam.gym_crm.db.entity.TrainingType;

@Repository
public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
	Optional<TrainingType> findByTrainingTypeNameIgnoreCase(String trainingTypeName);
}
