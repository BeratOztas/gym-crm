package com.epam.gym_crm.domain.service.impl;

import com.epam.gym_crm.config.LoginSecurityConfig;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

	private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

	private final LoadingCache<String, Integer> attemptsCache;
	private final LoginSecurityConfig loginSecurityConfig;

	public LoginAttemptService(LoginSecurityConfig loginSecurityConfig) {
		this.loginSecurityConfig = loginSecurityConfig;

		// Kilitlenme süresi dolduğunda, Guava Cache kaydı otomatik olarak
		// sileceği için kilit de otomatik olarak kalkar.
		this.attemptsCache = CacheBuilder.newBuilder()
				.expireAfterWrite(loginSecurityConfig.getLockoutDurationMinutes(), TimeUnit.MINUTES)
				.build(new CacheLoader<String, Integer>() {
					public Integer load(String key) {
						return 0; // İlk denemede counter 0
					}
				});
		logger.info("LoginAttemptService initialized. Users will be locked for {} minutes after {} failed attempts.",
				loginSecurityConfig.getLockoutDurationMinutes(), loginSecurityConfig.getMaxAttempts());

	}

	public void loginSucceeded(String username) {
		if (attemptsCache.getIfPresent(username) != null) {
			logger.info("Successful login for user '{}'. Resetting failed login attempt counter.", username);
			attemptsCache.invalidate(username);
		}
	}

	public void loginFailed(String username) {
		int attempts = 0;
		try {
			attempts = attemptsCache.get(username);
		} catch (ExecutionException e) {
			logger.error("Error retrieving attempts for user '{}' from cache.", username, e);
			attempts = 0;
		}
		attempts++;
		attemptsCache.put(username, attempts);

		int maxAttempts = loginSecurityConfig.getMaxAttempts();
		if (attempts >= maxAttempts) {
			logger.warn("User '{}' has been locked out. Failed attempts: {}/{}.", username, attempts, maxAttempts);
		} else {
			logger.warn("Failed login attempt for user '{}'. Attempt {} of {}.", username, attempts, maxAttempts);
		}
	}

	public boolean isBlocked(String username) {
		try {
			boolean isUserBlocked = attemptsCache.get(username) >= loginSecurityConfig.getMaxAttempts();

			if (isUserBlocked) {
				logger.warn("Access attempt rejected for user '{}' because the account is currently locked.", username);
			}

			return isUserBlocked;
		} catch (ExecutionException e) {
			logger.error("Error checking block status for user '{}' from cache.", username, e);
			return false;
		}
	}
}
