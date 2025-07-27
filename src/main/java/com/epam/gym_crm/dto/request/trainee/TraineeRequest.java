package com.epam.gym_crm.dto.request.trainee;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TraineeRequest {

	@NotBlank(message = "Trainee username can not be blank")
	private String username;
}
