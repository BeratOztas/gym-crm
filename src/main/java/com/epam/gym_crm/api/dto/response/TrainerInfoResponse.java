package com.epam.gym_crm.api.dto.response;

import com.epam.gym_crm.db.entity.Trainer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerInfoResponse {

	private String username;
	private String firstName;
	private String lastName;
	private String specialization;

	public TrainerInfoResponse(Trainer trainer) {
		if (trainer != null) {
			if (trainer.getUser() != null) {
				this.username = trainer.getUser().getUsername();
				this.firstName = trainer.getUser().getFirstName();
				this.lastName = trainer.getUser().getLastName();
			}
			if (trainer.getSpecialization() != null) {
				this.specialization = trainer.getSpecialization().getTrainingTypeName();
			}
		}
	}
}
