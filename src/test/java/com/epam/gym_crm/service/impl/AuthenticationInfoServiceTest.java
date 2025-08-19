package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.service.impl.AuthenticationInfoService;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class AuthenticationInfoServiceTest {

	@InjectMocks
	private AuthenticationInfoService authenticationInfoService;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	// --- Success Scenario ---

	@Test
	void shouldReturnCurrentUsernameWhenUserIsAuthenticated() {
		UserDetails userDetails = new User("test.user", "password", Collections.emptyList());

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);

		String currentUsername = authenticationInfoService.getCurrentUsername();

		assertNotNull(currentUsername);
		assertEquals("test.user", currentUsername, "Should return the username of the authenticated user.");
	}

	// --- Failure Scenario ---

	@Test
	void shouldThrowBaseExceptionWhenNoAuthenticationExists() {

		assertThrows(BaseException.class, () -> {
			authenticationInfoService.getCurrentUsername();
		}, "Should throw BaseException when security context is empty.");
	}

	@Test
	void shouldThrowBaseExceptionWhenAuthenticationIsNotAuthenticated() {

		Authentication authentication = new UsernamePasswordAuthenticationToken(null, null);
		authentication.setAuthenticated(false);

		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);

		assertThrows(BaseException.class, () -> {
			authenticationInfoService.getCurrentUsername();
		}, "Should throw BaseException when authentication is not authenticated.");
	}
}
