package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym_crm.config.JwtConfig;
import com.epam.gym_crm.domain.service.impl.JwtTokenBlacklistService;

@ExtendWith(MockitoExtension.class)
class JwtTokenBlacklistServiceTest {

	@Mock
	private JwtConfig jwtConfig;

	private JwtTokenBlacklistService jwtTokenBlacklistService;

	@BeforeEach
	void setUp() {
		when(jwtConfig.getBlacklistRetentionHours()).thenReturn(2L);

		jwtTokenBlacklistService = new JwtTokenBlacklistService(jwtConfig);
	}

	// ----- (Success Scenarios) -----

	@Test
	void shouldReturnTrueWhenTokenIsBlacklisted() {

		String token = "dummy-blacklisted-token";
		jwtTokenBlacklistService.blacklistToken(token);

		boolean isBlacklisted = jwtTokenBlacklistService.isBlacklisted(token);

		assertTrue(isBlacklisted, "Token should be found in the blacklist.");
	}

	@Test
	void shouldReturnFalseWhenTokenIsNotBlacklisted() {
		String token = "dummy-clean-token";

		boolean isBlacklisted = jwtTokenBlacklistService.isBlacklisted(token);

		assertFalse(isBlacklisted, "Token should not be found in the blacklist.");
	}

	@Test
	void shouldSuccessfullyAddValidTokenToBlacklist() {
		String token = "valid-token-to-be-blacklisted";

		assertDoesNotThrow(() -> {
			jwtTokenBlacklistService.blacklistToken(token);
		});

		assertTrue(jwtTokenBlacklistService.isBlacklisted(token));
	}

	// ----- (Failure Scenarios) -----

	@Test
	void shouldReturnFalseForDifferentToken() {
		String blacklistedToken = "i-am-blacklisted";

		String cleanToken = "i-am-clean";
		jwtTokenBlacklistService.blacklistToken(blacklistedToken);

		boolean isCleanTokenBlacklisted = jwtTokenBlacklistService.isBlacklisted(cleanToken);

		assertFalse(isCleanTokenBlacklisted, "A different, non-blacklisted token should not be found.");
	}

	@Test
	void shouldHandleNullTokenGracefully() {
		String nullToken = null;

		boolean isBlacklisted = jwtTokenBlacklistService.isBlacklisted(nullToken);

		assertFalse(isBlacklisted, "isBlacklisted should return false for a null token.");

		assertDoesNotThrow(() -> {
			jwtTokenBlacklistService.blacklistToken(nullToken);
		});
	}

	@Test
	void shouldHandleEmptyTokenGracefully() {
		String emptyToken = "";

		boolean isBlacklisted = jwtTokenBlacklistService.isBlacklisted(emptyToken);

		assertFalse(isBlacklisted, "isBlacklisted should return false for an empty token.");

		assertDoesNotThrow(() -> {
			jwtTokenBlacklistService.blacklistToken(emptyToken);
		});
	}
}
