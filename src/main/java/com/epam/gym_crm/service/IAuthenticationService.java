package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.dto.request.LoginRequest;
import com.epam.gym_crm.model.User;

public interface IAuthenticationService {

	void login(LoginRequest request);

	void logout();

	User createAndSaveUser(String firstName, String lastName);

	void changePassword(ChangePasswordRequest request);
}
