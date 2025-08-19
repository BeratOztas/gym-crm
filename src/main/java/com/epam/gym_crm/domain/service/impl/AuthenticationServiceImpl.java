package com.epam.gym_crm.domain.service.impl;

import java.security.SecureRandom;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.epam.gym_crm.api.dto.UserCreationResult;
import com.epam.gym_crm.api.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.api.dto.request.LoginRequest;
import com.epam.gym_crm.api.dto.response.LoginResponse;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.ErrorMessage;
import com.epam.gym_crm.domain.exception.MessageType;
import com.epam.gym_crm.domain.service.IAuthenticationService;
import com.epam.gym_crm.monitoring.metric.AppMetrics;
import com.epam.gym_crm.security.JwtTokenProvider;
import com.epam.gym_crm.security.JwtUserDetails;

import io.micrometer.core.annotation.Timed;

@Service
public class AuthenticationServiceImpl implements IAuthenticationService {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

	private static final int PASSWORD_LENGTH = 10;
	private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private final SecureRandom random = new SecureRandom();

	private final UserRepository userRepository;
	private final AuthenticationInfoService authenticationInfoService;
	private final LoginAttemptService loginAttemptService;
	private final JwtTokenBlacklistService jwtTokenBlacklistService;

	private final AuthenticationManager authenticationManager;

	private final JwtTokenProvider jwtTokenProvider;
	private final BCryptPasswordEncoder passwordEncoder;

	private final AppMetrics appMetrics;

	public AuthenticationServiceImpl(UserRepository userRepository, AuthenticationInfoService authenticationInfoService,
			LoginAttemptService loginAttemptService, JwtTokenBlacklistService jwtTokenBlacklistService,
			AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
			BCryptPasswordEncoder passwordEncoder, AppMetrics appMetrics) {
		this.userRepository = userRepository;
		this.authenticationInfoService = authenticationInfoService;
		this.loginAttemptService = loginAttemptService;
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtTokenBlacklistService = jwtTokenBlacklistService;
		this.passwordEncoder = passwordEncoder;
		this.appMetrics = appMetrics;
	}

	@Override
	@Timed(value = "gym_crm_api_duration_seconds", extraTags = { "endpoint", "login" })
	public LoginResponse login(LoginRequest request) {
		if (request == null) {
			logger.error("Login request DTO cannot be null.");
			throw new BaseException(
					new ErrorMessage(MessageType.INVALID_ARGUMENT, "Login request DTO cannot be null."));
		}

		String username = request.getUsername();

		if (loginAttemptService.isBlocked(username)) {
			logger.warn("Login failed for user '{}': Account is temporarily locked.", username);
			throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED,
					"User account is temporarily locked due to too many failed login attempts."));
		}

		try {

			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(username, request.getPassword()));

			loginAttemptService.loginSucceeded(username);

			appMetrics.incrementLoginSuccess(username);

			SecurityContextHolder.getContext().setAuthentication(authentication);

			JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
			String accessToken = jwtTokenProvider.generateJwtToken(userDetails);

			logger.info("User logged in successfully: {}", username);
			return new LoginResponse(userDetails.getUsername(), accessToken);

		} catch (AuthenticationException e) {
			logger.warn("Login failed for user '{}' with AuthenticationException: {}", username, e.getMessage());

			loginAttemptService.loginFailed(username);
			appMetrics.incrementLoginFailure(username);

			Throwable cause = e.getCause();

			if (cause instanceof BaseException) {
				throw (BaseException) cause;
			}

			if (e instanceof DisabledException) {
				throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED, "User account is inactive."));
			} else if (e instanceof BadCredentialsException) {
				throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid username or password."));
			} else {
				
				throw new BaseException(
						new ErrorMessage(MessageType.GENERAL_EXCEPTION, "An unexpected error occurred during login."));
			}
		} catch (Exception e) {
			logger.error("An unexpected error occurred during login for user '{}'.", username, e);
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "An unexpected error occurred."));
		}
	}

	@Override
	public void logout(String token) {
		String username = authenticationInfoService.getCurrentUsername();
		if (token != null) {
			jwtTokenBlacklistService.blacklistToken(token); // Add to blackList.
		}
		SecurityContextHolder.clearContext();
		logger.info("User logged out: {}. Security context cleared and token processed by blacklist service.", username);
	}

	@Override
	public UserCreationResult prepareUserWithCredentials(String firstName, String lastName) {
		String baseUsername = firstName.trim() + "." + lastName.trim();
		String username = generateUniqueUsername(baseUsername);
		String rawPassword = generateRandomPassword();

		User newUser = new User();
		newUser.setFirstName(firstName.trim());
		newUser.setLastName(lastName.trim());
		newUser.setUsername(username);
		newUser.setActive(true);

		newUser.setPassword(passwordEncoder.encode(rawPassword));

		return new UserCreationResult(newUser, rawPassword);
	}

	@Override
	public String createAccessToken(User user) {
		JwtUserDetails userDetails = JwtUserDetails.create(user, true);
		return jwtTokenProvider.generateJwtToken(userDetails);
	}

	private String generateUniqueUsername(String baseUsername) {
		String username = baseUsername;
		int counter = 1;

		while (userRepository.findByUsername(username).isPresent()) {
			username = baseUsername + counter;
			counter++;
		}
		logger.debug("Generated unique username: {}", username);
		return username;
	}

	private String generateRandomPassword() {
		StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
		for (int i = 0; i < PASSWORD_LENGTH; i++) {
			password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
		}
		logger.debug("Generated password of length {}", PASSWORD_LENGTH);
		return password.toString();
	}

	@Override
	public void changePassword(ChangePasswordRequest request) {
		String currentUsername = authenticationInfoService.getCurrentUsername();

		if (request == null) {
			logger.error("Change password request DTO cannot be null.");
			throw new BaseException(
					new ErrorMessage(MessageType.INVALID_ARGUMENT, "Change password request DTO cannot be null."));
		}

		String username = request.getUsername();
		String oldPassword = request.getOldPassword();
		String newPassword = request.getNewPassword();

		Optional<User> optUser = userRepository.findByUsername(username);
		if (optUser.isEmpty()) {
			logger.error("Password change failed: User with username '{}' not found.", username);
			throw new BaseException(
					new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid credentials or user not found."));
		}

		if (!currentUsername.equals(username)) {
			logger.error("Unauthorized attempt to change password for user '{}' by current user '{}'.", username,
					currentUsername);
			throw new BaseException(
					new ErrorMessage(MessageType.FORBIDDEN, "You are not authorized to change this user's password."));
		}

		User user = optUser.get();

		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			logger.warn("Password change failed: Invalid old password for username: {}", username);
			throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid old password."));
		}

		if (oldPassword.equals(newPassword)) {
			logger.warn("Password change failed: New password cannot be the same as the old password for username: {}",
					username);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"New password cannot be the same as the old password."));
		}

		user.setPassword(passwordEncoder.encode(newPassword));

		userRepository.save(user);

		logger.info("Password changed successfully for user: {}", username);

	}

}
