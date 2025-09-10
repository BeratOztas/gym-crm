package com.epam.gym_crm.client.model;

import java.time.LocalDate;

public record TrainerWorkloadRequest(String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        Boolean isActive,
        LocalDate trainingDate,
        long trainingDuration,
        ActionType actionType) {

}
