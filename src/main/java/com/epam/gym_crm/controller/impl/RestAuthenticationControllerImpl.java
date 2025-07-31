package com.epam.gym_crm.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym_crm.controller.ApiResponse;
import com.epam.gym_crm.controller.IRestAuthenticationController;
import com.epam.gym_crm.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.dto.request.LoginRequest;
import com.epam.gym_crm.service.IAuthenticationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class RestAuthenticationControllerImpl implements IRestAuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationControllerImpl.class);

	private final IAuthenticationService authenticationService;

	public RestAuthenticationControllerImpl(IAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	@GetMapping("/login")
	public ResponseEntity<ApiResponse<String>> login(@RequestParam("username") String username, 
		    @RequestParam("password") String password) {
		logger.info("Login request received for user: {}", username);
	    
	    LoginRequest loginRequest = new LoginRequest(username, password);
	    
	    authenticationService.login(loginRequest);
	    
	    logger.info("Login successful for user: {}", username);
	    return ResponseEntity.ok(ApiResponse.ok("Login successful."));
	}

	@Override
	@PutMapping("/change-password")
	public ResponseEntity<ApiResponse<String>> changePassword(

			@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {

		authenticationService.changePassword(changePasswordRequest);
		logger.info("Password changed successfully for user: {}", changePasswordRequest.getUsername());

		return ResponseEntity.ok(ApiResponse.ok("Password changed successfully"));
	}

	@Override
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout() {

		authenticationService.logout();

		logger.info("Logout successful.");
		return ResponseEntity.ok(ApiResponse.ok("Logout successful"));
	}
}