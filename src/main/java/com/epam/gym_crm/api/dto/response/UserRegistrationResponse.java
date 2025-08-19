package com.epam.gym_crm.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponse {

	private String username;
	private String password;
	private String accessToken;
}
