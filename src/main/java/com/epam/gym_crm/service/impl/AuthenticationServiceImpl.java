package com.epam.gym_crm.service.impl;

import java.security.SecureRandom;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.epam.gym_crm.auth.AuthManager;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.dto.request.LoginRequest;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.monitoring.metric.AppMetrics;
import com.epam.gym_crm.service.IAuthenticationService;

import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class AuthenticationServiceImpl implements IAuthenticationService {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

	private static final int PASSWORD_LENGTH = 10;
	private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private final SecureRandom random = new SecureRandom();

	private final UserRepository userRepository;
	private final AuthManager authManager;
	private final AppMetrics appMetrics;

	public AuthenticationServiceImpl(UserRepository userRepository, AuthManager authManager,AppMetrics appMetrics) {
		this.userRepository = userRepository;
		this.authManager = authManager;
		this.appMetrics = appMetrics;
	}

	@Override
	@Timed(value = "gym_crm_api_duration_seconds", extraTags = { "endpoint", "login" })
	public void login(LoginRequest request) {
		if (request == null) {
			logger.error("Login request DTO cannot be null.");
			throw new BaseException(
					new ErrorMessage(MessageType.INVALID_ARGUMENT, "Login request DTO cannot be null."));
		}
		String username = request.getUsername();
		String password = request.getPassword();

		User user = userRepository.findByUsername(username).orElseThrow(
				() -> new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid username or password.")));

		if (!user.isActive()) {
			throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED, "User account is inactive."));
		}

		if (!user.getPassword().equals(password)) {
			throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid username or password."));
		}

		try {
			//Login Success
			HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
					.currentRequestAttributes()).getRequest();
			HttpSession session = httpServletRequest.getSession(true);
			session.setAttribute("currentUser", user);
			logger.info("User logged in successfully: {}", username);

			appMetrics.incrementLoginSuccess();
		} catch (BaseException e) {
			appMetrics.incrementLoginFailure();
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Invalid username or password."));
		}catch (IllegalStateException e) {
			logger.error(
					"Failed to access HttpServletRequest. This method should be called within a web request context.",
					e);
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Request context not found."));
		}

	}

	@Override
	public void logout() {
		String username = authManager.getCurrentUser().getUsername();
		authManager.logout();
		logger.info("User logged out: {}", username);
	}

	@Override
	public User createAndSaveUser(String firstName, String lastName) {
		String baseUsername = firstName.trim() + "." + lastName.trim();
		String username = generateUniqueUsername(baseUsername);
		String password = generateRandomPassword();

		User newUser = new User();
		newUser.setFirstName(firstName.trim());
		newUser.setLastName(lastName.trim());
		newUser.setUsername(username);
		newUser.setPassword(password);

		return newUser;
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
		User currentUser = authManager.getCurrentUser();

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

		if (!currentUser.getUsername().equals(username)) {
			logger.error("Unauthorized attempt to change password for user '{}' by current user '{}'.", username,
					currentUser.getUsername());
			throw new BaseException(
					new ErrorMessage(MessageType.FORBIDDEN, "You are not authorized to change this user's password."));
		}

		User user = optUser.get();

		if (!user.getPassword().equals(oldPassword)) {
			logger.warn("Password change failed: Invalid old password for username: {}", username);
			throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid old password."));
		}

		if (oldPassword.equals(newPassword)) {
			logger.warn("Password change failed: New password cannot be the same as the old password for username: {}",
					username);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT,
					"New password cannot be the same as the old password."));
		}

		user.setPassword(newPassword);

		userRepository.save(user);

		logger.info("Password changed successfully for user: {}", username);

	}

}
