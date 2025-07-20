package com.epam.gym_crm.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "training")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Training {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotBlank(message = "Training name cannot be blank")
	@Size(min = 3, max = 100, message = "Training name must be between 3 and 100 characters")
	@Column(name = "training_name", nullable = false)
	private String trainingName;

	@NotNull(message = "Training date cannot be null")
	@Column(name = "training_date", nullable = false)
	private LocalDate trainingDate;

	@NotNull(message = "Training duration cannot be null")
	@Min(value = 1, message = "Training duration must be at least 1 minute")
	@Column(name = "training_duration", nullable = false)
	private Integer trainingDuration;

	@ManyToOne(optional = false)
	@JoinColumn(name = "trainee_id", nullable = false)
	@NotNull(message = "Trainee cannot be null for a training")
	private Trainee trainee;

	@ManyToOne(optional = true)
	@JoinColumn(name = "trainer_id", nullable = true)
//    @NotNull(message = "Trainer cannot be null for a training")
	private Trainer trainer;

	@ManyToOne(optional = false)
	@JoinColumn(name = "training_type_id", nullable = false)
	private TrainingType trainingType;

}
