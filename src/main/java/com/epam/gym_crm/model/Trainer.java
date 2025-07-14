package com.epam.gym_crm.model;

import java.util.Objects;

import jakarta.validation.constraints.NotNull;

public class Trainer {
	@NotNull(message = "Specialization cannot be null")
	private TrainingType specialization;

	private User user;

	public Trainer() {
		this.user = new User();
	}

	public Trainer(Long id, String firstName, String lastName, String username, String password, boolean isActive,
			TrainingType specialization) {
		this.user = new User(id, firstName, lastName, username, password, isActive);
		this.specialization = specialization;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public TrainingType getSpecialization() {
		return specialization;
	}

	public void setSpecialization(TrainingType specialization) {
		this.specialization = specialization;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), specialization);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		if (!super.equals(obj))
			return false;
		Trainer trainer = (Trainer) obj;
		return specialization == trainer.specialization;
	}

	@Override
	public String toString() {
		return "Trainer{" + super.toString() + ", specialization="
				+ (specialization != null ? specialization.name() : "null") + '}';
	}

}
