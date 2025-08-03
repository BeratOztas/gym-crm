package com.epam.gym_crm.controller;

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

import com.epam.gym_crm.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.dto.request.LoginRequest;
import com.epam.gym_crm.service.IAuthenticationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class RestAuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationController.class);

	private final IAuthenticationService authenticationService;

	public RestAuthenticationController(IAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@GetMapping("/login")
	public ResponseEntity<String> login(@RequestParam("username") String username,
			@RequestParam("password") String password) {
		logger.info("Login request received for user: {}", username);

		LoginRequest loginRequest = new LoginRequest(username, password);

		authenticationService.login(loginRequest);

		logger.info("Login successful for user: {}", username);
		return ResponseEntity.ok("Login successful for user: "+ username);
	}

	@PutMapping("/change-password")
	public ResponseEntity<String> changePassword(

			@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {

		authenticationService.changePassword(changePasswordRequest);
		logger.info("Password changed successfully for user: {}", changePasswordRequest.getUsername());

		return ResponseEntity.ok("Password changed successfully for user: "+ changePasswordRequest.getUsername());
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout() {

		authenticationService.logout();

		logger.info("Logout successful.");
		return ResponseEntity.ok("Logout successful");
	}
}