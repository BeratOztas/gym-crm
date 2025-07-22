package com.epam.gym_crm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.epam.gym_crm.model.Trainer;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
	Optional<Trainer> findByUserUsername(String username);
	
	@Query("""
	        SELECT tr FROM Trainer tr
	        WHERE tr.user.isActive = true AND
	        tr.id NOT IN (
	            SELECT t.id FROM Trainee tra
	            JOIN tra.trainers t
	            WHERE tra.user.username = :traineeUsername
	        )
	    """)
	    List<Trainer> findActiveTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);
}
