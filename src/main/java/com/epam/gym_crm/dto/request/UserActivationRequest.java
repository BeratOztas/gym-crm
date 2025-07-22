package com.epam.gym_crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivationRequest {

	@NotBlank(message = "Username cannot be blank")
	private String username;
	
	@NotNull(message = "Activation status (isActive) cannot be null")
	private boolean isActive;
}
