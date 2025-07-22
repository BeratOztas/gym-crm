package com.epam.gym_crm.dto.response;

import java.time.LocalDate;

import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraineeResponse {

	private LocalDate dateOfBirth;
	private String address;
	private String firstName;
	private String lastName;
	private String username;
	private boolean isActive;

	public TraineeResponse(Trainee trainee) {
		if (trainee != null) {
			User user = trainee.getUser();
			if (user != null) {
				this.firstName = user.getFirstName();
				this.lastName = user.getLastName();
				this.username = user.getUsername();
				this.isActive = user.isActive();
			}
			this.dateOfBirth = trainee.getDateOfBirth();
			this.address = trainee.getAddress();
		}
	}
}
