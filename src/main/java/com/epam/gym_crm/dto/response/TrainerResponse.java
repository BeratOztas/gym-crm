package com.epam.gym_crm.dto.response;

import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.TrainingType;
import com.epam.gym_crm.model.User;

import lombok.Data;

@Data
public class TrainerResponse {

    private String firstName;
    private String lastName;
    private String username;
    private boolean isActive;
    private String specializationName;

    public TrainerResponse(Trainer trainer) {
        if (trainer != null) {
            User user = trainer.getUser();
            if (user != null) {
                this.firstName = user.getFirstName();
                this.lastName = user.getLastName();
                this.username = user.getUsername();
                this.isActive = user.isActive();
            }
            TrainingType specialization = trainer.getSpecialization();
            if (specialization != null) {
                this.specializationName = specialization.getTrainingTypeName();
            }
        }
    }
}