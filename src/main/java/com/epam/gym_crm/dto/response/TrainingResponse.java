package com.epam.gym_crm.dto.response;

import java.time.LocalDate;

import com.epam.gym_crm.model.Training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingResponse {

	private String trainingName;
	private LocalDate trainingDate;
	private Integer trainingDuration;
	private String trainingTypeName;

	private String trainerUsername;
	private String trainerFirstName;
	private String trainerLastName;

	private String traineeUsername;
	private String traineeFirstName;
	private String traineeLastName;

	public TrainingResponse(Training training) {
		this.trainingName = training.getTrainingName();
		this.trainingDate = training.getTrainingDate();
		this.trainingDuration = training.getTrainingDuration();

		if (training.getTrainingType() != null) {
			this.trainingTypeName = training.getTrainingType().getTrainingTypeName();
		}

		if (training.getTrainer() != null && training.getTrainer().getUser() != null) {
			this.trainerUsername = training.getTrainer().getUser().getUsername();
			this.trainerFirstName = training.getTrainer().getUser().getFirstName();
			this.trainerLastName = training.getTrainer().getUser().getLastName();
		}

		if (training.getTrainee() != null && training.getTrainee().getUser() != null) {
			this.traineeUsername = training.getTrainee().getUser().getUsername();
			this.traineeFirstName = training.getTrainee().getUser().getFirstName();
			this.traineeLastName = training.getTrainee().getUser().getLastName();
		}
	}
}
