package com.epam.gym_crm.model;

import java.time.LocalDate;
import java.util.Objects;

public class Training {

	private Long id;
	private String trainingName;
	private TrainingType trainingType;
	private LocalDate trainingDate;
	private Integer trainingDuration;
	
	private Trainee trainee;
	private Trainer trainer;
	
	public Training() {
	}
	
	public Training(Long id, String trainingName, TrainingType trainingType, LocalDate trainingDate, Integer duration,
			Trainee trainee, Trainer trainer) {
		this.id = id;
		this.trainingName = trainingName;
		this.trainingType = trainingType;
		this.trainingDate = trainingDate;
		this.trainingDuration = duration;
		this.trainee = trainee;
		this.trainer = trainer;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTrainingName() {
		return trainingName;
	}

	public void setTrainingName(String trainingName) {
		this.trainingName = trainingName;
	}

	public TrainingType getTrainingType() {
		return trainingType;
	}

	public void setTrainingType(TrainingType trainingType) {
		this.trainingType = trainingType;
	}

	public LocalDate getTrainingDate() {
		return trainingDate;
	}

	public void setTrainingDate(LocalDate trainingDate) {
		this.trainingDate = trainingDate;
	}


	public Integer getTrainingDuration() {
		return trainingDuration;
	}

	public void setTrainingDuration(Integer trainingDuration) {
		this.trainingDuration = trainingDuration;
	}

	public Trainee getTrainee() {
		return trainee;
	}

	public void setTrainee(Trainee trainee) {
		this.trainee = trainee;
	}

	public Trainer getTrainer() {
		return trainer;
	}

	public void setTrainer(Trainer trainer) {
		this.trainer = trainer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		 if (this == obj) return true;
	        if (obj == null || getClass() != obj.getClass()) return false;
	        Training training = (Training) obj;
	        return Objects.equals(id, training.id);
	}

	@Override
	public String toString() {
		return "Training{" +
	               "id=" + id +
	               ", traineeId=" + (trainee != null ? trainee.getId() : "null") +
	               ", trainerId=" + (trainer != null ? trainer.getId() : "null") +
	               ", trainingName='" + trainingName + '\'' +
	               ", trainingType=" + trainingType.name() + 
	               ", trainingDate=" + trainingDate +
	               ", trainingDuration=" + trainingDuration +
	               '}';
	}
	
	
	
}
