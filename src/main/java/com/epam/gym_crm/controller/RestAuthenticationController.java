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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Authentication Management", description = "Endpoints for user authentication and session management.")
@RestController
@RequestMapping("/api/auth")
public class RestAuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationController.class);

	private final IAuthenticationService authenticationService;

	public RestAuthenticationController(IAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Operation(
		summary = "Login a user",
		description = "Authenticates a user with a username and password to start a session. Requires basic authentication headers."
	)
	@ApiResponse(responseCode = "200", description = "Login successful",
		content = @Content(schema = @Schema(implementation = String.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials",
		content = @Content(schema = @Schema(implementation = String.class)))
	@GetMapping("/login")
	public ResponseEntity<String> login(
			@Parameter(description = "The user's unique username", required = true)
			@RequestParam("username") String username,
			@Parameter(description = "The user's password", required = true)
			@RequestParam("password") String password) {
		logger.info("Login request received for user: {}", username);

		LoginRequest loginRequest = new LoginRequest(username, password);
		authenticationService.login(loginRequest);

		logger.info("Login successful for user: {}", username);
		return ResponseEntity.ok("Login successful for user: " + username);
	}

	@Operation(
		summary = "Change a user's password",
		description = "Allows a user to change their password by providing their old and new passwords."
	)
	@ApiResponse(responseCode = "200", description = "Password changed successfully",
		content = @Content(schema = @Schema(implementation = String.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid old password",
		content = @Content(schema = @Schema(implementation = String.class)))
	@PutMapping("/change-password")
	public ResponseEntity<String> changePassword(
			@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
		authenticationService.changePassword(changePasswordRequest);
		logger.info("Password changed successfully for user: {}", changePasswordRequest.getUsername());

		return ResponseEntity.ok("Password changed successfully for user: " + changePasswordRequest.getUsername());
	}

	@Operation(
		summary = "Logout a user",
		description = "Ends the current user's session."
	)
	@ApiResponse(responseCode = "200", description = "Logout successful",
		content = @Content(schema = @Schema(implementation = String.class)))
	@PostMapping("/logout")
	public ResponseEntity<String> logout() {
		authenticationService.logout();

		logger.info("Logout successful.");
		return ResponseEntity.ok("Logout successful");
	}
}
