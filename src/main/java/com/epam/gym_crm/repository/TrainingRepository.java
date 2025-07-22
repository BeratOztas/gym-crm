package com.epam.gym_crm.repository;

import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.dto.response.TrainingResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("""
            SELECT NEW com.epam.gym_crm.dto.response.TrainingResponse(
                t.trainingName,
                t.trainingDate,
                t.trainingDuration,
                tt.trainingTypeName,
                tr.user.username,
                tr.user.firstName,
                tr.user.lastName,
                ts.user.username,
                ts.user.firstName,
                ts.user.lastName
            )
            FROM Training t
            JOIN t.trainee ts
            JOIN t.trainer tr
            JOIN t.trainingType tt
            WHERE ts.user.username = :traineeUsername
              AND (t.trainingDate >= COALESCE(:fromDate, t.trainingDate))
              AND (t.trainingDate <= COALESCE(:toDate, t.trainingDate))
              AND (
                :trainerName IS NULL OR
                LOWER(tr.user.firstName) LIKE LOWER(CONCAT('%', :trainerName, '%')) OR
                LOWER(tr.user.lastName) LIKE LOWER(CONCAT('%', :trainerName, '%'))
              )
              AND (
                :trainingTypeName IS NULL OR
                LOWER(tt.trainingTypeName) LIKE LOWER(CONCAT('%', :trainingTypeName, '%'))
              )
        """)
    List<TrainingResponse> findTraineeTrainingsByCriteria(
        @Param("traineeUsername") String traineeUsername,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("trainerName") String trainerName,
        @Param("trainingTypeName") String trainingTypeName
    );

    @Query("""
            SELECT NEW com.epam.gym_crm.dto.response.TrainingResponse(
                t.trainingName,
                t.trainingDate,
                t.trainingDuration,
                tt.trainingTypeName,
                tr.user.username,
                tr.user.firstName,
                tr.user.lastName,
                ts.user.username,
                ts.user.firstName,
                ts.user.lastName
            )
            FROM Training t
            JOIN t.trainee ts
            JOIN t.trainer tr
            JOIN t.trainingType tt
            WHERE tr.user.username = :trainerUsername
              AND (t.trainingDate >= COALESCE(:fromDate, t.trainingDate))
              AND (t.trainingDate <= COALESCE(:toDate, t.trainingDate))
              AND (
                :traineeName IS NULL OR
                LOWER(ts.user.firstName) LIKE LOWER(CONCAT('%', :traineeName, '%')) OR
                LOWER(ts.user.lastName) LIKE LOWER(CONCAT('%', :traineeName, '%'))
              )
              AND (
                :trainingTypeName IS NULL OR
                LOWER(tt.trainingTypeName) LIKE LOWER(CONCAT('%', :trainingTypeName, '%'))
              )
        """)
    List<TrainingResponse> findTrainerTrainingsByCriteria(
        @Param("trainerUsername") String trainerUsername,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("traineeName") String traineeName,
        @Param("trainingTypeName") String trainingTypeName
    );
}