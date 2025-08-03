package com.epam.gym_crm.db.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.epam.gym_crm.db.entity.Training;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query(value = """
            SELECT
                t.training_name,
                t.training_date,
                tt.training_type_name,
                t.training_duration,
                (u_trainer.first_name || ' ' || u_trainer.last_name)
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
                :trainerName IS NULL OR
                LOWER(u_trainer.first_name) LIKE LOWER(CONCAT('%', :trainerName, '%')) OR
                LOWER(u_trainer.last_name) LIKE LOWER(CONCAT('%', :trainerName, '%'))
              )
              AND (
                :trainingTypeName IS NULL OR
                LOWER(tt.training_type_name) LIKE LOWER(CONCAT('%', :trainingTypeName, '%'))
              )
        """, nativeQuery = true)
    List<Object[]> findTraineeTrainingsByCriteria(
        @Param("traineeUsername") String traineeUsername,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("trainerName") String trainerName,
        @Param("trainingTypeName") String trainingTypeName
    );

    @Query(value = """
            SELECT
                t.training_name,
                t.training_date,
                tt.training_type_name,
                t.training_duration,
                (u_trainee.first_name || ' ' || u_trainee.last_name)
            FROM training t
            JOIN trainee ts ON ts.id = t.trainee_id
            JOIN "user" u_trainee ON u_trainee.id = ts.user_id
            JOIN trainer tr ON tr.id = t.trainer_id
            JOIN "user" u_trainer ON u_trainer.id = tr.user_id
            JOIN training_type tt ON tt.id = t.training_type_id
            WHERE u_trainer.username = :trainerUsername
              AND (:fromDate IS NULL OR t.training_date >= :fromDate)
              AND (:toDate IS NULL OR t.training_date <= :toDate)
              AND (
                :traineeName IS NULL OR
                LOWER(u_trainee.first_name) LIKE LOWER(CONCAT('%', :traineeName, '%')) OR
                LOWER(u_trainee.last_name) LIKE LOWER(CONCAT('%', :traineeName, '%'))
              )
        """, nativeQuery = true)
    List<Object[]> findTrainerTrainingsByCriteria(
        @Param("trainerUsername") String trainerUsername,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("traineeName") String traineeName
    );
    
    @Modifying 
    @Query("DELETE FROM Training t WHERE t.trainee.id = :traineeId")
    void deleteByTraineeId(@Param("traineeId") Long traineeId);
}