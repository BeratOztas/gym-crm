package com.epam.gym_crm.domain.service;

import com.epam.gym_crm.api.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.api.dto.request.LoginRequest;
import com.epam.gym_crm.db.entity.User;

public interface IAuthenticationService {

	void login(LoginRequest request);

	void changePassword(ChangePasswordRequest request);

	void logout();

	User createAndSaveUser(String firstName, String lastName);

}
