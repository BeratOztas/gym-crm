package com.epam.gym_crm.api.dto.request.trainer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerRequest {

	@NotBlank(message = "Trainer username can not be blank")
	private String username;
}
