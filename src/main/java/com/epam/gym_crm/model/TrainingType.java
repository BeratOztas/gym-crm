package com.epam.gym_crm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "training_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrainingType {

	@Id
	private Long id;

	@NotBlank(message = "Training type name cannot be blank")
	@Size(min = 3, max = 50, message = "Training type name must be between 3 and 50 characters")
	@Column(name = "training_type_name", nullable = false, unique = true)
	private String trainingTypeName;

}
