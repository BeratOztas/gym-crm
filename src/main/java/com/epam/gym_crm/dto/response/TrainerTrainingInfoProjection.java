package com.epam.gym_crm.dto.response;

import java.time.LocalDate;

public interface TrainerTrainingInfoProjection {
    
    String getTrainingName();
    
    LocalDate getTrainingDate();
    
    String getTrainingType();
    
    Integer getTrainingDuration();
    
    String getTraineeName();

}
