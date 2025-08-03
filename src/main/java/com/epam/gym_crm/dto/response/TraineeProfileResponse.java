package com.epam.gym_crm.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.epam.gym_crm.db.entity.Trainee;
import com.epam.gym_crm.db.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraineeProfileResponse {

	private String username;
	private String firstName;
	private String lastName;
	private LocalDate dateOfBirth;
	private String address;
	private Boolean isActive;
	private List<TrainerInfoResponse> trainersList;

	public TraineeProfileResponse(Trainee trainee) {
        if (trainee != null) {
            User user = trainee.getUser();
            if (user != null) {
            	this.username = user.getUsername();
                this.firstName = user.getFirstName();
                this.lastName = user.getLastName();
                this.isActive = user.isActive();
            }
            this.dateOfBirth = trainee.getDateOfBirth();
            this.address = trainee.getAddress();
            if (trainee.getTrainers() != null) {
                this.trainersList = trainee.getTrainers().stream()
                                         .map(TrainerInfoResponse::new) 
                                         .collect(Collectors.toList());
            }
        }
    }
}
