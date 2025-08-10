package com.epam.gym_crm.api.dto.response;

import com.epam.gym_crm.db.entity.Trainee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraineeInfoResponse {
	private String username;
    private String firstName;
    private String lastName;

    public TraineeInfoResponse(Trainee trainee) {
        if (trainee != null) {
            if (trainee.getUser() != null) {
                this.username = trainee.getUser().getUsername();
                this.firstName = trainee.getUser().getFirstName();
                this.lastName = trainee.getUser().getLastName();
            }
        }
    }
}
