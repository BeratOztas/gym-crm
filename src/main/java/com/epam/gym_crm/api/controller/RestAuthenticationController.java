package com.epam.gym_crm.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym_crm.api.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.api.dto.request.LoginRequest;
import com.epam.gym_crm.api.dto.response.LoginResponse;
import com.epam.gym_crm.domain.service.IAuthenticationService;
import com.epam.gym_crm.security.JwtTokenExtractor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "Authentication Management", description = "Endpoints for user authentication and session management.")
@RestController
@RequestMapping("/api/auth")
public class RestAuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationController.class);

	private final IAuthenticationService authenticationService;
	private final JwtTokenExtractor jwtTokenExtractor;

	public RestAuthenticationController(IAuthenticationService authenticationService,
			JwtTokenExtractor jwtTokenExtractor) {
		this.authenticationService = authenticationService;
		this.jwtTokenExtractor = jwtTokenExtractor;
	}

	@Operation(summary = "Login a user", description = "Authenticates a user with a username and password to start a session. Requires basic authentication headers.")
	@ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = String.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content(schema = @Schema(implementation = String.class)))
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {

		logger.info("Login request received for user: {}", loginRequest.getUsername());

		return ResponseEntity.ok(authenticationService.login(loginRequest));

	}

	@Operation(summary = "Change a user's password", description = "Allows a user to change their password by providing their old and new passwords.")
	@ApiResponse(responseCode = "200", description = "Password changed successfully", content = @Content(schema = @Schema(implementation = String.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid old password", content = @Content(schema = @Schema(implementation = String.class)))
	@PutMapping("/change-password")
	public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
		authenticationService.changePassword(changePasswordRequest);
		logger.info("Password changed successfully for user: {}", changePasswordRequest.getUsername());

		return ResponseEntity.ok("Password changed successfully for user: " + changePasswordRequest.getUsername());
	}

	@Operation(summary = "Logout a user", description = "Ends the current user's session.")
	@ApiResponse(responseCode = "200", description = "Logout successful", content = @Content(schema = @Schema(implementation = String.class)))
	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request) {
		String token = jwtTokenExtractor.extractJwtFromRequest(request);
		authenticationService.logout(token);

		logger.info("Logout successful.");
		return ResponseEntity.ok("Logout successful");
	}
}
