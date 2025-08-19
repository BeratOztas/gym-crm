package com.epam.gym_crm.domain.service;

import com.epam.gym_crm.api.dto.UserCreationResult;
import com.epam.gym_crm.api.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.api.dto.request.LoginRequest;
import com.epam.gym_crm.api.dto.response.LoginResponse;
import com.epam.gym_crm.db.entity.User;

public interface IAuthenticationService {

	LoginResponse login(LoginRequest request);

	void changePassword(ChangePasswordRequest request);

	void logout(String token);
	
	UserCreationResult prepareUserWithCredentials(String firstName, String lastName);
	
	String createAccessToken(User user);

}
