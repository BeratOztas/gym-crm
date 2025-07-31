package com.epam.gym_crm.controller;

import org.springframework.http.ResponseEntity;

import com.epam.gym_crm.dto.request.ChangePasswordRequest;

public interface IRestAuthenticationController {

	ResponseEntity<ApiResponse<String>> login(String username, String password);

	ResponseEntity<ApiResponse<String>> changePassword(ChangePasswordRequest request);

	ResponseEntity<ApiResponse<String>> logout();
}
