package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.epam.gym_crm.auth.AuthManager;
import com.epam.gym_crm.db.entity.Trainee;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.TraineeRepository;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.dto.request.LoginRequest;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.monitoring.metrics.AppMetrics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

class AuthenticationServiceImplTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private AuthManager authManager;
	@Mock
	private TraineeRepository traineeRepository;

	@Mock
	private HttpServletRequest httpServletRequest;

	@InjectMocks
	private AuthenticationServiceImpl authenticationService;

	@Mock
    private HttpSession session;
	@Mock
	private AppMetrics appMetrics;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(httpServletRequest.getSession(true)).thenReturn(session);
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	@AfterEach
	void teardown() {
		// Her testten sonra web bağlamını temizle
		RequestContextHolder.resetRequestAttributes();
	}

	// --- 3. Trainee/Trainer username and password matching (Login Success) ---
	@Test
	void shouldLoginSuccessfullyForTrainee() {
		LoginRequest request = new LoginRequest("trainee.user", "correctPass");
		User traineeUser = new User(1L, "Trainee", "User", "trainee.user", "correctPass", true, null, null);
		Trainee trainee = new Trainee();
		trainee.setUser(traineeUser);
		traineeUser.setTrainee(trainee);

		when(userRepository.findByUsername("trainee.user")).thenReturn(Optional.of(traineeUser));

		assertDoesNotThrow(() -> authenticationService.login(request));

		verify(userRepository, times(1)).findByUsername("trainee.user");

		// Testin sonunda HttpSession'a doğru kullanıcı eklenmiş mi diye kontrol
		// edebiliriz
		HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
				.getSession(false);
		assertNotNull(session, "Session should not be null after login");
		assertNotNull(session.getAttribute("currentUser"), "currentUser should be in the session");
		assertEquals(traineeUser, session.getAttribute("currentUser"),
				"The user in session should be the one who logged in");
	}

	@Test
	void shouldLoginSuccessfullyForTrainer() {
		LoginRequest request = new LoginRequest("trainer.user", "correctPass");
		User trainerUser = new User(2L, "Trainer", "User", "trainer.user", "correctPass", true, null, null);

		when(userRepository.findByUsername("trainer.user")).thenReturn(Optional.of(trainerUser));
		doNothing().when(authManager).login(trainerUser);

		assertDoesNotThrow(() -> authenticationService.login(request));

		verify(userRepository, times(1)).findByUsername("trainer.user");
	}

	// --- Login - Failure ---
	@Test
	void shouldThrowBaseExceptionWhenLoginWithWrongPassword() {
		LoginRequest request = new LoginRequest("test.user", "wrongPass");
		User user = new User(3L, "Test", "User", "test.user", "correctPass", true, null, null);

		when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(user));

		BaseException exception = assertThrows(BaseException.class, () -> authenticationService.login(request));

		assertEquals(MessageType.UNAUTHORIZED.getMessage() + " : Invalid username or password.",
				exception.getMessage());
		verify(userRepository, times(1)).findByUsername("test.user");
		verify(authManager, never()).login(any(User.class));
	}

	@Test
	void shouldThrowBaseExceptionWhenLoginWithNonExistentUser() {
		LoginRequest request = new LoginRequest("nonexistent.user", "somePass");

		when(userRepository.findByUsername("nonexistent.user")).thenReturn(Optional.empty());

		BaseException exception = assertThrows(BaseException.class, () -> authenticationService.login(request));

		assertEquals(MessageType.UNAUTHORIZED.getMessage() + " : Invalid username or password.",
				exception.getMessage());
		verify(userRepository, times(1)).findByUsername("nonexistent.user");
		verify(authManager, never()).login(any(User.class));
	}

	@Test
	void shouldThrowBaseExceptionWhenLoginWithInactiveUser() {
		LoginRequest request = new LoginRequest("inactive.user", "somePass");
		User inactiveUser = new User(4L, "Inactive", "User", "inactive.user", "somePass", false, null, null);

		when(userRepository.findByUsername("inactive.user")).thenReturn(Optional.of(inactiveUser));

		BaseException exception = assertThrows(BaseException.class, () -> authenticationService.login(request));

		assertEquals(MessageType.UNAUTHORIZED.getMessage() + " : User account is inactive.", exception.getMessage());
		verify(userRepository, times(1)).findByUsername("inactive.user");
		verify(authManager, never()).login(any(User.class));
	}

	@Test
	void shouldThrowBaseExceptionWhenLoginRequestIsNull() {
		BaseException exception = assertThrows(BaseException.class, () -> authenticationService.login(null));

		assertEquals(MessageType.INVALID_ARGUMENT.getMessage() + " : Login request DTO cannot be null.",
				exception.getMessage());
		verify(userRepository, never()).findByUsername(anyString());
		verify(authManager, never()).login(any(User.class));
	}

	// --- createAndSaveUser ---
	@Test
	void shouldCreateAndSaveUserSuccessfully() {
		String firstName = "New";
		String lastName = "User";

		when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

		User createdUser = authenticationService.createAndSaveUser(firstName, lastName);

		assertNotNull(createdUser);
		assertEquals(firstName, createdUser.getFirstName());
		assertEquals(lastName, createdUser.getLastName());
		assertNotNull(createdUser.getUsername());
		assertNotNull(createdUser.getPassword());

		verify(userRepository, times(1)).findByUsername(anyString()); // generateUniqueUsername için
		verify(userRepository, never()).save(any(User.class)); // FIX: save() çağrılmadığını doğrula
	}

	@Test
	void shouldCreateAndSaveUserWithUniqueUsernameWhenBaseUsernameExists() {
		String firstName = "Existing";
		String lastName = "User";
		String baseUsername = "Existing.User";
		String firstUniqueUsername = "Existing.User1";

		when(userRepository.findByUsername(baseUsername)).thenReturn(Optional.of(new User()));
		when(userRepository.findByUsername(firstUniqueUsername)).thenReturn(Optional.empty());

		User createdUser = authenticationService.createAndSaveUser(firstName, lastName);

		assertNotNull(createdUser);
		assertEquals(firstUniqueUsername, createdUser.getUsername());
		verify(userRepository, times(1)).findByUsername(baseUsername);
		verify(userRepository, times(1)).findByUsername(firstUniqueUsername);
		verify(userRepository, never()).save(any(User.class)); // FIX: save() çağrılmadığını doğrula
	}

	// --- 7. Trainee password change & 8. Trainer password change ---
	@Test
	void shouldChangePasswordSuccessfully() {
		ChangePasswordRequest request = new ChangePasswordRequest("test.user", "oldPass", "newPass");
		User user = new User(1L, "Test", "User", "test.user", "oldPass", true, null, null);

		// When authManager.getCurrentUser() is called, it will internally call
		// checkAuthentication().
		// We only need to mock what getCurrentUser() returns.
		when(authManager.getCurrentUser()).thenReturn(user);
		when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		assertDoesNotThrow(() -> authenticationService.changePassword(request));

		// We should no longer verify checkAuthentication() directly, as it's called
		// internally by getCurrentUser().
		verify(authManager, never()).checkAuthentication(); // Confirm it's NOT directly called
		verify(authManager, times(1)).getCurrentUser(); // This is the direct call we expect
		verify(userRepository, times(1)).findByUsername("test.user");
		verify(userRepository, times(1)).save(user);
		assertEquals("newPass", user.getPassword());
	}

	@Test
	void shouldThrowBaseExceptionWhenChangePasswordWithInvalidOldPassword() {
		ChangePasswordRequest request = new ChangePasswordRequest("test.user", "wrongOldPass", "newPass");
		User user = new User(1L, "Test", "User", "test.user", "correctOldPass", true, null, null);

		// When authManager.getCurrentUser() is called, it will internally call
		// checkAuthentication().
		when(authManager.getCurrentUser()).thenReturn(user);
		when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(user));

		BaseException exception = assertThrows(BaseException.class,
				() -> authenticationService.changePassword(request));

		assertEquals(MessageType.UNAUTHORIZED.getMessage() + " : Invalid old password.", exception.getMessage());

		// We should no longer verify checkAuthentication() directly.
		verify(authManager, never()).checkAuthentication(); // Confirm it's NOT directly called
		verify(authManager, times(1)).getCurrentUser(); // This is the direct call we expect
		verify(userRepository, times(1)).findByUsername("test.user");
		verify(userRepository, never()).save(any(User.class)); // User is not saved in this error scenario
	}

	@Test
	void shouldThrowBaseExceptionWhenChangePasswordForNonExistentUser() {
		ChangePasswordRequest request = new ChangePasswordRequest("nonexistent.user", "oldPass", "newPass");
		User currentUser = new User(10L, "Current", "User", "current.user", "pass", true, null, null);

		// When authManager.getCurrentUser() is called, it will internally call
		// checkAuthentication().
		when(authManager.getCurrentUser()).thenReturn(currentUser);
		when(userRepository.findByUsername("nonexistent.user")).thenReturn(Optional.empty());

		BaseException exception = assertThrows(BaseException.class,
				() -> authenticationService.changePassword(request));

		assertEquals(MessageType.UNAUTHORIZED.getMessage() + " : Invalid credentials or user not found.",
				exception.getMessage());

		// We should no longer verify checkAuthentication() directly.
		verify(authManager, never()).checkAuthentication(); // Confirm it's NOT directly called
		verify(authManager, times(1)).getCurrentUser(); // This is the direct call we expect
		verify(userRepository, times(1)).findByUsername("nonexistent.user");
		verify(userRepository, never()).save(any(User.class)); // User is not saved in this error scenario
	}

	@Test
	void shouldThrowBaseExceptionWhenUnauthorizedUserChangesPasswordForAnotherUser() {
		ChangePasswordRequest request = new ChangePasswordRequest("another.user", "oldPass", "newPass");
		User currentUser = new User(10L, "Current", "User", "current.user", "pass", true, null, null);
		User anotherUser = new User(11L, "Another", "User", "another.user", "oldPass", true, null, null);

		// When authManager.getCurrentUser() is called (now only once by the service
		// method)
		when(authManager.getCurrentUser()).thenReturn(currentUser);
		when(userRepository.findByUsername("another.user")).thenReturn(Optional.of(anotherUser));

		BaseException exception = assertThrows(BaseException.class,
				() -> authenticationService.changePassword(request));

		assertEquals(MessageType.FORBIDDEN.getMessage() + " : You are not authorized to change this user's password.",
				exception.getMessage());

		// Now, we expect getCurrentUser() to be called exactly once by the service
		// method.
		verify(authManager, times(1)).getCurrentUser();
		verify(authManager, never()).checkAuthentication(); // checkAuthentication is not directly called by service
															// method, and not by stubbed getCurrentUser.

		verify(userRepository, times(1)).findByUsername("another.user");
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void shouldThrowBaseExceptionWhenNewPasswordIsSameAsOldPassword() {
		ChangePasswordRequest request = new ChangePasswordRequest("test.user", "samePass", "samePass");
		User user = new User(1L, "Test", "User", "test.user", "samePass", true, null, null);

		// When authManager.getCurrentUser() is called, it will internally call
		// checkAuthentication().
		when(authManager.getCurrentUser()).thenReturn(user);
		when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(user));

		BaseException exception = assertThrows(BaseException.class,
				() -> authenticationService.changePassword(request));

		assertEquals(
				MessageType.INVALID_ARGUMENT.getMessage() + " : New password cannot be the same as the old password.",
				exception.getMessage());

		// We should no longer verify checkAuthentication() directly.
		verify(authManager, never()).checkAuthentication();
		verify(authManager, times(1)).getCurrentUser();
		verify(userRepository, times(1)).findByUsername("test.user");
		verify(userRepository, never()).save(any(User.class)); // User is not saved in this error scenario
	}

	@Test
	void shouldThrowBaseExceptionWhenChangePasswordRequestIsNull() {

		when(authManager.getCurrentUser()).thenReturn(new User());

		BaseException exception = assertThrows(BaseException.class, () -> authenticationService.changePassword(null));

		assertEquals(MessageType.INVALID_ARGUMENT.getMessage() + " : Change password request DTO cannot be null.",
				exception.getMessage());

		verify(authManager, times(1)).getCurrentUser();

		verify(authManager, never()).checkAuthentication();

		verify(userRepository, never()).findByUsername(anyString());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void shouldLogoutSuccessfully() {
		User currentUser = new User(1L, "Logged", "Out", "logged.out", "pass", true, null, null);

		when(authManager.getCurrentUser()).thenReturn(currentUser);
		doNothing().when(authManager).logout();

		assertDoesNotThrow(() -> authenticationService.logout());

		verify(authManager, never()).checkAuthentication();

		verify(authManager, times(1)).getCurrentUser();
		verify(authManager, times(1)).logout();
	}
}