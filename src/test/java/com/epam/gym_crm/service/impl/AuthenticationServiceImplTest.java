package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.epam.gym_crm.api.dto.UserCreationResult;
import com.epam.gym_crm.api.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.api.dto.request.LoginRequest;
import com.epam.gym_crm.api.dto.response.LoginResponse;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.MessageType;
import com.epam.gym_crm.domain.service.impl.AuthenticationInfoService;
import com.epam.gym_crm.domain.service.impl.AuthenticationServiceImpl;
import com.epam.gym_crm.domain.service.impl.JwtTokenBlacklistService;
import com.epam.gym_crm.domain.service.impl.LoginAttemptService;
import com.epam.gym_crm.monitoring.metric.AppMetrics;
import com.epam.gym_crm.security.JwtTokenProvider;
import com.epam.gym_crm.security.JwtUserDetails;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private BCryptPasswordEncoder passwordEncoder;
	@Mock
	private LoginAttemptService loginAttemptService;
	@Mock
	private JwtTokenBlacklistService jwtTokenBlacklistService;
	@Mock
	private AuthenticationInfoService authenticationInfoService;
	@Mock
	private AppMetrics appMetrics;

	@InjectMocks
	private AuthenticationServiceImpl authenticationService;

	@Test
	void shouldLoginSuccessfullyAndReturnToken() {

		LoginRequest request = new LoginRequest("test.user", "correctPass");
		User user = new User(1L, "Test", "User", "test.user", "encodedPass", true, null, null);
		JwtUserDetails userDetails = JwtUserDetails.create(user, true);
		Authentication authentication = mock(Authentication.class);

		when(loginAttemptService.isBlocked("test.user")).thenReturn(false);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(jwtTokenProvider.generateJwtToken(userDetails)).thenReturn("dummy.jwt.token");

		LoginResponse response = authenticationService.login(request);

		assertNotNull(response);
		assertEquals("test.user", response.username());
		assertEquals("dummy.jwt.token", response.accessToken());

		verify(loginAttemptService, times(1)).loginSucceeded("test.user");
		verify(appMetrics, times(1)).incrementLoginSuccess("test.user");
	}

	@Test
	void shouldThrowExceptionWhenUserIsBlocked() {
		LoginRequest request = new LoginRequest("blocked.user", "anyPass");
		when(loginAttemptService.isBlocked("blocked.user")).thenReturn(true);

		BaseException exception = assertThrows(BaseException.class, () -> authenticationService.login(request));
		assertEquals(MessageType.UNAUTHORIZED, exception.getErrorMessage().getMessageType());
		assertTrue(exception.getMessage().contains("User account is temporarily locked"));
		verify(authenticationManager, never()).authenticate(any());
	}

	@Test
	void shouldThrowExceptionForBadCredentials() {
		LoginRequest request = new LoginRequest("test.user", "wrongPass");
		when(loginAttemptService.isBlocked("test.user")).thenReturn(false);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		BaseException exception = assertThrows(BaseException.class, () -> authenticationService.login(request));
		assertEquals(MessageType.UNAUTHORIZED, exception.getErrorMessage().getMessageType());
		assertTrue(exception.getMessage().contains("Invalid username or password"));
		verify(loginAttemptService, times(1)).loginFailed("test.user");
		verify(appMetrics, times(1)).incrementLoginFailure("test.user");
	}

	@Test
	void shouldThrowExceptionForDisabledUser() {
		LoginRequest request = new LoginRequest("disabled.user", "anyPass");
		when(loginAttemptService.isBlocked("disabled.user")).thenReturn(false);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new DisabledException("User is disabled"));

		BaseException exception = assertThrows(BaseException.class, () -> authenticationService.login(request));
		assertEquals(MessageType.UNAUTHORIZED, exception.getErrorMessage().getMessageType());
		assertTrue(exception.getMessage().contains("User account is inactive"));
		verify(loginAttemptService, times(1)).loginFailed("disabled.user");
	}

	// --- prepareUserWithCredentials Tests ---
	@Test
	void shouldPrepareUserWithEncodedPassword() {

		String firstName = "New";
		String lastName = "User";
		when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

		UserCreationResult result = authenticationService.prepareUserWithCredentials(firstName, lastName);

		assertNotNull(result);
		assertNotNull(result.userToPersist());
		assertNotNull(result.rawPassword());
		assertEquals("encodedPassword", result.userToPersist().getPassword());
	}

	// --- Change Passwords Tests ---

	@Test
	void shouldChangePasswordSuccessfully() {
		ChangePasswordRequest request = new ChangePasswordRequest("test.user", "oldPass", "newPass");
		User user = new User(1L, "Test", "User", "test.user", "encodedOldPass", true, null, null);

		when(authenticationInfoService.getCurrentUsername()).thenReturn("test.user");
		when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
		when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

		assertDoesNotThrow(() -> authenticationService.changePassword(request));

		verify(userRepository, times(1)).save(user);
		assertEquals("encodedNewPass", user.getPassword());
	}

	@Test
	void shouldThrowExceptionWhenChangingPasswordWithInvalidOldPassword() {

		ChangePasswordRequest request = new ChangePasswordRequest("test.user", "wrongOldPass", "newPass");
		User user = new User(1L, "Test", "User", "test.user", "encodedCorrectOldPass", true, null, null);

		when(authenticationInfoService.getCurrentUsername()).thenReturn("test.user");
		when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrongOldPass", "encodedCorrectOldPass")).thenReturn(false);

		BaseException exception = assertThrows(BaseException.class,
				() -> authenticationService.changePassword(request));
		assertEquals(MessageType.UNAUTHORIZED, exception.getErrorMessage().getMessageType());
		assertTrue(exception.getMessage().contains("Invalid old password."));
		verify(userRepository, never()).save(any());
	}

	@Test
	void shouldThrowExceptionWhenNewPasswordIsSameAsOld() {
		ChangePasswordRequest request = new ChangePasswordRequest("test.user", "samePass", "samePass");
		User user = new User(1L, "Test", "User", "test.user", "encodedSamePass", true, null, null);

		when(authenticationInfoService.getCurrentUsername()).thenReturn("test.user");
		when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("samePass", "encodedSamePass")).thenReturn(true);

		BaseException exception = assertThrows(BaseException.class,
				() -> authenticationService.changePassword(request));
		assertEquals(MessageType.INVALID_ARGUMENT, exception.getErrorMessage().getMessageType());
		assertTrue(exception.getMessage().contains("New password cannot be the same as the old password."));
	}

	// --- Logout Test ---

	@Test
	void shouldBlacklistTokenOnLogout() {
		String tokenToBlacklist = "dummy.token";
		when(authenticationInfoService.getCurrentUsername()).thenReturn("test.user");

		authenticationService.logout(tokenToBlacklist);

		verify(jwtTokenBlacklistService, times(1)).blacklistToken(tokenToBlacklist);
	}
}