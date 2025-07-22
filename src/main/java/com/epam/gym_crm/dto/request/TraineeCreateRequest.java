package com.epam.gym_crm.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TraineeCreateRequest {
	
	@NotBlank(message = "First name cannot be blank") 
	@Size(min = 3, max = 50, message = "First name must be between 2 and 50 characters")
	private String firstName;

	@NotBlank(message = "Last name cannot be blank") 
	@Size(min = 3, max = 50, message = "Last name must be between 2 and 50 characters")
	private String lastName;

	@Past(message = "Date of birth must be in the past")
	private LocalDate dateOfBirth;
	
	private String address;
	
	@NotNull(message = "Active status cannot be null")
	private boolean isActive;
}