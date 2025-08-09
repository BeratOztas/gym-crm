package com.epam.gym_crm.db.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.epam.gym_crm.db.entity.Training;
import com.epam.gym_crm.dto.response.TraineeTrainingInfoProjection;
import com.epam.gym_crm.dto.response.TrainerTrainingInfoProjection;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

  
	@Query(value = """
            SELECT
                t.training_name AS trainingName,
                t.training_date AS trainingDate,
                tt.training_type_name AS trainingType,
                t.training_duration AS trainingDuration,
                (u_trainer.first_name || ' ' || u_trainer.last_name) AS trainerName
            FROM training t
            JOIN trainee ts ON ts.id = t.trainee_id
            JOIN "user" u_trainee ON u_trainee.id = ts.user_id
            JOIN trainer tr ON tr.id = t.trainer_id
            JOIN "user" u_trainer ON u_trainer.id = tr.user_id
            JOIN training_type tt ON tt.id = t.training_type_id
            WHERE u_trainee.username = :traineeUsername
              AND (:fromDate IS NULL OR t.training_date >= :fromDate)
              AND (:toDate IS NULL OR t.training_date <= :toDate)
              AND (
                CAST(:trainerName AS text) IS NULL OR
                LOWER(u_trainer.first_name) LIKE LOWER(CONCAT('%', CAST(:trainerName AS text), '%')) OR
                LOWER(u_trainer.last_name) LIKE LOWER(CONCAT('%', CAST(:trainerName AS text), '%'))
              )
              AND (
                CAST(:trainingTypeName AS text) IS NULL OR
                LOWER(tt.training_type_name) LIKE LOWER(CONCAT('%', CAST(:trainingTypeName AS text), '%'))
              )
            ORDER BY t.training_date DESC
        """, nativeQuery = true)
    List<TraineeTrainingInfoProjection> findTraineeTrainingsByCriteria(
        @Param("traineeUsername") String traineeUsername,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("trainerName") String trainerName,
        @Param("trainingTypeName") String trainingTypeName
    );


	@Query(value = """
            SELECT
                t.training_name AS trainingName,
                t.training_date AS trainingDate,
                tt.training_type_name AS trainingType,
                t.training_duration AS trainingDuration,
                (u_trainee.first_name || ' ' || u_trainee.last_name) AS traineeName
            FROM training t
            JOIN trainer tr ON tr.id = t.trainer_id
            JOIN "user" u_trainer ON u_trainer.id = tr.user_id
            JOIN trainee ts ON ts.id = t.trainee_id
            JOIN "user" u_trainee ON u_trainee.id = ts.user_id
            JOIN training_type tt ON tt.id = t.training_type_id
            WHERE u_trainer.username = :trainerUsername
              AND (:fromDate IS NULL OR t.training_date >= :fromDate)
              AND (:toDate IS NULL OR t.training_date <= :toDate)
              AND (
                CAST(:traineeName AS text) IS NULL OR
                LOWER(u_trainee.first_name) LIKE LOWER(CONCAT('%', CAST(:traineeName AS text), '%')) OR
                LOWER(u_trainee.last_name) LIKE LOWER(CONCAT('%', CAST(:traineeName AS text), '%'))
              )
            ORDER BY t.training_date DESC
        """, nativeQuery = true)
    List<TrainerTrainingInfoProjection> findTrainerTrainingsByCriteria(
        @Param("trainerUsername") String trainerUsername,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("traineeName") String traineeName
    );
    
    @Modifying
    void deleteByTraineeId(@Param("traineeId") Long traineeId);
}
