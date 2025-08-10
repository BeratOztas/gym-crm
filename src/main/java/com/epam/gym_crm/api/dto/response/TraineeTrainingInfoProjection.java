package com.epam.gym_crm.api.dto.response;

import java.time.LocalDate;

public interface TraineeTrainingInfoProjection {
    String getTrainingName();
    LocalDate getTrainingDate();
    String getTrainingType();
    Integer getTrainingDuration();
    String getTrainerName();
}
