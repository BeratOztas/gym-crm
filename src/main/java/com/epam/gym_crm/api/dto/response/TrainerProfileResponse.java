package com.epam.gym_crm.api.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import com.epam.gym_crm.db.entity.Trainer;
import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.db.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerProfileResponse {

	private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private Boolean isActive;
    private List<TraineeInfoResponse> traineesList;

    public TrainerProfileResponse(Trainer trainer) {
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
                this.specialization = specialization.getTrainingTypeName();
            }
            if (trainer.getTrainees() != null) {
                this.traineesList = trainer.getTrainees().stream()
                                         .map(TraineeInfoResponse::new)
                                         .collect(Collectors.toList());
            }
        }
    }
}